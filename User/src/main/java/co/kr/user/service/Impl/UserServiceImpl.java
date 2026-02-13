package co.kr.user.service.Impl;

import co.kr.user.dao.UserInformationRepository;
import co.kr.user.dao.UserVerificationsRepository;
import co.kr.user.model.dto.mail.EmailMessage;
import co.kr.user.model.dto.my.UserAmendReq;
import co.kr.user.model.dto.my.UserDeleteDTO;
import co.kr.user.model.dto.my.UserProfileDTO;
import co.kr.user.model.dto.my.UserDTO;
import co.kr.user.model.entity.Users;
import co.kr.user.model.entity.UsersInformation;
import co.kr.user.model.entity.UsersVerifications;
import co.kr.user.model.vo.UsersVerificationsPurPose;
import co.kr.user.model.vo.UsersVerificationsStatus;
import co.kr.user.service.UserService;
import co.kr.user.util.CookieUtil;
import co.kr.user.util.EmailTemplateProvider;
import co.kr.user.util.MailUtil;
import co.kr.user.util.RandomCodeUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.regex.Pattern;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private final UserInformationRepository userInformationRepository;
    private final UserVerificationsRepository userVerificationsRepository;

    private final RedisTemplate<String, Object> redisTemplate;

    private final UserQueryServiceImpl userQueryServiceImpl;

    private final RandomCodeUtil randomCodeUtil;
    private final MailUtil mailUtil;
    private final EmailTemplateProvider emailTemplateProvider;

    @Override
    public BigDecimal balance(Long userIdx) {
        Users users = userQueryServiceImpl.findActiveUser(userIdx);

        UsersInformation userInfo = userInformationRepository.findByUsersIdxAndDel(users.getUsersIdx(), 0)
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보를 찾을 수 없습니다."));

        return userInfo.getBalance();
    }

    public UserDTO my(Long userIdx) {
        Users users = userQueryServiceImpl.findActiveUser(userIdx);

        UsersInformation userInfo = userInformationRepository.findByUsersIdxAndDel(users.getUsersIdx(), 0)
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보를 찾을 수 없습니다."));

        UserDTO userDTO = new UserDTO();
        userDTO.setId(users.getId());
        userDTO.setRole(users.getRole());
        userDTO.setMembership(users.getMembership());
        userDTO.setCreatedAt(users.getCreatedAt());
        userDTO.setBalance(userInfo.getBalance());

        return userDTO;
    }

    public UserProfileDTO myDetails(Long userIdx) {
        Users users = userQueryServiceImpl.findActiveUser(userIdx);

        UsersInformation userInfo = userInformationRepository.findByUsersIdxAndDel(users.getUsersIdx(), 0)
                .orElseThrow(() -> new IllegalArgumentException("상세 회원 정보를 찾을 수 없습니다. UserID: " + users.getUsersIdx()));

        UserProfileDTO userProfileDTO = new UserProfileDTO();
        userProfileDTO.setName(userInfo.getName());
        userProfileDTO.setPhoneNumber(userInfo.getPhoneNumber());
        userProfileDTO.setBirth(userInfo.getBirth());
        userProfileDTO.setGender(userInfo.getGender());
        userProfileDTO.setMail(userInfo.getMail());
        userProfileDTO.setAgreeMarketingAt(userInfo.getAgreeMarketingAt());

        return userProfileDTO;
    }

    @Override
    @Transactional
    public UserDeleteDTO myDelete(Long userIdx) {
        Users users = userQueryServiceImpl.findActiveUser(userIdx);
        UsersInformation usersInformation = userInformationRepository.findByUsersIdxAndDel(users.getUsersIdx(), 0)
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보를 찾을 수 없습니다. UserID: " + users.getUsersIdx()));

        UsersVerifications usersVerifications = co.kr.user.model.entity.UsersVerifications.builder()
                .usersIdx(users.getUsersIdx())
                .purpose(UsersVerificationsPurPose.DELETE_ACCOUNT)
                .code(randomCodeUtil.getCode())
                .expiresAt(LocalDateTime.now().plusMinutes(30))
                .status(UsersVerificationsStatus.PENDING)
                .build();

        UsersVerifications savedUserVerifications = userVerificationsRepository.save(usersVerifications);

        String finalContent = emailTemplateProvider.getDeleteAccountTemplate(savedUserVerifications.getCode());

        EmailMessage emailMessage = EmailMessage.builder()
                .to(usersInformation.getMail())
                .subject("[GutJJeu] 회원탈퇴 인증번호 안내해 드립니다.")
                .message(finalContent)
                .build();

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                mailUtil.sendEmail(emailMessage, true);
            }
        });

        UserDeleteDTO userDeleteDTO = new UserDeleteDTO();
        userDeleteDTO.setMail(usersInformation.getMail());
        userDeleteDTO.setCertificationTime(LocalDateTime.now());

        return userDeleteDTO;
    }

    @Override
    @Transactional
    public String myDelete(Long userIdx, String authCode, HttpServletResponse response) {
        Users users = userQueryServiceImpl.findActiveUser(userIdx);

        UsersInformation usersInformation = userInformationRepository.findByUsersIdxAndDel(users.getUsersIdx(), 0)
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보를 찾을 수 없습니다."));

        UsersVerifications verification = userVerificationsRepository.findTopByUsersIdxAndDelOrderByCreatedAtDesc(users.getUsersIdx(), 0)
                .orElseThrow(() -> new IllegalArgumentException("인증 요청 내역이 존재하지 않습니다."));

        if (verification.getPurpose() != UsersVerificationsPurPose.DELETE_ACCOUNT) {
            throw new IllegalArgumentException("회원탈퇴 인증 코드가 아닙니다.");
        }
        else if (verification.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("인증 시간이 만료되었습니다. 인증번호를 다시 요청해주세요.");
        }
        else if (!verification.getCode().equals(authCode)) {
            throw new IllegalArgumentException("인증번호가 일치하지 않습니다.");
        }
        else if (verification.getStatus() == UsersVerificationsStatus.VERIFIED) {
            return "이미 인증 완료된 코드입니다.";
        }

        verification.confirmVerification();

        users.deleteUsers(users.getId() + "_DEL_" + LocalDateTime.now());
        usersInformation.deleteInformation(usersInformation.getMail() + "_DEL_" + LocalDateTime.now());

        String rtKey = "RT:" + users.getUsersIdx();
        String refreshToken = (String) redisTemplate.opsForValue().get(rtKey);

        if (refreshToken != null) {
            redisTemplate.delete(rtKey);

            redisTemplate.opsForValue().set("BL:" + refreshToken, "DELETED_BY_ADMIN");
        }

        CookieUtil.deleteCookie(response, CookieUtil.ACCESS_TOKEN_NAME);
        CookieUtil.deleteCookie(response, CookieUtil.REFRESH_TOKEN_NAME);

        return "회원 탈퇴가 정상 처리되었습니다.";
    }

    @Override
    @Transactional
    public UserAmendReq myAmend(Long userIdx, UserAmendReq userAmendReq) {
        Users users = userQueryServiceImpl.findActiveUser(userIdx);
        UsersInformation userInfo = userInformationRepository.findByUsersIdxAndDel(users.getUsersIdx(), 0)
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보를 찾을 수 없습니다."));

        String updateMail = userInfo.getMail();
        if (StringUtils.hasText(userAmendReq.getMail())) {
            if (!Pattern.matches("^[A-Za-z0-9+_.-]+@(.+)$", userAmendReq.getMail())) {
                throw new IllegalArgumentException("유효하지 않은 이메일 형식입니다.");
            }
            updateMail = userAmendReq.getMail();
        }

        String updateName = userInfo.getName();
        if (StringUtils.hasText(userAmendReq.getName())) {
            String trimmedName = userAmendReq.getName().trim();
            if (trimmedName.length() < 2) {
                throw new IllegalArgumentException("이름은 최소 2자 이상이어야 합니다.");
            }
            updateName = trimmedName;
        }

        String updatePhone = userInfo.getPhoneNumber();
        if (StringUtils.hasText(userAmendReq.getPhoneNumber())) {
            String cleanPhone = userAmendReq.getPhoneNumber();
            if (cleanPhone.length() < 10 || cleanPhone.length() > 13) {
                throw new IllegalArgumentException("유효하지 않은 전화번호 형식입니다.");
            }
            updatePhone = cleanPhone;
        }

        String updateBirth = userInfo.getBirth();
        if (StringUtils.hasText(userAmendReq.getBirth())) {
            if (!Pattern.matches("^\\d{8}$", userAmendReq.getBirth())) {
                throw new IllegalArgumentException("생년월일은 8자리 숫자(YYYYMMDD)여야 합니다.");
            }
            updateBirth = userAmendReq.getBirth();
        }

        userInfo.updateInformation(
                updateMail,
                userAmendReq.getGender() != null ? userAmendReq.getGender() : userInfo.getGender(),
                updateName,
                updatePhone,
                updateBirth
        );

        return userAmendReq;
    }
}