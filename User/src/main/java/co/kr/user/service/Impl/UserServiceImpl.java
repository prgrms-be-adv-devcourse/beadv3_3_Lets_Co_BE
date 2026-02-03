package co.kr.user.service.Impl;

import co.kr.user.dao.UserInformationRepository;
import co.kr.user.dao.UserRepository;
import co.kr.user.dao.UserVerificationsRepository;
import co.kr.user.dao.UsersLoginRepository;
import co.kr.user.model.dto.mail.EmailMessage;
import co.kr.user.model.dto.my.UserAmendReq;
import co.kr.user.model.dto.my.UserDeleteDTO;
import co.kr.user.model.dto.my.UserProfileDTO;
import co.kr.user.model.dto.my.UserDTO;
import co.kr.user.model.entity.Users;
import co.kr.user.model.entity.UsersInformation;
import co.kr.user.model.entity.UsersLogin;
import co.kr.user.model.entity.UsersVerifications;
import co.kr.user.model.vo.UsersVerificationsPurPose;
import co.kr.user.model.vo.UsersVerificationsStatus;
import co.kr.user.service.UserService;
import co.kr.user.util.AESUtil;
import co.kr.user.util.CookieUtil;
import co.kr.user.util.MailUtil;
import co.kr.user.util.RandomCodeUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;

/**
 * 회원 정보 관리(마이페이지) 관련 비즈니스 로직을 처리하는 서비스 클래스입니다.
 * 내 정보 조회, 상세 정보 수정, 회원 탈퇴 요청 및 처리 등의 기능을 수행합니다.
 */
