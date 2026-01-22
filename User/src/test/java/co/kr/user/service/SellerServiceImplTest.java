package co.kr.user.service;

import co.kr.user.DAO.SellerRepository;
import co.kr.user.DAO.UserInformationRepository;
import co.kr.user.DAO.UserRepository;
import co.kr.user.DAO.UserVerificationsRepository;
import co.kr.user.model.DTO.mail.EmailMessage;
import co.kr.user.model.DTO.my.UserAmendReq;
import co.kr.user.model.DTO.seller.SellerRegisterDTO;
import co.kr.user.model.DTO.seller.SellerRegisterReq;
import co.kr.user.model.entity.Seller;
import co.kr.user.model.entity.Users;
import co.kr.user.model.entity.UsersInformation;
import co.kr.user.model.entity.UsersVerifications;
import co.kr.user.model.vo.UsersRole;
import co.kr.user.model.vo.UsersVerificationsPurPose;
import co.kr.user.model.vo.UsersVerificationsStatus;
import co.kr.user.util.AesUtil;
import co.kr.user.util.BCryptUtil;
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
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SellerServiceImplTest {

    @InjectMocks SellerServiceImpl sellerService;

    @Mock UserRepository userRepository;
    @Mock SellerRepository sellerRepository;
    @Mock UserVerificationsRepository userVerificationsRepository;
    @Mock UserInformationRepository userInformationRepository;

    @Mock MailUtil mailUtil;
    @Mock RandomCodeUtil randomCodeUtil;
    @Mock AesUtil aesUtil;
    @Mock BCryptUtil bCryptUtil;

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
    @DisplayName("판매자 등록 신청 - 성공")
    void 판매자_등록_신청() {

        // given
        Users user = createUser();
        UsersVerifications sellerVerification = createSellerVerification(USER_IDX);

        given(userRepository.findById(USER_IDX)).willReturn(Optional.ofNullable(user));
        given(randomCodeUtil.getCode()).willReturn("123456");
        given(userVerificationsRepository.save(any(UsersVerifications.class))).willReturn(sellerVerification);

        // when
        SellerRegisterDTO result = sellerService.sellerRegister(USER_IDX, createSellerRegisterReq());

        // then
        assertThat(result.getID()).isEqualTo(user.getID());
        verify(sellerRepository).save(any(Seller.class));
        verify(userVerificationsRepository).save(any(UsersVerifications.class));

        List<TransactionSynchronization> synchronizations = TransactionSynchronizationManager.getSynchronizations();
        for (TransactionSynchronization sync : synchronizations) {
            sync.afterCommit();
        }

        // 확인용
        System.out.println(result);
    }

    @Test
    @DisplayName("판매자 등록 확인 - 성공")
    void 판매자_등록_확인 () {

        // given
        Users user = createUser();
        Seller seller = createSeller(USER_IDX);
        UsersVerifications sellerVerification = createSellerVerification(USER_IDX);
        UsersInformation userInfo = createUsersInformation(USER_IDX);

        given(userRepository.findById(USER_IDX)).willReturn(Optional.of(user));
        given(userVerificationsRepository.findTopByUsersIdxAndDelOrderByCreatedAtDesc(USER_IDX, 0)).willReturn(Optional.of(sellerVerification));
        given(sellerRepository.findBySellerIdxAndDel(USER_IDX, 0)).willReturn(Optional.of(seller));

        given(userInformationRepository.findById(USER_IDX)).willReturn(Optional.of(userInfo));
        given(aesUtil.decrypt(any())).willReturn("홍길동");

        // when
        String result = sellerService.sellerRegisterCheck(USER_IDX, "123456");

        // then
        assertThat(result).isEqualTo("판매자 등록이 완료되었습니다.");
        assertThat(user.getRole()).isEqualTo(UsersRole.SELLER);

        // 트랜잭션 동기화 (이메일 발송)
        List<TransactionSynchronization> synchronizations = TransactionSynchronizationManager.getSynchronizations();
        for (TransactionSynchronization sync : synchronizations) {
            sync.afterCommit();
        }
        verify(mailUtil).sendEmail(any(EmailMessage.class), eq(true));

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

        ReflectionTestUtils.setField(result, "usersIdx", USER_IDX);
        ReflectionTestUtils.setField(result, "balance", BigDecimal.valueOf(10000));
        ReflectionTestUtils.setField(result, "del", 0);
        ReflectionTestUtils.setField(result, "createdAt", LocalDateTime.now());

        return result;
    }

    private Seller createSeller(Long usersIdx) {

        return Seller.builder()
                .sellerIdx(usersIdx)
                .businessLicense("123-45-67890")
                .bankBrand("국민은행")
                .bankName("홍길동")
                .bankToken("encrypted-bank-token-1234")
                .build();
    }

    private SellerRegisterReq createSellerRegisterReq() {

        SellerRegisterReq req = new SellerRegisterReq();
        req.setBusinessLicense("123-45-67890");
        req.setBankBrand("국민은행");
        req.setBankName("홍길동");
        req.setBankToken("encrypted-bank-token-1234");

        return req;
    }

    private UsersInformation createUsersInformation(Long usersIdx) {

        return UsersInformation.builder()
                .usersIdx(usersIdx)
                .name("홍길동")
                .phoneNumber("01012345678")
                .birth("19900101")
                .build();
    }

    // UsersVerifications 테스트용 생성 메서드 (셀러용)
    private UsersVerifications createSellerVerification(Long usersIdx) {

        return UsersVerifications.builder()
                .usersIdx(usersIdx)
                .purPose(UsersVerificationsPurPose.SELLER_SIGNUP)
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

