package co.kr.user.service.Impl;

import co.kr.user.dao.UserVerificationsRepository;
import co.kr.user.model.dto.mail.EmailMessage;
import co.kr.user.model.dto.my.*;
import co.kr.user.model.entity.Users;
import co.kr.user.model.entity.UsersInformation;
import co.kr.user.model.entity.UsersVerifications;
import co.kr.user.model.vo.PublicDel;
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

/**
 * UserService 인터페이스의 구현체입니다.
 * 사용자 마이페이지 정보 조회, 수정, 탈퇴 등 회원 개인과 관련된 핵심 비즈니스 로직을 처리합니다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private final UserVerificationsRepository userVerificationsRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final UserQueryService userQueryService;

    // 유틸리티 클래스 주입
    private final RandomCodeUtil randomCodeUtil;
    private final MailUtil mailUtil;
    private final EmailTemplateProvider emailTemplateProvider;

    // 인증 유효 시간 (분)
    @Value("${custom.security.verification.expiration-minutes}")
    private long expirationMinutes;

    // 회원 탈퇴 메일 제목
    @Value("${custom.mail.subject.delete-account}")
    private String deleteAccountSubject;

    // Redis 키 접두사 설정
    @Value("${custom.security.redis.rt-prefix}")
    private String rtPrefix;

    @Value("${custom.security.redis.bl-prefix}")
    private String blPrefix;

    /**
     * 사용자의 현재 잔액(포인트/머니)을 조회합니다.
     * @param userIdx 사용자 식별자
     * @return 현재 잔액
     */
    @Override
    public BigDecimal balance(Long userIdx) {
        return userQueryService.findActiveUserInfo(userIdx).getBalance();
    }

    /**
     * 마이페이지 메인에 표시할 사용자의 요약 정보를 조회합니다.
     * @param userIdx 사용자 식별자
     * @return 사용자 요약 정보 DTO (아이디, 등급, 가입일, 잔액 등)
     */
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

    /**
     * 사용자의 상세 프로필 정보를 조회합니다.
     * @param userIdx 사용자 식별자
     * @return 사용자 상세 정보 DTO (이름, 전화번호, 생일, 이메일 등)
     */
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

    /**
     * [회원 탈퇴 1단계]
     * 회원 탈퇴를 위해 본인 확인용 인증 메일을 발송합니다.
     * @param userIdx 사용자 식별자
     * @return 탈퇴 요청 결과 DTO (이메일 정보 포함)
     */
    @Override
    @Transactional
    public UserDeleteDTO myDelete(Long userIdx) {
        userQueryService.findActiveUser(userIdx);
        UsersInformation userInfo = userQueryService.findActiveUserInfo(userIdx);

        // 회원 탈퇴 목적(DELETE_ACCOUNT)의 인증 정보 생성
        UsersVerifications verification = UsersVerifications.builder()
                .usersIdx(userIdx)
                .purpose(UsersVerificationsPurPose.DELETE_ACCOUNT)
                .code(randomCodeUtil.getCode())
                .expiresAt(LocalDateTime.now().plusMinutes(expirationMinutes))
                .status(UsersVerificationsStatus.PENDING)
                .build();

        userVerificationsRepository.save(verification);

        // 탈퇴 안내 메일 구성
        EmailMessage emailMessage = EmailMessage.builder()
                .to(userInfo.getMail())
                .subject(deleteAccountSubject)
                .message(emailTemplateProvider.getDeleteAccountTemplate(verification.getCode()))
                .build();

        // 트랜잭션 커밋 후 메일 발송
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() { mailUtil.sendEmail(emailMessage, true); }
        });

        UserDeleteDTO dto = new UserDeleteDTO();
        dto.setMail(userInfo.getMail());
        dto.setCertificationTime(LocalDateTime.now());
        return dto;
    }

    /**
     * [회원 탈퇴 2단계]
     * 인증 코드를 검증하고 회원 탈퇴를 최종 완료합니다.
     * 토큰 무효화 및 쿠키 삭제 처리도 함께 수행합니다.
     * @param userIdx 사용자 식별자
     * @param authCode 인증 코드
     * @param response 쿠키 삭제를 위한 응답 객체
     * @return 처리 결과 메시지
     */
    @Override
    @Transactional
    public String myDelete(Long userIdx, String authCode, HttpServletResponse response) {
        Users users = userQueryService.findActiveUser(userIdx);
        UsersInformation userInfo = userQueryService.findActiveUserInfo(userIdx);

        // 최신 인증 요청 조회
        UsersVerifications verification = userVerificationsRepository.findTopByUsersIdxAndDelOrderByCreatedAtDesc(userIdx, PublicDel.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("인증 요청 내역이 존재하지 않습니다."));

        // 유효성 검증
        if (verification.getPurpose() != UsersVerificationsPurPose.DELETE_ACCOUNT) {
            throw new IllegalArgumentException("인증 코드가 일치하지 않습니다.");
        } else if (verification.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("인증 시간이 만료되었습니다.");
        } else if (!verification.getCode().equals(authCode)) {
            throw new IllegalArgumentException("인증번호가 일치하지 않습니다.");
        }

        // 인증 완료 처리
        verification.confirmVerification();

        // 사용자 및 정보 엔티티에 탈퇴 처리 (식별자에 탈퇴 시간 등을 붙여 변경)
        users.deleteUsers(users.getId() + "_DEL_" + LocalDateTime.now());
        userInfo.deleteInformation(userInfo.getMail() + "_DEL_" + LocalDateTime.now());

        // Redis에서 Refresh Token 삭제 및 블랙리스트 등록 (로그아웃 처리)
        String rtKey = rtPrefix + userIdx;
        String refreshToken = (String) redisTemplate.opsForValue().get(rtKey);
        if (refreshToken != null) {
            redisTemplate.delete(rtKey);
            redisTemplate.opsForValue().set(blPrefix + refreshToken, "DELETED_BY_USER");
        }

        // 클라이언트 쿠키 삭제 (Access/Refresh Token)
        CookieUtil.deleteCookie(response, CookieUtil.ACCESS_TOKEN_NAME);
        CookieUtil.deleteCookie(response, CookieUtil.REFRESH_TOKEN_NAME);

        return "회원 탈퇴가 정상 처리되었습니다.";
    }

    /**
     * 사용자 프로필 정보를 수정합니다.
     * @param userIdx 사용자 식별자
     * @param req 수정할 정보 (이메일, 성별, 이름 등)
     * @return 수정된 정보 DTO
     */
    @Override
    @Transactional
    public UserAmendDTO myAmend(Long userIdx, UserAmendReq req) {
        userQueryService.findActiveUser(userIdx);
        UsersInformation userInfo = userQueryService.findActiveUserInfo(userIdx);

        // 정보 업데이트 (Dirty Checking)
        userInfo.updateProfile(
                req.getMail(),
                req.getGender(),
                req.getName(),
                req.getPhoneNumber(),
                req.getBirth(),
                req.getAgreeMarketingAt()
        );

        UserAmendDTO userAmendDTO = new UserAmendDTO();
        userAmendDTO.setMail(userInfo.getMail());
        userAmendDTO.setGender(userInfo.getGender());
        userAmendDTO.setName(userInfo.getName());
        userAmendDTO.setPhoneNumber(userInfo.getPhoneNumber());
        userAmendDTO.setBirth(userInfo.getBirth());
        userAmendDTO.setAgreeMarketingAt(req.getAgreeMarketingAt());

        return userAmendDTO;
    }
}