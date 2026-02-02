package co.kr.user.service.Impl;

import co.kr.user.dao.UserInformationRepository;
import co.kr.user.dao.UserRepository;
import co.kr.user.dao.UserVerificationsRepository;
import co.kr.user.model.dto.mail.EmailMessage;
import co.kr.user.model.dto.register.RegisterDTO;
import co.kr.user.model.dto.register.RegisterReq;
import co.kr.user.model.entity.Users;
import co.kr.user.model.entity.UsersInformation;
import co.kr.user.model.entity.UsersVerifications;
import co.kr.user.model.vo.UsersVerificationsPurPose;
import co.kr.user.model.vo.UsersVerificationsStatus;
import co.kr.user.service.RegisterService;
import co.kr.user.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;

/**
 * 회원가입(Sign Up) 관련 비즈니스 로직을 처리하는 서비스 클래스입니다.
 * 이메일 중복 확인, 신규 계정 생성(암호화 포함), 가입 인증 메일 발송 및 확인 기능을 제공합니다.
 */
@Service
@RequiredArgsConstructor
public class RegisterServiceImpl implements RegisterService {
    private final UserRepository userRepository;
    private final UserInformationRepository userInformationRepository;
    private final UserVerificationsRepository userVerificationsRepository;

    private final BCryptUtil bCryptUtil; // 비밀번호 해싱(단방향 암호화)
    private final AesUtil aesUtil; // 개인정보 양방향 암호화
    private final RandomCodeUtil randomCodeUtil; // 인증코드 생성
    private final MailUtil mailUtil; // 이메일 발송

    /**
     * 이메일(아이디) 중복 확인 메서드입니다.
     *
     * @param email 확인할 이메일
     * @return 중복 여부 메시지
     */
    @Transactional(readOnly = true)
    public String checkDuplicate(String email) {
        boolean isDuplicate = userRepository.existsByID(email);

        if (isDuplicate) {
            return "이메일 사용이 불가능합니다";
        }

        return "이메일 사용이 가능합니다.";
    }

