package co.kr.user.service;

import co.kr.user.DAO.UserInformationRepository;
import co.kr.user.DAO.UserRepository;
import co.kr.user.DAO.UserVerificationsRepository;
import co.kr.user.model.DTO.mail.EmailMessage;
import co.kr.user.model.DTO.my.UserAmendReq;
import co.kr.user.model.DTO.my.UserDeleteDTO;
import co.kr.user.model.DTO.my.UserProfileDTO;
import co.kr.user.model.DTO.my.UserDTO;
import co.kr.user.model.entity.Users;
import co.kr.user.model.entity.UsersInformation;
import co.kr.user.model.entity.UsersVerifications;
import co.kr.user.model.vo.UsersVerificationsPurPose;
import co.kr.user.model.vo.UsersVerificationsStatus;
import co.kr.user.util.AESUtil;
import co.kr.user.util.EMailUtil;
import co.kr.user.util.RandomCodeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;

/**
 * 컨트롤러의 요청을 받아 Repository를 통해 데이터를 처리하고 가공하여 반환
 */
@Slf4j
@Service
@RequiredArgsConstructor // final 필드에 대한 생성자 자동 주입
public class UserService implements UserServiceImpl{

    private final UserRepository userRepository;
    private final UserInformationRepository userInformationRepository;

    private final AESUtil aesUtil;
    private final RandomCodeUtil randomCodeUtil;
    private final UserVerificationsRepository userVerificationsRepository;
    private final EMailUtil eMailUtil;


    public UserDTO my(Long user_Idx) {
        // DB에서 사용자 조회 (없으면 예외 발생)
        Users users = userRepository.findById(user_Idx)
                .orElseThrow();

        if (users.getDel() == 1) {
            throw new IllegalStateException("탈퇴한 회원입니다.");
        }
        else if (users.getDel() == 2) {
            throw new IllegalStateException("인증을 먼저 시도해 주세요.");
        }

        UserDTO userDTO = new UserDTO();
        userDTO.setID(users.getID());
        userDTO.setRole(users.getRole());
        userDTO.setBalance(users.getBalance());
        userDTO.setCreatedAt(users.getCreatedAt());

        // Entity를 DTO로 변환하여 반환
        return userDTO;
    }

    /**
     * 상세 개인 정보 조회 로직
     * @param user_Idx 조회할 사용자의 PK
     * @return UserProfileResponse (상세 정보 + 부가 정보 DTO)
     */
    public UserProfileDTO myDetails(Long user_Idx) {
        // 기본 유저 정보 조회 (로그인 ID, 역할 등 확인용)
        Users users = userRepository.findById(user_Idx)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이디입니다."));

        if (users.getDel() == 1) {
            throw new IllegalStateException("탈퇴한 회원입니다.");
        }
        else if (users.getDel() == 2) {
            throw new IllegalStateException("인증을 먼저 시도해 주세요.");
        }

        // 상세 유저 정보 조회 (이름, 전화번호 등)
        // UserInformation 테이블에서 해당 유저와 매핑된 정보를 찾음
        UsersInformation userInfo = userInformationRepository.findById(user_Idx)
                .orElseThrow(() -> new IllegalArgumentException("상세 회원 정보를 찾을 수 없습니다. UserID: " + user_Idx));

        if (userInfo.getDel() == 1) {
            throw new IllegalStateException("탈퇴한 회원입니다.");
        }
        /*
         * [MSA 연동 시나리오]
         * Payment Service 등에 API 요청을 보내 카드가 있는지 확인하는 로직이 여기에 추가
         * List<CardDto> cards = paymentClient.getCards(userId);
         */

        UserProfileDTO userProfileDTO = new UserProfileDTO();
        // 민감한 개인정보 복호화
        userProfileDTO.setName(aesUtil.decrypt(userInfo.getName()));
        userProfileDTO.setPhoneNumber(aesUtil.decrypt(userInfo.getPhoneNumber()));
        userProfileDTO.setBirth(aesUtil.decrypt(userInfo.getBirth()));
        userProfileDTO.setGrade("STANDARD"); // 등급 정보는 추후 로직에 따라 동적으로 설정 가능



        // 조회된 정보들을 조합하여 하나의 응답 DTO로 생성
        return userProfileDTO;
    }

