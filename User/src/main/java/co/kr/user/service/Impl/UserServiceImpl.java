package co.kr.user.service.Impl;

import co.kr.user.dao.UserVerificationsRepository;
import co.kr.user.model.dto.mail.EmailMessage;
import co.kr.user.model.dto.my.UserAmendReq;
import co.kr.user.model.dto.my.UserDeleteDTO;
import co.kr.user.model.dto.my.UserProfileDTO;
import co.kr.user.model.dto.my.UserDTO;
import co.kr.user.model.entity.Users;
import co.kr.user.model.entity.UsersInformation;
import co.kr.user.model.entity.UsersVerifications;
import co.kr.user.model.vo.UserDel;
import co.kr.user.model.vo.UsersVerificationsPurPose;
import co.kr.user.model.vo.UsersVerificationsStatus;
import co.kr.user.service.UserService;
import co.kr.user.service.UserQueryService;
import co.kr.user.util.CookieUtil;
import co.kr.user.util.EmailTemplateProvider;
import co.kr.user.util.MailUtil;
import co.kr.user.util.RandomCodeUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.util.regex.Pattern;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private final UserVerificationsRepository userVerificationsRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final UserQueryService userQueryService;
    private final RandomCodeUtil randomCodeUtil;
    private final MailUtil mailUtil;
    private final EmailTemplateProvider emailTemplateProvider;

    @Value("${custom.security.verification.expiration-minutes}")
    private long expirationMinutes;

    @Value("${custom.mail.subject.delete-account}")
    private String deleteAccountSubject;

    @Value("${custom.security.redis.rt-prefix}")
    private String rtPrefix;

    @Value("${custom.security.redis.bl-prefix}")
    private String blPrefix;

    @Override
    public BigDecimal balance(Long userIdx) {
        return userQueryService.findActiveUserInfo(userIdx).getBalance();
    }

    @Override
    public UserDTO my(Long userIdx) {
        Users users = userQueryService.findActiveUser(userIdx);
        UsersInformation userInfo = userQueryService.findActiveUserInfo(userIdx);

        UserDTO userDTO = new UserDTO();
        userDTO.setId(users.getId());
        userDTO.setRole(users.getRole());
        userDTO.setMembership(users.getMembership());
        userDTO.setCreatedAt(users.getCreatedAt());
        userDTO.setBalance(userInfo.getBalance());
        return userDTO;
    }

    @Override
    public UserProfileDTO myDetails(Long userIdx) {
        UsersInformation userInfo = userQueryService.findActiveUserInfo(userIdx);

        UserProfileDTO dto = new UserProfileDTO();
        dto.setName(userInfo.getName());
        dto.setPhoneNumber(userInfo.getPhoneNumber());
        dto.setBirth(userInfo.getBirth());
        dto.setGender(userInfo.getGender());
        dto.setMail(userInfo.getMail());
        dto.setAgreeMarketingAt(userInfo.getAgreeMarketingAt());
        return dto;
    }

    @Override
    @Transactional
    public UserDeleteDTO myDelete(Long userIdx) {
        userQueryService.findActiveUser(userIdx);
        UsersInformation userInfo = userQueryService.findActiveUserInfo(userIdx);

        UsersVerifications verification = UsersVerifications.builder()
                .usersIdx(userIdx)
                .purpose(UsersVerificationsPurPose.DELETE_ACCOUNT)
                .code(randomCodeUtil.getCode())
                .expiresAt(LocalDateTime.now().plusMinutes(expirationMinutes))
                .status(UsersVerificationsStatus.PENDING)
                .build();

        userVerificationsRepository.save(verification);

        EmailMessage emailMessage = EmailMessage.builder()
                .to(userInfo.getMail())
                .subject(deleteAccountSubject)
                .message(emailTemplateProvider.getDeleteAccountTemplate(verification.getCode()))
                .build();

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() { mailUtil.sendEmail(emailMessage, true); }
        });

        UserDeleteDTO dto = new UserDeleteDTO();
        dto.setMail(userInfo.getMail());
        dto.setCertificationTime(LocalDateTime.now());
        return dto;
    }

    @Override
    @Transactional
    public String myDelete(Long userIdx, String authCode, HttpServletResponse response) {
        Users users = userQueryService.findActiveUser(userIdx);
        UsersInformation userInfo = userQueryService.findActiveUserInfo(userIdx);

        UsersVerifications verification = userVerificationsRepository.findTopByUsersIdxAndDelOrderByCreatedAtDesc(userIdx, UserDel.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("인증 요청 내역이 존재하지 않습니다."));

        if (verification.getPurpose() != UsersVerificationsPurPose.DELETE_ACCOUNT) {
            throw new IllegalArgumentException("인증 코드가 일치하지 않습니다.");
        } else if (verification.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("인증 시간이 만료되었습니다.");
        } else if (!verification.getCode().equals(authCode)) {
            throw new IllegalArgumentException("인증번호가 일치하지 않습니다.");
        }

        verification.confirmVerification();
        users.deleteUsers(users.getId() + "_DEL_" + LocalDateTime.now());
        userInfo.deleteInformation(userInfo.getMail() + "_DEL_" + LocalDateTime.now());

        String rtKey = rtPrefix + userIdx;
        String refreshToken = (String) redisTemplate.opsForValue().get(rtKey);
        if (refreshToken != null) {
            redisTemplate.delete(rtKey);
            redisTemplate.opsForValue().set(blPrefix + refreshToken, "DELETED_BY_USER");
        }

        CookieUtil.deleteCookie(response, CookieUtil.ACCESS_TOKEN_NAME);
        CookieUtil.deleteCookie(response, CookieUtil.REFRESH_TOKEN_NAME);

        return "회원 탈퇴가 정상 처리되었습니다.";
    }

    @Override
    @Transactional
    public UserAmendReq myAmend(Long userIdx, UserAmendReq req) {
        userQueryService.findActiveUser(userIdx);
        UsersInformation userInfo = userQueryService.findActiveUserInfo(userIdx);

        if (req.getMail() != null && !Pattern.matches("^[A-Za-z0-9+_.-]+@(.+)$", req.getMail())) {
            throw new IllegalArgumentException("유효하지 않은 이메일 형식입니다.");
        }

        userInfo.updateProfile(req.getMail(), req.getGender(), req.getName(), req.getPhoneNumber(), req.getBirth());

        return req;
    }
}