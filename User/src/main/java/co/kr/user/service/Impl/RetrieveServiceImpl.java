package co.kr.user.service.Impl;

import co.kr.user.dao.UserInformationRepository;
import co.kr.user.dao.UserVerificationsRepository;
import co.kr.user.model.dto.mail.EmailMessage;
import co.kr.user.model.dto.retrieve.*;
import co.kr.user.model.entity.*;
import co.kr.user.model.vo.UserDel;
import co.kr.user.model.vo.UsersVerificationsPurPose;
import co.kr.user.model.vo.UsersVerificationsStatus;
import co.kr.user.service.RetrieveService;
import co.kr.user.service.UserQueryService;
import co.kr.user.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RetrieveServiceImpl implements RetrieveService {
    private final UserVerificationsRepository userVerificationsRepository;
    private final UserInformationRepository userInformationRepository;
    private final UserQueryService userQueryService;
    private final RandomCodeUtil randomCodeUtil;
    private final MailUtil mailUtil;
    private final BCryptUtil bCryptUtil;
    private final EmailTemplateProvider emailTemplateProvider;

    @Value("${custom.security.verification.expiration-minutes}")
    private long expirationMinutes;

    @Value("${custom.mail.subject.find-id}")
    private String findIdSubject;

    @Value("${custom.mail.subject.reset-pw}")
    private String resetPwSubject;

    @Override
    @Transactional
    public FindIDFirstStepDTO findIdFirst(String mail) {
        UsersInformation info = userInformationRepository.findByMailAndDel(mail, UserDel.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("해당 이메일로 가입된 정보가 없습니다."));

        userQueryService.findActiveUser(info.getUsersIdx());

        UsersVerifications verification = UsersVerifications.builder()
                .usersIdx(info.getUsersIdx())
                .purpose(UsersVerificationsPurPose.FIND_ID)
                .code(randomCodeUtil.getCode())
                .expiresAt(LocalDateTime.now().plusMinutes(expirationMinutes))
                .status(UsersVerificationsStatus.PENDING)
                .build();

        userVerificationsRepository.save(verification);
        sendEmailAfterCommit(info.getMail(), findIdSubject, emailTemplateProvider.getFindIDTemplate(verification.getCode()));

        FindIDFirstStepDTO response = new FindIDFirstStepDTO();
        response.setMail(info.getMail());
        response.setCertificationTime(verification.getExpiresAt());
        return response;
    }

    @Override
    public String findIdSecond(FindIDSecondStepReq req) {
        UsersInformation info = userInformationRepository.findByMailAndDel(req.getMail(), UserDel.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("해당 이메일 정보가 없습니다."));

        Users user = userQueryService.findActiveUser(info.getUsersIdx());

        UsersVerifications verification = userVerificationsRepository
                .findTopByUsersIdxAndDelOrderByCreatedAtDesc(info.getUsersIdx(), UserDel.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("인증 내역이 없습니다."));

        validateVerification(verification, req.getAuthCode(), UsersVerificationsPurPose.FIND_ID);

        return user.getId();
    }

    @Override
    @Transactional
    public FindPWFirstStepDTO findPwFirst(String mail) {
        UsersInformation info = userInformationRepository.findByMailAndDel(mail, UserDel.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("해당 이메일로 가입된 정보가 없습니다."));

        userQueryService.findActiveUser(info.getUsersIdx());

        UsersVerifications verification = UsersVerifications.builder()
                .usersIdx(info.getUsersIdx())
                .purpose(UsersVerificationsPurPose.RESET_PW)
                .code(randomCodeUtil.getCode())
                .expiresAt(LocalDateTime.now().plusMinutes(expirationMinutes))
                .status(UsersVerificationsStatus.PENDING)
                .build();

        userVerificationsRepository.save(verification);
        sendEmailAfterCommit(info.getMail(), resetPwSubject, emailTemplateProvider.getResetPasswordTemplate(verification.getCode()));

        FindPWFirstStepDTO response = new FindPWFirstStepDTO();
        response.setMail(info.getMail());
        response.setCertificationTime(verification.getExpiresAt());
        return response;
    }

    @Override
    @Transactional
    public String findPwSecond(FindPWSecondStepReq req) {
        UsersInformation info = userInformationRepository.findByMailAndDel(req.getMail(), UserDel.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("해당 이메일 정보가 없습니다."));

        Users user = userQueryService.findActiveUser(info.getUsersIdx());

        UsersVerifications verification = userVerificationsRepository
                .findTopByUsersIdxAndDelOrderByCreatedAtDesc(info.getUsersIdx(), UserDel.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("인증 내역이 없습니다."));

        validateVerification(verification, req.getAuthCode(), UsersVerificationsPurPose.RESET_PW);

        if (!req.getNewPW().equals(req.getNewPWCheck())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        info.validateNewPassword(req.getNewPW(), bCryptUtil::check);

        String oldPw = user.getPw();
        user.changePassword(bCryptUtil.encode(req.getNewPW()), req.getNewPW(), bCryptUtil::check);

        info.updatePrePW(oldPw);
        verification.confirmVerification();

        return "비밀번호 재설정이 정상 처리되었습니다.";
    }

    private void validateVerification(UsersVerifications v, String code, UsersVerificationsPurPose purpose) {
        if (v.getStatus() == UsersVerificationsStatus.VERIFIED) throw new IllegalStateException("이미 완료된 인증입니다.");
        if (v.getPurpose() != purpose) throw new IllegalArgumentException("잘못된 접근입니다.");
        if (v.getExpiresAt().isBefore(LocalDateTime.now())) throw new IllegalStateException("만료된 인증번호입니다.");
        if (!v.getCode().equals(code)) throw new IllegalArgumentException("인증번호가 일치하지 않습니다.");
    }

    private void sendEmailAfterCommit(String email, String subject, String content) {
        EmailMessage message = EmailMessage.builder()
                .to(email)
                .subject(subject)
                .message(content)
                .build();
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() { mailUtil.sendEmail(message, true); }
        });
    }
}