    /**
     * 회원가입 신청 메서드입니다.
     * 사용자 정보와 상세 정보를 DB에 저장하고, 이메일 인증 코드를 발송합니다.
     * 계정은 초기에 미인증 상태(Del=2)로 생성됩니다.
     *
     * @param registerReq 회원가입 요청 정보
     * @return RegisterDTO (가입 처리 결과 및 인증 시간)
     */
    @Transactional
    public RegisterDTO signup(RegisterReq registerReq) {
        // 중복 가입 방지
        boolean isDuplicate = userRepository.existsByID(registerReq.getID());
        if (isDuplicate) {
            throw new IllegalStateException("이미 가입된 이메일입니다.");
        }

        // 필수 약관 동의 체크
        if (registerReq.getAgreeTermsAt() == null) {
            throw new IllegalStateException("이용약관 동의는 필수입니다.");
        }
        if (registerReq.getAgreePrivateAt() == null) {
            throw new IllegalStateException("개인정보 처리방침 동의는 필수입니다.");
        }

        // 비밀번호 해싱 및 개인정보 암호화
        registerReq.setPW(bCryptUtil.encode(registerReq.getPW()));
        registerReq.setName(aesUtil.encrypt(registerReq.getName()));
        registerReq.setPhoneNumber(aesUtil.encrypt(registerReq.getPhoneNumber()));
        registerReq.setBirth(aesUtil.encrypt(registerReq.getBirth()));

        // Users 엔티티 생성 및 저장 (기본 상태: 미인증)
        Users user = Users.builder()
                .ID(registerReq.getID())
                .PW(registerReq.getPW())
                .agreeTermsAt(LocalDateTime.now())
                .agreePrivacyAt(LocalDateTime.now())
                .agreeMarketingAt(registerReq.getAgreeMarketingAt())
                .build();

        Users savedUser = userRepository.save(user);

        // UsersInformation 엔티티 생성 및 저장
        UsersInformation usersInformation = UsersInformation.builder()
                .usersIdx(savedUser.getUsersIdx())
                .name(registerReq.getName())
                .phoneNumber(registerReq.getPhoneNumber())
                .birth(registerReq.getBirth())
                .build();

        userInformationRepository.save(usersInformation);

        // 인증 코드 생성 및 저장 (목적: SIGNUP)
        UsersVerifications usersVerifications = co.kr.user.model.entity.UsersVerifications.builder()
                .usersIdx(savedUser.getUsersIdx())
                .purPose(UsersVerificationsPurPose.SIGNUP)
                .code(randomCodeUtil.getCode())
                .expiresAt(LocalDateTime.now().plusMinutes(30))
                .status(UsersVerificationsStatus.PENDING)
                .build();

        UsersVerifications savedUserVerifications = userVerificationsRepository.save(usersVerifications);

        // 인증 메일 발송 로직
        String htmlTemplate = """
        <div style='background-color: #f6f7f9; padding: 40px 20px; font-family: "Apple SD Gothic Neo", "Malgun Gothic", sans-serif; line-height: 1.6;'>
            <div style='max-width: 500px; margin: 0 auto; background-color: #ffffff; border-radius: 8px; border: 1px solid #e0e0e0; overflow: hidden; box-shadow: 0 4px 12px rgba(0,0,0,0.05);'>
                
                <div style='background-color: #007bff; padding: 20px; text-align: center;'>
                    <h1 style='color: #ffffff; font-size: 20px; margin: 0; font-weight: 600;'>GutJJeu</h1>
                </div>
        
                <div style='padding: 30px;'>
                    <h2 style='color: #333; font-size: 22px; margin-top: 0; text-align: center;'>이메일 인증 안내</h2>
                    <p style='color: #555; font-size: 16px; margin-bottom: 20px; text-align: center;'>
                        안녕하세요.<br>
                        서비스 이용을 위해 아래 인증번호를 입력해 주세요.
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

        String finalContent = htmlTemplate.formatted(savedUserVerifications.getCode());

        EmailMessage emailMessage = EmailMessage.builder()
                .to(savedUser.getID())
                .subject("[GutJJeu] 회원가입 인증번호 안내해 드립니다.")
                .message(finalContent)
                .build();

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                mailUtil.sendEmail(emailMessage, true);
            }
        });

        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setID(savedUser.getID());
        registerDTO.setCertificationTime(savedUserVerifications.getExpiresAt());

        return registerDTO;
    }

    /**
     * 가입 인증 확인 메서드입니다.
     * 인증 코드가 유효하면 계정을 활성화(Del=0)합니다.
     *
     * @param code 인증 코드
     * @return 인증 결과 메시지
     */
    @Transactional
    public String signupAuthentication(String code) {
        // 코드 조회
        UsersVerifications usersVerifications = userVerificationsRepository.findTopByCodeAndDelOrderByCreatedAtDesc(code, 0)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 인증 코드입니다."));

        // 유효성 검증 (만료, 목적, 이미 인증됨 등)
        if (usersVerifications.getExpiresAt().isBefore(LocalDateTime.now())) {
            return "인증 시간이 만료되었습니다.";
        }

        if (usersVerifications.getPurPose() != UsersVerificationsPurPose.SIGNUP) {
            throw new IllegalArgumentException("회원가입 인증 코드가 아닙니다.");
        }

        if (usersVerifications.getStatus() == UsersVerificationsStatus.VERIFIED) {
            return "이미 인증 완료된 코드입니다.";
        }

        // 인증 완료 처리
        usersVerifications.confirmVerification();

        // 사용자 계정 활성화 (Del: 2 -> 0)
        Users user = userRepository.findById(usersVerifications.getUsersIdx())
                .orElseThrow(() -> new IllegalArgumentException("가입된 회원 정보를 찾을 수 없습니다."));

        user.confirmVerification();

        return "인증 완료";
    }
}