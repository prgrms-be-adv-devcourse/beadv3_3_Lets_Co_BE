package co.kr.user.service.Impl;

import co.kr.user.dao.UserInformationRepository;
import co.kr.user.dao.UserVerificationsRepository;
import co.kr.user.model.dto.mail.EmailMessage;
import co.kr.user.model.dto.retrieve.*;
import co.kr.user.model.entity.*;
import co.kr.user.model.vo.UsersVerificationsPurPose;
import co.kr.user.model.vo.UsersVerificationsStatus;
import co.kr.user.service.RetrieveService;
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
public class RetrieveServiceImpl implements RetrieveService {
    private final UserVerificationsRepository userVerificationsRepository;
    private final UserInformationRepository userInformationRepository;

    private final UserQueryService userQueryService;

    private final RandomCodeUtil randomCodeUtil;
    private final MailUtil mailUtil;
    private final BCryptUtil bCryptUtil;
    private final EmailTemplateProvider emailTemplateProvider;

    @Override
    @Transactional
    public FindIDFirstStepDTO findIdFirst(String mail) {
        UsersInformation info = userInformationRepository.findByMailAndDel(mail, 0)
                .orElseThrow(() -> new IllegalArgumentException("해당 이메일로 가입된 정보가 없습니다."));

        userQueryService.findActiveUser(info.getUsersIdx());

        UsersVerifications verification = UsersVerifications.builder()
                .usersIdx(info.getUsersIdx())
                .purpose(UsersVerificationsPurPose.FIND_ID)
                .code(randomCodeUtil.getCode())
                .expiresAt(LocalDateTime.now().plusMinutes(30))
                .status(UsersVerificationsStatus.PENDING)
                .build();

        userVerificationsRepository.save(verification);
        sendFindEmailAfterCommit(info.getMail(), verification.getCode());

        FindIDFirstStepDTO response = new FindIDFirstStepDTO();
        response.setMail(info.getMail());
        response.setCertificationTime(verification.getExpiresAt());
        return response;
    }

    @Override
    public String findIdSecond(FindIDSecondStepReq req) {
        UsersInformation info = userInformationRepository.findByMailAndDel(req.getMail(), 0)
                .orElseThrow(() -> new IllegalArgumentException("해당 이메일 정보가 없습니다."));

        Users user = userQueryService.findActiveUser(info.getUsersIdx());

        UsersVerifications verification = userVerificationsRepository
                .findTopByUsersIdxAndDelOrderByCreatedAtDesc(info.getUsersIdx(), 0)
                .orElseThrow(() -> new IllegalArgumentException("인증 내역이 없습니다."));

        validateVerification(verification, req.getAuthCode(), UsersVerificationsPurPose.FIND_ID);

        return user.getId();
    }

    @Override
    @Transactional
    public FindPWFirstStepDTO findPwFirst(String mail) {
        UsersInformation info = userInformationRepository.findByMailAndDel(mail, 0)
                .orElseThrow(() -> new IllegalArgumentException("해당 이메일로 가입된 정보가 없습니다."));

        userQueryService.findActiveUser(info.getUsersIdx());

        UsersVerifications verification = UsersVerifications.builder()
                .usersIdx(info.getUsersIdx())
                .purpose(UsersVerificationsPurPose.RESET_PW)
                .code(randomCodeUtil.getCode())
                .expiresAt(LocalDateTime.now().plusMinutes(30))
                .status(UsersVerificationsStatus.PENDING)
                .build();

        userVerificationsRepository.save(verification);
        sendResetEmailAfterCommit(info.getMail(), verification.getCode());

        FindPWFirstStepDTO response = new FindPWFirstStepDTO();
        response.setMail(info.getMail());
        response.setCertificationTime(verification.getExpiresAt());
        return response;
    }

    @Override
    @Transactional
    public String findPwSecond(FindPWSecondStepReq req) {
        UsersInformation info = userInformationRepository.findByMailAndDel(req.getMail(), 0)
                .orElseThrow(() -> new IllegalArgumentException("해당 이메일 정보가 없습니다."));

        Users user = userQueryService.findActiveUser(info.getUsersIdx());

        UsersVerifications verification = userVerificationsRepository
                .findTopByUsersIdxAndDelOrderByCreatedAtDesc(info.getUsersIdx(), 0)
                .orElseThrow(() -> new IllegalArgumentException("인증 내역이 없습니다."));

        validateVerification(verification, req.getAuthCode(), UsersVerificationsPurPose.RESET_PW);

        if (!req.getNewPW().equals(req.getNewPWCheck())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        info.updatePrePW(user.getPw());
        user.changePassword(bCryptUtil.encode(req.getNewPW()));
        verification.confirmVerification();

        return "비밀번호 재설정이 정상 처리되었습니다.";
    }

    private void validateVerification(UsersVerifications v, String code, UsersVerificationsPurPose purpose) {
        if (v.getStatus() == UsersVerificationsStatus.VERIFIED) throw new IllegalStateException("이미 완료된 인증입니다.");
        if (v.getPurpose() != purpose) throw new IllegalArgumentException("잘못된 접근입니다.");
        if (v.getExpiresAt().isBefore(LocalDateTime.now())) throw new IllegalStateException("만료된 인증번호입니다.");
        if (!v.getCode().equals(code)) throw new IllegalArgumentException("인증번호가 일치하지 않습니다.");
    }

    private void sendFindEmailAfterCommit(String email, String code) {
        EmailMessage message = EmailMessage.builder()
                .to(email)
                .subject("[GutJJeu] 아이디 찾기 인증번호 안내")
                .message(emailTemplateProvider.getFindIDTemplate(code))
                .build();
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() { mailUtil.sendEmail(message, true); }
        });
    }

    private void sendResetEmailAfterCommit(String email, String code) {
        EmailMessage message = EmailMessage.builder()
                .to(email)
                .subject("[GutJJeu] 비밀번호 재설정 인증번호 안내")
                .message(emailTemplateProvider.getResetPasswordTemplate(code))
                .build();
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() { mailUtil.sendEmail(message, true); }
        });
    }
}