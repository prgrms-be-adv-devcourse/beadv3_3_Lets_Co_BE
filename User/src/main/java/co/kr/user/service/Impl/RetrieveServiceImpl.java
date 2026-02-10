package co.kr.user.service.Impl;

import co.kr.user.dao.UserInformationRepository;
import co.kr.user.dao.UserVerificationsRepository;
import co.kr.user.model.dto.mail.EmailMessage;
import co.kr.user.model.dto.retrieve.FindIDFirstStepDTO;
import co.kr.user.model.dto.retrieve.FindIDSecondStepReq;
import co.kr.user.model.dto.retrieve.FindPWFirstStepDTO;
import co.kr.user.model.dto.retrieve.FindPWSecondStepReq;
import co.kr.user.model.entity.Users;
import co.kr.user.model.entity.UsersInformation;
import co.kr.user.model.entity.UsersVerifications;
import co.kr.user.model.vo.UsersVerificationsPurPose;
import co.kr.user.model.vo.UsersVerificationsStatus;
import co.kr.user.service.RetrieveService;
import co.kr.user.util.BCryptUtil;
import co.kr.user.util.EmailTemplateProvider;
import co.kr.user.util.MailUtil;
import co.kr.user.util.RandomCodeUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RetrieveServiceImpl implements RetrieveService {
    private final UserVerificationsRepository userVerificationsRepository;
    private final UserInformationRepository userInformationRepository;
    private final UserQueryServiceImpl userQueryServiceImpl;
    private final RandomCodeUtil randomCodeUtil;
    private final MailUtil mailUtil;
    private final BCryptUtil bCryptUtil;
    private final EmailTemplateProvider emailTemplateProvider;

    @Override
    public FindIDFirstStepDTO findIdFirst(String mail) {
        UsersInformation info = userInformationRepository.findByMailAndDel(mail, 0)
                .orElseThrow(() -> new IllegalArgumentException("사용자 상세 정보를 찾을 수 없습니다."));
        Users user = userQueryServiceImpl.findActiveUser(info.getUsersIdx());

        UsersVerifications verification = UsersVerifications.builder()
                .usersIdx(user.getUsersIdx())
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

        Users user = userQueryServiceImpl.findActiveUser(info.getUsersIdx());

        UsersVerifications verification = userVerificationsRepository
                .findTopByUsersIdxAndDelOrderByCreatedAtDesc(info.getUsersIdx(), 0)
                .orElseThrow(() -> new IllegalArgumentException("인증 내역이 없습니다."));

        validateIDVerification(verification, req.getAuthCode());

        return user.getId();
    }

    @Override
    @Transactional
    public FindPWFirstStepDTO findPwFirst(String mail) {
        UsersInformation info = userInformationRepository.findByMailAndDel(mail, 0)
                .orElseThrow(() -> new IllegalArgumentException("사용자 상세 정보를 찾을 수 없습니다."));
        Users user = userQueryServiceImpl.findActiveUser(info.getUsersIdx());

        UsersVerifications verification = UsersVerifications.builder()
                .usersIdx(user.getUsersIdx())
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

        Users user = userQueryServiceImpl.findActiveUser(info.getUsersIdx());

        UsersVerifications verification = userVerificationsRepository
                .findTopByUsersIdxAndDelOrderByCreatedAtDesc(info.getUsersIdx(), 0)
                .orElseThrow(() -> new IllegalArgumentException("인증 내역이 없습니다."));

        validatePWVerification(verification, req.getAuthCode());

        Optional.of(req)
                .filter(r -> r.getNewPW().equals(r.getNewPWCheck()))
                .orElseThrow(() -> new IllegalArgumentException("비밀번호가 일치하지 않습니다."));

        info.updatePrePW(user.getPw());
        user.changePassword(bCryptUtil.encode(req.getNewPW()));
        verification.confirmVerification();

        return "비밀번호 재설정이 정상 처리되었습니다.";
    }

    private void validateIDVerification(UsersVerifications v, String code) {
        Optional.of(v).filter(ver -> ver.getStatus() != UsersVerificationsStatus.VERIFIED)
                .orElseThrow(() -> new IllegalStateException("이미 완료된 인증입니다."));

        Optional.of(v).filter(ver -> ver.getPurpose() == UsersVerificationsPurPose.FIND_ID)
                .orElseThrow(() -> new IllegalArgumentException("잘못된 접근입니다."));

        Optional.of(v).filter(ver -> ver.getExpiresAt().isAfter(LocalDateTime.now()))
                .orElseThrow(() -> new IllegalStateException("만료된 인증번호입니다."));

        Optional.of(v).filter(ver -> ver.getCode().equals(code))
                .orElseThrow(() -> new IllegalArgumentException("인증번호가 일치하지 않습니다."));
    }

    private void validatePWVerification(UsersVerifications v, String code) {
        Optional.of(v).filter(ver -> ver.getStatus() != UsersVerificationsStatus.VERIFIED)
                .orElseThrow(() -> new IllegalStateException("이미 완료된 인증입니다."));

        Optional.of(v).filter(ver -> ver.getPurpose() == UsersVerificationsPurPose.RESET_PW)
                .orElseThrow(() -> new IllegalArgumentException("잘못된 접근입니다."));

        Optional.of(v).filter(ver -> ver.getExpiresAt().isAfter(LocalDateTime.now()))
                .orElseThrow(() -> new IllegalStateException("만료된 인증번호입니다."));

        Optional.of(v).filter(ver -> ver.getCode().equals(code))
                .orElseThrow(() -> new IllegalArgumentException("인증번호가 일치하지 않습니다."));
    }

    private void sendFindEmailAfterCommit(String email, String code) {
        EmailMessage message = EmailMessage.builder()
                .to(email)
                .subject("[GutJJeu] 아이디 찾기 인증번호 안내")
                .message(emailTemplateProvider.getFindIDTemplate(code))
                .build();

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                mailUtil.sendEmail(message, true);
            }
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
            public void afterCommit() {
                mailUtil.sendEmail(message, true);
            }
        });
    }
}