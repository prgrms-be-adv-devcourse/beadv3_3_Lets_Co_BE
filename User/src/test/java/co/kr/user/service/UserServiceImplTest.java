package co.kr.user.service;

import co.kr.user.DAO.UserInformationRepository;
import co.kr.user.DAO.UserRepository;
import co.kr.user.DAO.UserVerificationsRepository;
import co.kr.user.DAO.UsersLoginRepository;
import co.kr.user.model.DTO.mail.EmailMessage;
import co.kr.user.model.DTO.my.UserAmendReq;
import co.kr.user.model.DTO.my.UserDTO;
import co.kr.user.model.DTO.my.UserDeleteDTO;
import co.kr.user.model.DTO.my.UserProfileDTO;
import co.kr.user.model.entity.Users;
import co.kr.user.model.entity.UsersInformation;
import co.kr.user.model.entity.UsersLogin;
import co.kr.user.model.entity.UsersVerifications;
import co.kr.user.model.vo.UsersVerificationsPurPose;
import co.kr.user.model.vo.UsersVerificationsStatus;
import co.kr.user.util.AesUtil;
import co.kr.user.util.CookieUtil;
import co.kr.user.util.MailUtil;
import co.kr.user.util.RandomCodeUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @InjectMocks UserServiceImpl userService;

    @Mock AesUtil aesUtil;
    @Mock RandomCodeUtil randomCodeUtil;
    @Mock MailUtil mailUtil;

    @Mock UserRepository userRepository;
    @Mock UsersLoginRepository usersLoginRepository;
    @Mock UserInformationRepository userInformationRepository;
    @Mock UserVerificationsRepository userVerificationsRepository;

    Long USER_IDX = 10L;

    @BeforeEach
    void init () {
        TransactionSynchronizationManager.initSynchronization();
    }

    @AfterEach
    void clear() {
        TransactionSynchronizationManager.clearSynchronization();
    }


    @Test
    @DisplayName("마이페이지 - 정상")
    void 마이_페이지 () {

        // given
        Users user = createUser();
        given(userRepository.findById(USER_IDX)).willReturn(Optional.ofNullable(user));

        // when
        UserDTO result = userService.my(USER_IDX);

        // then
        assertThat(result.getID()).isEqualTo(user.getID());
        assertThat(result.getRole()).isEqualTo(user.getRole());
        assertThat(result.getBalance()).isEqualTo(user.getBalance());

        // 확인용
        System.out.println(result);
    }

    @Test
    @DisplayName("마이페이지 상세 - 정상")
    void 마이_페이지_상세 () {

        given(aesUtil.decrypt(any())).willAnswer(invocation -> invocation.getArgument(0));

        // given
        Users user = createUser();
        UsersInformation usersInformation = createUsersInformation(USER_IDX);

        given(userRepository.findById(USER_IDX)).willReturn(Optional.ofNullable(user));
        given(userInformationRepository.findById(USER_IDX)).willReturn(Optional.ofNullable(usersInformation));

        // when
        UserProfileDTO result = userService.myDetails(USER_IDX);

        // then
        assertThat(result.getName()).isEqualTo("홍길동");
        assertThat(result.getPhoneNumber()).isEqualTo("01012345678");
        assertThat(result.getBirth()).isEqualTo("19900101");

        // 확인용
        System.out.println(result);
    }

    @Test
    @DisplayName("회원 탈퇴 1차 - 정상")
    void 회원_탈퇴_1차() {
        // lenient 설정은 그대로 유지 (필요한 경우)
        lenient().when(aesUtil.decrypt(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // given
        Users user = createUser();
        UsersVerifications savedVerifications = createUsersVerification(USER_IDX);

        given(userRepository.findById(USER_IDX)).willReturn(Optional.ofNullable(user));
        given(randomCodeUtil.getCode()).willReturn("123456");
        given(userVerificationsRepository.save(any(UsersVerifications.class))).willReturn(savedVerifications);

        // when
        // 1차 탈퇴 요청은 파라미터가 1개(userIdx)이므로 그대로 둡니다.
        UserDeleteDTO result = userService.myDelete(USER_IDX);

        // then
        verify(userVerificationsRepository).save(any(UsersVerifications.class));

        // 트랜잭션 동기화 (이메일 발송)
        List<TransactionSynchronization> synchronizations = TransactionSynchronizationManager.getSynchronizations();
        for (TransactionSynchronization sync : synchronizations) {
            sync.afterCommit();
        }
        verify(mailUtil).sendEmail(any(EmailMessage.class), eq(true));

        System.out.println(result);
    }

    @Test
    @DisplayName("회원 탈퇴 확정 2단계 - 정상")
    void 회원_탈퇴_2단계() {

        // given
        Users user = createUser();
        UsersVerifications verification = createUsersVerification(USER_IDX);

        UsersLogin usersLogin = UsersLogin.builder()
                .usersIdx(USER_IDX)
                .token("access_token")
                .build();

        MockHttpServletResponse response = new MockHttpServletResponse();

        given(userRepository.findById(USER_IDX)).willReturn(Optional.ofNullable(user));
        given(userVerificationsRepository.findTopByUsersIdxAndDelOrderByCreatedAtDesc(USER_IDX, 0)) .willReturn(Optional.of(verification));
        given(usersLoginRepository.findFirstByUsersIdxOrderByLoginIdxDesc(USER_IDX)).willReturn(usersLogin);

        // when
        String result = userService.myDelete(USER_IDX, verification.getCode(), response);

        // then
        assertThat(result).isEqualTo("회원 탈퇴가 정상 처리되었습니다.");
        assertThat(user.getDel()).isEqualTo(1); // 탈퇴 상태 변경 확인

        // 쿠키가 삭제되었는지 확인 (MaxAge가 0인지 확인)
        assertThat(response.getCookie(CookieUtil.ACCESS_TOKEN_NAME).getMaxAge()).isZero();
        assertThat(response.getCookie(CookieUtil.REFRESH_TOKEN_NAME).getMaxAge()).isZero();

        System.out.println(result);
    }

    @Test
    @DisplayName("회원 수정 - 정상")
    void 회원_수정_정상 () {

        lenient().when(aesUtil.encrypt(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // given
        Users user = createUser();
        UsersInformation usersInformation = createUsersInformation(USER_IDX);
        UserAmendReq userAmendReq = createUserAmendReq();

        given(userRepository.findById(USER_IDX)).willReturn(Optional.ofNullable(user));
        given(userInformationRepository.findById(USER_IDX)).willReturn(Optional.ofNullable(usersInformation));

        // when
        UserAmendReq result = userService.myAmend(USER_IDX, userAmendReq);

        // then
        assertThat(result.getName()).isEqualTo("김철수");
        assertThat(result.getPhoneNumber()).isEqualTo("0101111111");

        // 확인용
        System.out.println(result);
    }


    /*
     * ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ
     * 헬퍼 매서드 (Given)
     * ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ
     */
    private Users createUser() {

        Users result = Users.builder()
                .ID("dummy" + System.currentTimeMillis() + "@test.com")
                .PW("encrypted_pw")
                .agreeTermsAt(LocalDateTime.now())
                .agreePrivacyAt(LocalDateTime.now())
                .build();

        // setter나 builder가 없어서 임시로
        ReflectionTestUtils.setField(result, "usersIdx", USER_IDX);
        ReflectionTestUtils.setField(result, "balance", BigDecimal.valueOf(10000));
        ReflectionTestUtils.setField(result, "del", 0);
        ReflectionTestUtils.setField(result, "createdAt", LocalDateTime.now());

        return result;
    }

    private UsersInformation createUsersInformation(Long usersIdx) {

        return UsersInformation.builder()
                .usersIdx(usersIdx)
                .name("홍길동")
                .phoneNumber("01012345678")
                .birth("19900101")
                .build();
    }

    // UsersVerifications 테스트용 생성 메서드 (탈퇴용)
    private UsersVerifications createUsersVerification(Long usersIdx) {

        return UsersVerifications.builder()
                .usersIdx(usersIdx)
                .purPose(UsersVerificationsPurPose.DELETE_ACCOUNT)
                .code("123456")
                .expiresAt(LocalDateTime.now().plusMinutes(30))
                .status(UsersVerificationsStatus.PENDING)
                .build();
    }

    private UserAmendReq createUserAmendReq() {

        UserAmendReq req = new UserAmendReq();
        req.setName("김철수");
        req.setPhoneNumber("0101111111");
        req.setBirth("19900101");
        req.setGrade("STANDARD");

        return req;
    }
}