    @Override
    @Transactional
    public UserDeleteDTO myDelete(Long user_Idx) {
        Users users = userRepository.findById(user_Idx)
                .orElseThrow();

        // 2. 탈퇴 여부 확인 (Del 컬럼 활용)
        // (Users 엔티티에 getDel() 메서드가 있다고 가정)
        if (users.getDel() == 1) {
            throw new IllegalStateException("탈퇴한 회원입니다.");
        }
        else if (users.getDel() == 2) {
            throw new IllegalStateException("인증을 먼저 시도해 주세요.");
        }

        log.info("user : {}", users.toString());

        UsersVerifications usersVerifications = co.kr.user.model.entity.UsersVerifications.builder()
                .usersIdx(users.getUsersIdx())
                .purPose(UsersVerificationsPurPose.DELETE_ACCOUNT) // 목적: 회원가입 인증
                .code(randomCodeUtil.getCode()) // 랜덤 코드 생성 유틸 호출
                .expiresAt(LocalDateTime.now().plusMinutes(30)) // 유효기간: 30분 뒤
                .status(UsersVerificationsStatus.PENDING) // 상태: 대기 중
                .build();

        UsersVerifications savedUserVerifications = userVerificationsRepository.save(usersVerifications);
        log.info("Saved User Verifications : {}", savedUserVerifications.toString());

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

        // 메일 전송 객체 생성
        EmailMessage emailMessage = EmailMessage.builder()
                .to(users.getID()) // 수신자: 가입한 이메일 ID
                .subject("[GutJJeu] 회원탈퇴 인증번호 안내해 드립니다.")
                .message(finalContent)
                .build();

        // [핵심 변경] 트랜잭션 커밋 후 실행 (After Commit)
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                log.info("Transaction Committed. Sending Email to {}", users.getID());
                eMailUtil.sendEmail(emailMessage, true);
            }
        });

        UserDeleteDTO userDeleteDTO = new UserDeleteDTO();
        userDeleteDTO.setID(users.getID());
        userDeleteDTO.setCertificationTime(LocalDateTime.now());

        return userDeleteDTO;
    }

    @Override
    @Transactional
    public String myDelete(Long user_Idx, String authCode) {

        Users users = userRepository.findById(user_Idx)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이디입니다."));

        if (users.getDel() == 1) {
            throw new IllegalStateException("탈퇴한 회원입니다.");
        }
        else if (users.getDel() == 2) {
            throw new IllegalStateException("인증을 먼저 시도해 주세요.");
        }

        // 3. 해당 유저의 가장 최근 인증 내역 조회
        UsersVerifications verification = userVerificationsRepository.findTopByUsersIdxOrderByCreatedAtDesc(users.getUsersIdx())
                .orElseThrow(() -> new IllegalArgumentException("인증 요청 내역이 존재하지 않습니다."));

        log.info("verification : {}", verification.toString());

        // [추가 검증] 조회해온 인증 내역이 '비밀번호 찾기(RESET_PW)'용인지 확인 필요
        if (verification.getPurPose() != UsersVerificationsPurPose.DELETE_ACCOUNT) {
            throw new IllegalArgumentException("올바르지 않은 인증 요청입니다.");
        }

        // 4. 인증 시간 만료 여부 확인
        if (verification.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("인증 시간이 만료되었습니다. 인증번호를 다시 요청해주세요.");
        }

        // 5. 인증 코드 일치 여부 확인
        if (!verification.getCode().equals(authCode)) {
            throw new IllegalArgumentException("인증번호가 일치하지 않습니다.");
        }

        if (verification.getStatus() == UsersVerificationsStatus.VERIFIED) {
            return "이미 인증 완료된 코드입니다.";
        }

        // 6. 검증 성공 처리
        verification.confirmVerification();

        users.del();

        return "회원 탈퇴가 정상 처리되었습니다.";
    }

    @Override
    public UserAmendReq myAmend(Long user_Idx, UserAmendReq userAmendReq) {
        Users users = userRepository.findById(user_Idx)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이디입니다."));

        if (users.getDel() == 1) {
            throw new IllegalStateException("탈퇴한 회원입니다.");
        }
        else if (users.getDel() == 2) {
            throw new IllegalStateException("인증을 먼저 시도해 주세요.");
        }

        UsersInformation userInfo = userInformationRepository.findById(user_Idx)
                .orElseThrow(() -> new IllegalArgumentException("상세 회원 정보를 찾을 수 없습니다. UserID: " + user_Idx));

        if (userInfo.getDel() == 1) {
            throw new IllegalStateException("탈퇴한 회원입니다.");
        }

        UserAmendReq amend = new UserAmendReq();
        amend.setName(userInfo.getName());
        amend.setPhoneNumber(userInfo.getPhoneNumber());
        amend.setBirth(userInfo.getBirth());
        amend.setGrade("STANDARD");


        if (!userAmendReq.getName().equals(userInfo.getName())) {
            amend.setName(userAmendReq.getName());
        }
        if (!userAmendReq.getPhoneNumber().equals(userInfo.getPhoneNumber())) {
            amend.setPhoneNumber(userAmendReq.getPhoneNumber());
        }
        if (!userAmendReq.getBirth().equals(userInfo.getBirth())) {
            amend.setBirth(userAmendReq.getBirth());
        }

        userInfo.amend(amend.getName(), amend.getPhoneNumber(), amend.getBirth());

        return amend;
    }
}

