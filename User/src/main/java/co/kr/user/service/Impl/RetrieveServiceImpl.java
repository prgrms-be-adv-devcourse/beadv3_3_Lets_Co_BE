package co.kr.user.service.Impl;

import co.kr.user.dao.UserInformationRepository;
import co.kr.user.dao.UserRepository;
import co.kr.user.dao.UserVerificationsRepository;
import co.kr.user.model.dto.mail.EmailMessage;
import co.kr.user.model.dto.retrieve.FindPWFirstStepReq;
import co.kr.user.model.dto.retrieve.FindPWSecondStepReq;
import co.kr.user.model.dto.retrieve.FindPWFirstStepDTO;
import co.kr.user.model.entity.Users;
import co.kr.user.model.entity.UsersInformation;
import co.kr.user.model.entity.UsersVerifications;
import co.kr.user.model.vo.UsersVerificationsPurPose;
import co.kr.user.model.vo.UsersVerificationsStatus;
import co.kr.user.service.RetrieveService;
import co.kr.user.util.BCryptUtil;
import co.kr.user.util.MailUtil;
import co.kr.user.util.RandomCodeUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * 회원 정보 찾기(Retrieve) 관련 비즈니스 로직을 처리하는 서비스 클래스입니다.
 * 비밀번호 재설정을 위한 인증번호 발송 및 검증, 비밀번호 변경 기능을 수행합니다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RetrieveServiceImpl implements RetrieveService {
    private final UserRepository userRepository;
    private final UserVerificationsRepository userVerificationsRepository;
    private final UserInformationRepository userInformationRepository;

    private final UserQueryServiceImpl userQueryServiceImpl;

    private final RandomCodeUtil randomCodeUtil; // 인증코드 생성 유틸리티
    private final MailUtil mailUtil; // 이메일 발송 유틸리티
    private final BCryptUtil bCryptUtil; // 비밀번호 암호화 유틸리티

    /**
     * 비밀번호 찾기 1단계: 인증번호 발송 메서드입니다.
     * 사용자가 입력한 아이디(이메일)로 회원을 조회하고, 본인 인증을 위한 코드를 이메일로 전송합니다.
     *
     * @param findPWFirstStepReq 아이디 정보가 담긴 요청 객체
     * @return FindPWFirstStepDTO (인증 요청 결과 및 만료 시간)
     */
    @Override
    @Transactional
    public FindPWFirstStepDTO findPwFirst(FindPWFirstStepReq findPWFirstStepReq) {
        Users users = userQueryServiceImpl.findActiveUserById(findPWFirstStepReq.getID());

        // 인증 코드 생성 및 DB 저장 (목적: RESET_PW)
        UsersVerifications usersVerifications = co.kr.user.model.entity.UsersVerifications.builder()
                .usersIdx(users.getUsersIdx())
                .purpose(UsersVerificationsPurPose.RESET_PW)
                .code(randomCodeUtil.getCode())
                .expiresAt(LocalDateTime.now().plusMinutes(30)) // 유효시간 30분
                .status(UsersVerificationsStatus.PENDING)
                .build();

        UsersVerifications savedUserVerifications = userVerificationsRepository.save(usersVerifications);

        // 이메일 템플릿 작성
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

        EmailMessage emailMessage = EmailMessage.builder()
                .to(users.getId())
                .subject("[GutJJeu] 비밀번호 재설정 인증번호 안내해 드립니다.")
                .message(finalContent)
                .build();

        // 트랜잭션 커밋 후 이메일 전송 (데이터 정합성 및 비동기 처리 고려)
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                mailUtil.sendEmail(emailMessage, true);
            }
        });

        // 결과 반환
        FindPWFirstStepDTO retrieveDTO = new FindPWFirstStepDTO();
        retrieveDTO.setID(users.getId());
        retrieveDTO.setCertificationTime(savedUserVerifications.getExpiresAt());

        return retrieveDTO;
    }

    /**
     * 비밀번호 찾기 2단계: 인증 확인 및 비밀번호 변경 메서드입니다.
     * 사용자가 입력한 인증 코드를 검증하고, 유효할 경우 비밀번호를 재설정합니다.
     *
     * @param findPWSecondStepReq 인증 코드 및 새 비밀번호 정보
     * @return 처리 결과 메시지
     */
    @Override
    @Transactional
    public String findPwSecond(FindPWSecondStepReq findPWSecondStepReq) {
        Users users = userQueryServiceImpl.findActiveUserById(findPWSecondStepReq.getID());

        // 최신 인증 내역 조회
        UsersVerifications verification = userVerificationsRepository.findTopByUsersIdxAndDelOrderByCreatedAtDesc(users.getUsersIdx(), 0)
                .orElseThrow(() -> new IllegalArgumentException("인증 요청 내역이 존재하지 않습니다."));

        // 인증 내역 유효성 검사 (목적, 만료 시간, 코드 일치 여부)
        if (verification.getPurpose() != UsersVerificationsPurPose.RESET_PW) {
            throw new IllegalArgumentException("올바르지 않은 인증 요청입니다.");
        }

        if (verification.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("인증 시간이 만료되었습니다. 인증번호를 다시 요청해주세요.");
        }

        if (!verification.getCode().equals(findPWSecondStepReq.getAuthCode())) {
            throw new IllegalArgumentException("인증번호가 일치하지 않습니다.");
        }

        if (verification.getStatus() == UsersVerificationsStatus.VERIFIED) {
            return "이미 인증 완료된 코드입니다.";
        }

        // 비밀번호 확인 검증
        if (!findPWSecondStepReq.getNewPW().equals(findPWSecondStepReq.getNewPWCheck())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 기존 비밀번호 백업 (UsersInformation 테이블)
        UsersInformation usersInformation = userInformationRepository.findById(users.getUsersIdx())
                .orElseThrow(() -> new IllegalArgumentException("비밀번호 재설정을 위한 사용자 상세 정보를 찾을 수 없습니다. (UserIDX: " + users.getUsersIdx() + ")"));
        usersInformation.updatePrePW(users.getPw());

        // 새 비밀번호 암호화 및 저장
        users.changePassword(bCryptUtil.encode(findPWSecondStepReq.getNewPW()));

        // 인증 상태 완료로 변경
        verification.confirmVerification();

        return "비밀번호 재설정이 정상 처리되었습니다.";
    }
}