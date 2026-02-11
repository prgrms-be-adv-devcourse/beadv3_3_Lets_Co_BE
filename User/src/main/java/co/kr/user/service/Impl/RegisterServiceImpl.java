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
import co.kr.user.service.UserQueryService;
import co.kr.user.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RegisterServiceImpl implements RegisterService {
    private final UserRepository userRepository;
    private final UserInformationRepository userInformationRepository;
    private final UserVerificationsRepository userVerificationsRepository;

    private final UserQueryService userQueryService;

    private final BCryptUtil bCryptUtil;
    private final RandomCodeUtil randomCodeUtil;
    private final MailUtil mailUtil;
    private final EmailTemplateProvider emailTemplateProvider;

    public String checkDuplicate(String id) {
        if (userQueryService.existsActiveId(id)) {
            return "ID 사용이 불가능합니다";
        }
        return "ID 사용이 가능합니다.";
    }

    @Transactional
    public RegisterDTO signup(RegisterReq registerReq) {
        if (userQueryService.existsActiveId(registerReq.getId())) {
            throw new IllegalStateException("이미 존재하는 ID입니다.");
        }

        registerReq.setPw(bCryptUtil.encode(registerReq.getPw()));

        Users user = Users.builder()
                .id(registerReq.getId())
                .pw(registerReq.getPw())
                .build();
        Users savedUser = userRepository.save(user);

        UsersInformation usersInformation = UsersInformation.builder()
                .usersIdx(savedUser.getUsersIdx())
                .mail(registerReq.getMail())
                .gender(registerReq.getGender())
                .name(registerReq.getName())
                .phoneNumber(registerReq.getPhoneNumber())
                .birth(registerReq.getBirth())
                .agreeMarketingAt(registerReq.getAgreeMarketingAt())
                .build();
        userInformationRepository.save(usersInformation);

        UsersVerifications usersVerifications = UsersVerifications.builder()
                .usersIdx(savedUser.getUsersIdx())
                .purpose(UsersVerificationsPurPose.SIGNUP)
                .code(randomCodeUtil.getCode())
                .expiresAt(LocalDateTime.now().plusMinutes(30))
                .status(UsersVerificationsStatus.PENDING)
                .build();
        userVerificationsRepository.save(usersVerifications);

        EmailMessage emailMessage = EmailMessage.builder()
                .to(usersInformation.getMail())
                .subject("[GutJJeu] 회원가입 인증번호 안내해 드립니다.")
                .message(emailTemplateProvider.getSignupTemplate(usersVerifications.getCode()))
                .build();

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() { mailUtil.sendEmail(emailMessage, true); }
        });

        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setMail(usersInformation.getMail());
        registerDTO.setCertificationTime(usersVerifications.getExpiresAt());
        return registerDTO;
    }

    @Transactional
    public String signupAuthentication(String code) {
        UsersVerifications usersVerifications = userVerificationsRepository.findTopByCodeAndDelOrderByCreatedAtDesc(code, 0)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 인증 코드입니다."));

        if (usersVerifications.getExpiresAt().isBefore(LocalDateTime.now())) {
            return "인증 시간이 만료되었습니다.";
        } else if (usersVerifications.getPurpose() != UsersVerificationsPurPose.SIGNUP) {
            throw new IllegalArgumentException("회원가입 인증 코드가 아닙니다.");
        } else if (usersVerifications.getStatus() == UsersVerificationsStatus.VERIFIED) {
            return "이미 인증 완료된 코드입니다.";
        }

        usersVerifications.confirmVerification();

        Users user = userQueryService.findWaitUser(usersVerifications.getUsersIdx());
        UsersInformation userInfo = userQueryService.findWaitUserInfo(usersVerifications.getUsersIdx());

        user.activateUsers();
        userInfo.activateInformation();

        return "인증 완료";
    }
}