@Service // 스프링 서비스 빈으로 등록합니다.
@RequiredArgsConstructor // final 필드 생성자 주입을 자동화합니다.
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserInformationRepository userInformationRepository;
    private final UserVerificationsRepository userVerificationsRepository;
    private final UsersLoginRepository usersLoginRepository;

    private final AESUtil aesUtil; // 양방향 암호화 유틸리티 (이름, 전화번호 등 복호화용)
    private final RandomCodeUtil randomCodeUtil; // 인증번호 생성 유틸리티
    private final MailUtil mailUtil; // 이메일 발송 유틸리티

    /**
     * 내 정보 조회(기본 정보) 메서드입니다.
     * 사용자의 아이디, 권한, 잔액, 가입일 등 민감하지 않은 기본 정보를 반환합니다.
     *
     * @param userIdx 로그인한 사용자의 식별자
     * @return UserDTO (기본 회원 정보)
     */
    public UserDTO my(Long userIdx) {
        // 사용자 조회 (없을 경우 예외 발생)
        Users users = userRepository.findById(userIdx)
                .orElseThrow();

        // 탈퇴(1) 또는 미인증(2) 상태 확인
        if (users.getDel() == 1) {
            throw new IllegalStateException("탈퇴한 회원입니다.");
        }
        else if (users.getDel() == 2) {
            throw new IllegalStateException("인증을 먼저 시도해 주세요.");
        }

        // DTO 변환 및 반환
        UserDTO userDTO = new UserDTO();
        userDTO.setId(users.getId());
        userDTO.setRole(users.getRole());
        userDTO.setMembership(users.getMembership());
        userDTO.setCreatedAt(users.getCreatedAt());

        return userDTO;
    }

    /**
     * 내 상세 정보 조회 메서드입니다.
     * 암호화되어 저장된 상세 정보(이름, 전화번호, 생년월일)를 복호화하여 반환합니다.
     *
     * @param userIdx 로그인한 사용자의 식별자
     * @return UserProfileDTO (상세 회원 정보)
     */
    public UserProfileDTO myDetails(Long userIdx) {
        Users users = userRepository.findById(userIdx)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이디입니다."));

        // 계정 상태 검증
        if (users.getDel() == 1) {
            throw new IllegalStateException("탈퇴한 회원입니다.");
        }
        else if (users.getDel() == 2) {
            throw new IllegalStateException("인증을 먼저 시도해 주세요.");
        }

        // 상세 정보 조회
        UsersInformation userInfo = userInformationRepository.findById(userIdx)
                .orElseThrow(() -> new IllegalArgumentException("상세 회원 정보를 찾을 수 없습니다. UserID: " + userIdx));

        // 암호화된 데이터를 복호화하여 DTO에 설정
        UserProfileDTO userProfileDTO = new UserProfileDTO();
        userProfileDTO.setGender(userInfo.getGender());
        userProfileDTO.setBalance(userInfo.getBalance());
        userProfileDTO.setName(aesUtil.decrypt(userInfo.getName()));
        userProfileDTO.setPhoneNumber(aesUtil.decrypt(userInfo.getPhoneNumber()));
        userProfileDTO.setBirth(aesUtil.decrypt(userInfo.getBirth()));
        userProfileDTO.setAgreeMarketingAt(userInfo.getAgreeMarketingAt());

        return userProfileDTO;
    }

    /**
     * 회원 탈퇴 요청(1단계) 메서드입니다.
     * 탈퇴를 위한 인증번호를 생성하여 이메일로 발송합니다.
     *
     * @param userIdx 로그인한 사용자의 식별자
     * @return UserDeleteDTO (인증 요청 정보)
     */
    @Override
    @Transactional // 트랜잭션 처리 (인증 정보 저장)
    public UserDeleteDTO myDelete(Long userIdx) {
        Users users = userRepository.findById(userIdx)
                .orElseThrow();

        if (users.getDel() == 1) {
            throw new IllegalStateException("탈퇴한 회원입니다.");
        }
        else if (users.getDel() == 2) {
            throw new IllegalStateException("인증을 먼저 시도해 주세요.");
        }

        // 탈퇴용 인증번호 생성 및 저장
        UsersVerifications usersVerifications = co.kr.user.model.entity.UsersVerifications.builder()
                .usersIdx(users.getUsersIdx())
                .purPose(UsersVerificationsPurPose.DELETE_ACCOUNT) // 목적: 회원 탈퇴
                .code(randomCodeUtil.getCode())
                .expiresAt(LocalDateTime.now().plusMinutes(30)) // 유효기간 30분
                .status(UsersVerificationsStatus.PENDING)
                .build();

        UsersVerifications savedUserVerifications = userVerificationsRepository.save(usersVerifications);

        // 이메일 본문 생성 (HTML)
        String accountDeletionTemplate = """
            <div style='background-color: #f6f7f9; padding: 40px 20px; font-family: "Apple SD Gothic Neo", "Malgun Gothic", sans-serif; line-height: 1.6;'>
                <div style='max-width: 500px; margin: 0 auto; background-color: #ffffff; border-radius: 8px; border: 1px solid #e0e0e0; overflow: hidden; box-shadow: 0 4px 12px rgba(0,0,0,0.05);'>
                    
                    <div style='background-color: #007bff; padding: 20px; text-align: center;'>
                        <h1 style='color: #ffffff; font-size: 20px; margin: 0; font-weight: 600;'>GutJJeu</h1>
                    </div>
            
                    <div style='padding: 30px;'>
                        <h2 style='color: #333; font-size: 22px; margin-top: 0; text-align: center;'>회원탈퇴 인증번호</h2>
                        <p style='color: #555; font-size: 16px; margin-bottom: 20px; text-align: center;'>
                            안녕하세요.<br>
                            회원탈퇴 처리를 위해 아래 인증번호를 입력해 주세요.
                        </p>
                        
                        <div style='background-color: #f0f4f8; padding: 20px; text-align: center; border-radius: 6px; margin: 30px 0; border: 1px dashed #007bff;'>
                            <span style='font-size: 18px; font-weight: bold; color: #007bff; word-break: break-all; display: inline-block;'>
                                %s
                            </span>
                        </div>
                        
                        <p style='color: #888; font-size: 13px; text-align: center; margin-top: 20px;'>
                            * 이 인증번호는 <strong>30분 동안만 유효</strong>합니다.<br>
                            * 본인이 요청하지 않은 경우, 절대 타인에게 공유하지 마세요.
                        </p>
                    </div>
            
                    <div style='background-color: #fafafa; padding: 15px; text-align: center; border-top: 1px solid #eee;'>
                        <p style='color: #aaa; font-size: 11px; margin: 0;'>
                            © 2026 GutJJeu. All rights reserved.
                        </p>
                    </div>
                </div>
            </div>
            """;

        String finalContent = accountDeletionTemplate.formatted(savedUserVerifications.getCode());

        EmailMessage emailMessage = EmailMessage.builder()
                .to(users.getId())
                .subject("[GutJJeu] 회원탈퇴 인증번호 안내해 드립니다.")
                .message(finalContent)
                .build();

        // 트랜잭션 커밋 후 이메일 전송 (비동기 처리 등을 위해)
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                mailUtil.sendEmail(emailMessage, true);
            }
        });

        UserDeleteDTO userDeleteDTO = new UserDeleteDTO();
        userDeleteDTO.setID(users.getId());
        userDeleteDTO.setCertificationTime(LocalDateTime.now());

        return userDeleteDTO;
    }

    /**
     * 회원 탈퇴 확정(2단계) 메서드입니다.
     * 사용자가 입력한 인증번호를 검증하고, 일치할 경우 계정을 탈퇴(Soft Delete) 처리합니다.
     *
     * @param userIdx 로그인한 사용자의 식별자
     * @param authCode 사용자가 입력한 인증번호
     * @return 처리 결과 메시지
     */
    @Override
    @Transactional
    public String myDelete(Long userIdx, String authCode, HttpServletResponse response) {
        Users users = userRepository.findById(userIdx)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이디입니다."));

        if (users.getDel() == 1) {
            throw new IllegalStateException("탈퇴한 회원입니다.");
        }
        else if (users.getDel() == 2) {
            throw new IllegalStateException("인증을 먼저 시도해 주세요.");
        }

        // 최신 인증 요청 조회
        UsersVerifications verification = userVerificationsRepository.findTopByUsersIdxAndDelOrderByCreatedAtDesc(users.getUsersIdx(), 0)
                .orElseThrow(() -> new IllegalArgumentException("인증 요청 내역이 존재하지 않습니다."));

        // 인증 목적 및 만료 시간, 코드 일치 여부 검증
        if (verification.getPurPose() != UsersVerificationsPurPose.DELETE_ACCOUNT) {
            throw new IllegalArgumentException("올바르지 않은 인증 요청입니다.");
        }

        if (verification.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("인증 시간이 만료되었습니다. 인증번호를 다시 요청해주세요.");
        }

        if (!verification.getCode().equals(authCode)) {
            throw new IllegalArgumentException("인증번호가 일치하지 않습니다.");
        }

        if (verification.getStatus() == UsersVerificationsStatus.VERIFIED) {
            return "이미 인증 완료된 코드입니다.";
        }

        // 인증 완료 처리
        verification.confirmVerification();

        // 회원 탈퇴 처리 (Del = 1)
        users.deleteUsers();

        // 토큰 폐기 처리
        UsersLogin usersLogin = usersLoginRepository.findFirstByUsersIdxOrderByLoginIdxDesc((users.getUsersIdx()));

        if (usersLogin != null) {
            if (usersLogin.getRevokedAt() == null && usersLogin.getRevokeReason() == null) {
                usersLogin.lockToken(LocalDateTime.now(), "LOCKED");
            }
        }


        // 2. 클라이언트 브라우저에 저장된 Access Token 쿠키를 삭제합니다.
        //    (유효 시간을 0으로 설정한 쿠키를 덮어씌워 삭제 효과를 냅니다.)
        CookieUtil.deleteCookie(response, CookieUtil.ACCESS_TOKEN_NAME);
        // 3. 클라이언트 브라우저에 저장된 Refresh Token 쿠키를 삭제합니다.
        CookieUtil.deleteCookie(response, CookieUtil.REFRESH_TOKEN_NAME);

        return "회원 탈퇴가 정상 처리되었습니다.";
    }

    /**
     * 회원 정보 수정 메서드입니다.
     * 요청된 정보(이름, 전화번호, 생년월일)를 암호화하여 DB에 업데이트합니다.
     * 빈 값이 아닌 항목만 부분 수정(Patch)합니다.
     *
     * @param userIdx 로그인한 사용자의 식별자
     * @param userAmendReq 수정할 정보가 담긴 DTO
     * @return UserAmendReq (수정 반영된 정보)
     */
    @Override
    @Transactional
    public UserAmendReq myAmend(Long userIdx, UserAmendReq userAmendReq) {
        Users users = userRepository.findById(userIdx)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이디입니다."));

        if (users.getDel() == 1) {
            throw new IllegalStateException("탈퇴한 회원입니다.");
        }
        else if (users.getDel() == 2) {
            throw new IllegalStateException("인증을 먼저 시도해 주세요.");
        }

        UsersInformation userInfo = userInformationRepository.findById(userIdx)
                .orElseThrow(() -> new IllegalArgumentException("상세 회원 정보를 찾을 수 없습니다. UserID: " + userIdx));

        // 기존 정보로 DTO 초기화
        UserAmendReq amend = new UserAmendReq();
        amend.setGender(userInfo.getGender());
        amend.setName(userInfo.getName());
        amend.setPhoneNumber(userInfo.getPhoneNumber());
        amend.setBirth(userInfo.getBirth());
        amend.setAgreeMarketingAt(userInfo.getAgreeMarketingAt());

        // 입력된 값이 있는 경우에만 암호화하여 수정 객체에 반영
        if (!userAmendReq.getGender().equals(userInfo.getGender())) {
            amend.setGender(userAmendReq.getGender());
        }
        if (!userAmendReq.getName().isEmpty()) {
            amend.setName(aesUtil.encrypt(userAmendReq.getName()));
        }
        if (!userAmendReq.getPhoneNumber().isEmpty()) {
            amend.setPhoneNumber(aesUtil.encrypt(userAmendReq.getPhoneNumber()));
        }
        if (!userAmendReq.getBirth().isEmpty()) {
            amend.setBirth(aesUtil.encrypt(userAmendReq.getBirth()));
        }

        // 엔티티 업데이트
        userInfo.updateInformation(amend.getGender(), amend.getName(), amend.getPhoneNumber(), amend.getBirth());

        return amend;
    }
}