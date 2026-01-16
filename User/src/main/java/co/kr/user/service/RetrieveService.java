package co.kr.user.service;

import co.kr.user.DAO.UserInformationRepository;
import co.kr.user.DAO.UserRepository;
import co.kr.user.DAO.UserVerificationsRepository;
import co.kr.user.model.DTO.mail.EmailMessage;
import co.kr.user.model.DTO.retrieve.FindPWFirstStepReq;
import co.kr.user.model.DTO.retrieve.FindPWSecondStepReq;
import co.kr.user.model.DTO.retrieve.FindPWFirstStepDTO;
import co.kr.user.model.entity.Users;
import co.kr.user.model.entity.UsersInformation;
import co.kr.user.model.entity.UsersVerifications;
import co.kr.user.model.vo.UsersVerificationsPurPose;
import co.kr.user.model.vo.UsersVerificationsStatus;
import co.kr.user.util.BCryptUtil;
import co.kr.user.util.EMailUtil;
import co.kr.user.util.RandomCodeUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@Service
@RequiredArgsConstructor // final 필드에 대한 생성자 자동 주입
public class RetrieveService implements RetrieveServiceImpl{

    private final UserRepository userRepository;
    private final UserVerificationsRepository userVerificationsRepository;
    private final UserInformationRepository userInformationRepository;

    private final RandomCodeUtil randomCodeUtil;
    private final EMailUtil eMailUtil;
    private final BCryptUtil bCryptUtil;

    @Override
    @Transactional
    public FindPWFirstStepDTO findPwFirst(FindPWFirstStepReq findPWFirstStepReq) {

        Users users = userRepository.findByID(findPWFirstStepReq.getID())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이디입니다."));

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
                .purPose(UsersVerificationsPurPose.RESET_PW) // 목적: 회원가입 인증
                .code(randomCodeUtil.getCode()) // 랜덤 코드 생성 유틸 호출
                .expiresAt(LocalDateTime.now().plusMinutes(30)) // 유효기간: 30분 뒤
                .status(UsersVerificationsStatus.PENDING) // 상태: 대기 중
                .build();

        UsersVerifications savedUserVerifications = userVerificationsRepository.save(usersVerifications);
        log.info("Saved User Verifications : {}", savedUserVerifications.toString());

        String passwordResetTemplate = """
            <div style='background-color: #f6f7f9; padding: 40px 20px; font-family: "Apple SD Gothic Neo", "Malgun Gothic", sans-serif; line-height: 1.6;'>
                <div style='max-width: 500px; margin: 0 auto; background-color: #ffffff; border-radius: 8px; border: 1px solid #e0e0e0; overflow: hidden; box-shadow: 0 4px 12px rgba(0,0,0,0.05);'>
                    
                    <div style='background-color: #007bff; padding: 20px; text-align: center;'>
                        <h1 style='color: #ffffff; font-size: 20px; margin: 0; font-weight: 600;'>GutJJeu</h1>
                    </div>
            
                    <div style='padding: 30px;'>
                        <h2 style='color: #333; font-size: 22px; margin-top: 0; text-align: center;'>비밀번호 찾기 인증번호</h2>
                        <p style='color: #555; font-size: 16px; margin-bottom: 20px; text-align: center;'>
                            안녕하세요.<br>
                            비밀번호 재설정을 위해 아래 인증번호를 입력해 주세요.
                        </p>
                        
                        <div style='background-color: #f0f4f8; padding: 20px; text-align: center; border-radius: 6px; margin: 30px 0; border: 1px dashed #007bff;'>
                            <span style='font-size: 18px; font-weight: bold; color: #007bff; word-break: break-all; display: inline-block;'>
                                %s
                            </span>
                        </div>
                        
                        <p style='color: #888; font-size: 13px; text-align: center; margin-top: 20px;'>
                            * 이 인증번호는 <strong>30분 동안만 유효</strong>합니다.<br>
                            * 본인이 요청하지 않은 경우 이 메일을 무시해 주세요.
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

        String finalContent = passwordResetTemplate.formatted(savedUserVerifications.getCode());

        // 메일 전송 객체 생성
        EmailMessage emailMessage = EmailMessage.builder()
                .to(users.getID()) // 수신자: 가입한 이메일 ID
                .subject("[GutJJeu] 비밀번호 재설정 인증번호 안내해 드립니다.")
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

        FindPWFirstStepDTO retrieveDTO = new FindPWFirstStepDTO();
        retrieveDTO.setID(users.getID());
        retrieveDTO.setCertificationTime(savedUserVerifications.getExpiresAt());

        return retrieveDTO;
    }

    @Override
    @Transactional
    public String findPwSecond(FindPWSecondStepReq findPWSecondStepReq) {

        Users users = userRepository.findByID(findPWSecondStepReq.getID())
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
        if (verification.getPurPose() != UsersVerificationsPurPose.RESET_PW) {
            throw new IllegalArgumentException("올바르지 않은 인증 요청입니다.");
        }

        // 4. 인증 시간 만료 여부 확인
        if (verification.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("인증 시간이 만료되었습니다. 인증번호를 다시 요청해주세요.");
        }

        // 5. 인증 코드 일치 여부 확인
        if (!verification.getCode().equals(findPWSecondStepReq.getAuthCode())) {
            throw new IllegalArgumentException("인증번호가 일치하지 않습니다.");
        }

        if (verification.getStatus() == UsersVerificationsStatus.VERIFIED) {
            return "이미 인증 완료된 코드입니다.";
        }

        if (!findPWSecondStepReq.getNewPW().equals(findPWSecondStepReq.getNewPWCheck())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        UsersInformation usersInformation = userInformationRepository.findById(users.getUsersIdx())
                .orElseThrow();

        usersInformation.lastPassword(users.getPW());

        users.setPW(bCryptUtil.setPassword(findPWSecondStepReq.getNewPW()));

        // 6. 검증 성공 처리
        verification.confirmVerification();

        return "비밀번호 재설정이 정상 처리되었습니다.";
    }
}
