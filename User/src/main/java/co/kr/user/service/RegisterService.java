package co.kr.user.service;

import co.kr.user.DAO.UserInformationRepository;
import co.kr.user.DAO.UserRepository;
import co.kr.user.DAO.UserVerificationsRepository;
import co.kr.user.model.DTO.mail.EmailMessage;
import co.kr.user.model.DTO.register.RegisterDTO;
import co.kr.user.model.DTO.register.RegisterReq;
import co.kr.user.model.entity.Users;
import co.kr.user.model.entity.UsersInformation;
import co.kr.user.model.entity.UsersVerifications;
import co.kr.user.model.vo.UsersVerificationsPurPose;
import co.kr.user.model.vo.UsersVerificationsStatus;
import co.kr.user.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@RequiredArgsConstructor
public class RegisterService implements RegisterServiceImpl{
    private final UserRepository userRepository;
    private final UserInformationRepository userInformationRepository;
    private final UserVerificationsRepository userVerificationsRepository;

    private final BCryptUtil bCryptUtil;
    private final AESUtil aesUtil;
    private final RandomCodeUtil randomCodeUtil;
    private final EMailUtil eMailUtil;

    @Transactional(readOnly = true)
    public String checkDuplicate(String email) {
        boolean isDuplicate = userRepository.existsByID(email);

        if (isDuplicate) {
            return "이메일 사용이 불가능합니다";
        }

        return "이메일 사용이 가능합니다.";
    }

    @Transactional
    public RegisterDTO signup(RegisterReq registerReq) {
        boolean isDuplicate = userRepository.existsByID(registerReq.getID());

        if (isDuplicate) {
            throw new IllegalStateException("이미 가입된 이메일입니다.");
        }

        if (registerReq.getAgreeTermsAt() == null) {
            throw new IllegalStateException("이용약관 동의는 필수입니다.");
        }
        if (registerReq.getAgreePrivateAt() == null) {
            throw new IllegalStateException("개인정보 처리방침 동의는 필수입니다.");
        }

        registerReq.setPW(bCryptUtil.encode(registerReq.getPW()));
        registerReq.setName(aesUtil.encrypt(registerReq.getName()));
        registerReq.setPhoneNumber(aesUtil.encrypt(registerReq.getPhoneNumber()));
        registerReq.setBirth(aesUtil.encrypt(registerReq.getBirth()));

        Users user = Users.builder()
                .ID(registerReq.getID())
                .PW(registerReq.getPW())
                .agreeTermsAt(LocalDateTime.now())
                .agreePrivacyAt(LocalDateTime.now())
                .agreeMarketingAt(registerReq.getAgreeMarketingAt())
                .build();

        Users savedUser = userRepository.save(user);

        UsersInformation usersInformation = UsersInformation.builder()
                .usersIdx(savedUser.getUsersIdx())
                .name(registerReq.getName())
                .phoneNumber(registerReq.getPhoneNumber())
                .birth(registerReq.getBirth())
                .build();

        userInformationRepository.save(usersInformation);

        UsersVerifications usersVerifications = co.kr.user.model.entity.UsersVerifications.builder()
                .usersIdx(savedUser.getUsersIdx())
                .purPose(UsersVerificationsPurPose.SIGNUP)
                .code(randomCodeUtil.getCode())
                .expiresAt(LocalDateTime.now().plusMinutes(30))
                .status(UsersVerificationsStatus.PENDING)
                .build();

        UsersVerifications savedUserVerifications = userVerificationsRepository.save(usersVerifications);

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
                eMailUtil.sendEmail(emailMessage, true);
            }
        });

        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setID(savedUser.getID());
        registerDTO.setCertificationTime(savedUserVerifications.getExpiresAt());

        return registerDTO;
    }

    @Transactional
    public String signupAuthentication(String code) {
        UsersVerifications usersVerifications = userVerificationsRepository.findTopByCodeOrderByCreatedAtDesc(code)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 인증 코드입니다."));

        if (usersVerifications.getExpiresAt().isBefore(LocalDateTime.now())) {
            return "인증 시간이 만료되었습니다.";
        }

        if (usersVerifications.getPurPose() != UsersVerificationsPurPose.SIGNUP) {
            throw new IllegalArgumentException("회원가입 인증 코드가 아닙니다.");
        }

        if (usersVerifications.getStatus() == UsersVerificationsStatus.VERIFIED) {
            return "이미 인증 완료된 코드입니다.";
        }

        usersVerifications.confirmVerification();

        Users user = userRepository.findById(usersVerifications.getUsersIdx())
                .orElseThrow(() -> new IllegalArgumentException("가입된 회원 정보를 찾을 수 없습니다."));

        user.confirmVerification();

        return "인증 완료";
    }
}