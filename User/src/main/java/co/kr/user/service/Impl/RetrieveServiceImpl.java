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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;

/**
 * RetrieveService 인터페이스의 구현체입니다.
 * 아이디 찾기 및 비밀번호 재설정 관련 비즈니스 로직을 처리합니다.
 * 이메일 인증을 통한 본인 확인 절차를 포함합니다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RetrieveServiceImpl implements RetrieveService {
    private final UserVerificationsRepository userVerificationsRepository;
    private final UserInformationRepository userInformationRepository;
    private final UserQueryService userQueryService;

    private final RedisTemplate<String, Object> redisTemplate;

    // 랜덤 코드 생성, 메일 발송, 비밀번호 암호화 등 유틸리티 주입
    private final RandomCodeUtil randomCodeUtil;
    private final MailUtil mailUtil;
    private final BCryptUtil bCryptUtil;
    private final EmailTemplateProvider emailTemplateProvider;

    // application.yml에서 설정된 인증번호 유효 시간 (분 단위)
    @Value("${custom.security.verification.expiration-minutes}")
    private long expirationMinutes;

    // 아이디 찾기 메일 제목
    @Value("${custom.mail.subject.find-id}")
    private String findIdSubject;

    // 비밀번호 재설정 메일 제목
    @Value("${custom.mail.subject.reset-pw}")
    private String resetPwSubject;

    @Value("${custom.security.redis.rt-prefix}")
    private String rtPrefix; // Refresh Token Key 접두사

    @Value("${custom.security.redis.bl-prefix}")
    private String blPrefix; // Blacklist Key 접두사

    /**
     * [아이디 찾기 1단계]
     * 입력받은 이메일로 가입된 정보가 있는지 확인하고, 인증 메일을 발송합니다.
     * @param mail 사용자가 입력한 이메일
     * @return 1단계 결과 DTO (이메일, 인증 만료 시간)
     */
    @Override
    @Transactional // 인증 정보 저장 및 메일 발송을 위해 트랜잭션 적용
    public FindIDFirstStepDTO findIdFirst(String mail) {
        // 이메일로 활성 상태인 사용자 정보 조회
        UsersInformation info = userInformationRepository.findByMailAndDel(mail, UserDel.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("해당 이메일로 가입된 정보가 없습니다."));

        // 사용자 계정 상태 확인 (탈퇴하거나 정지된 계정인지 체크)
        userQueryService.findActiveUser(info.getUsersIdx());

        // 아이디 찾기 목적(FIND_ID)의 인증 정보 생성 및 저장
        UsersVerifications verification = UsersVerifications.builder()
                .usersIdx(info.getUsersIdx())
                .purpose(UsersVerificationsPurPose.FIND_ID)
                .code(randomCodeUtil.getCode()) // 랜덤 인증 코드 생성
                .expiresAt(LocalDateTime.now().plusMinutes(expirationMinutes)) // 유효시간 설정
                .status(UsersVerificationsStatus.PENDING)
                .build();

        userVerificationsRepository.save(verification);

        // 트랜잭션 커밋 후 메일 발송 (이메일 템플릿 사용)
        sendEmailAfterCommit(info.getMail(), findIdSubject, emailTemplateProvider.getFindIDTemplate(verification.getCode()));

        // 결과 반환
        FindIDFirstStepDTO response = new FindIDFirstStepDTO();
        response.setMail(info.getMail());
        response.setCertificationTime(verification.getExpiresAt());
        return response;
    }

    /**
     * [아이디 찾기 2단계]
     * 이메일로 전송된 인증 코드를 검증하고, 성공 시 아이디를 반환합니다.
     * @param req 이메일과 인증 코드가 담긴 요청 객체
     * @return 사용자 아이디
     */
    @Override
    public String findIdSecond(FindIDSecondStepReq req) {
        // 이메일로 사용자 정보 조회
        UsersInformation info = userInformationRepository.findByMailAndDel(req.getMail(), UserDel.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("해당 이메일 정보가 없습니다."));

        // 사용자 엔티티 조회
        Users user = userQueryService.findActiveUser(info.getUsersIdx());

        // 해당 사용자의 최신 인증 내역 조회
        UsersVerifications verification = userVerificationsRepository
                .findTopByUsersIdxAndDelOrderByCreatedAtDesc(info.getUsersIdx(), UserDel.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("인증 내역이 없습니다."));

        // 인증 코드 검증 (목적: FIND_ID)
        validateVerification(verification, req.getAuthCode(), UsersVerificationsPurPose.FIND_ID);

        // 검증 통과 시 아이디 반환
        return user.getId();
    }

    /**
     * [비밀번호 찾기 1단계]
     * 이메일로 가입 정보를 확인하고, 비밀번호 재설정용 인증 메일을 발송합니다.
     * @param mail 사용자가 입력한 이메일
     * @return 1단계 결과 DTO
     */
    @Override
    @Transactional
    public FindPWFirstStepDTO findPwFirst(String mail) {
        // 이메일로 사용자 정보 조회
        UsersInformation info = userInformationRepository.findByMailAndDel(mail, UserDel.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("해당 이메일로 가입된 정보가 없습니다."));

        // 사용자 계정 상태 확인
        userQueryService.findActiveUser(info.getUsersIdx());

        // 비밀번호 재설정 목적(RESET_PW)의 인증 정보 생성 및 저장
        UsersVerifications verification = UsersVerifications.builder()
                .usersIdx(info.getUsersIdx())
                .purpose(UsersVerificationsPurPose.RESET_PW)
                .code(randomCodeUtil.getCode())
                .expiresAt(LocalDateTime.now().plusMinutes(expirationMinutes))
                .status(UsersVerificationsStatus.PENDING)
                .build();

        userVerificationsRepository.save(verification);
        // 트랜잭션 커밋 후 메일 발송
        sendEmailAfterCommit(info.getMail(), resetPwSubject, emailTemplateProvider.getResetPasswordTemplate(verification.getCode()));

        // 결과 반환
        FindPWFirstStepDTO response = new FindPWFirstStepDTO();
        response.setMail(info.getMail());
        response.setCertificationTime(verification.getExpiresAt());
        return response;
    }

    /**
     * [비밀번호 찾기 2단계]
     * 인증 코드를 검증하고, 새로운 비밀번호로 변경합니다.
     * @param req 이메일, 인증 코드, 새 비밀번호 정보
     * @return 결과 메시지
     */
    @Override
    @Transactional
    public String findPwSecond(FindPWSecondStepReq req) {
        // 이메일로 사용자 정보 조회
        UsersInformation info = userInformationRepository.findByMailAndDel(req.getMail(), UserDel.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("해당 이메일 정보가 없습니다."));

        // 사용자 엔티티 조회
        Users user = userQueryService.findActiveUser(info.getUsersIdx());

        // 최신 인증 내역 조회
        UsersVerifications verification = userVerificationsRepository
                .findTopByUsersIdxAndDelOrderByCreatedAtDesc(info.getUsersIdx(), UserDel.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("인증 내역이 없습니다."));

        // 인증 코드 검증 (목적: RESET_PW)
        validateVerification(verification, req.getAuthCode(), UsersVerificationsPurPose.RESET_PW);

        // 새 비밀번호와 비밀번호 확인 일치 여부 검사
        if (!req.getNewPW().equals(req.getNewPWCheck())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 새 비밀번호 유효성 검사 (이전 비밀번호와 동일한지 등 정책 확인)
        info.validateNewPassword(req.getNewPW(), bCryptUtil::check);

        // 현재 비밀번호 백업
        String oldPw = user.getPw();
        // 비밀번호 변경 수행 (암호화하여 저장)
        user.changePassword(bCryptUtil.encode(req.getNewPW()), req.getNewPW(), bCryptUtil::check);

        // 변경 이력 관리 (이전 비밀번호 저장)
        info.updatePrePW(oldPw);
        // 인증 상태를 완료(VERIFIED)로 변경하여 재사용 방지
        verification.confirmVerification();

        // Redis에서 해당 사용자의 Refresh Token 조회
        String rtKey = rtPrefix + user.getUsersIdx(); // 예: "RT:101"

        // Redis에서 해당 사용자의 Refresh Token 조회
        String refreshToken = (String) redisTemplate.opsForValue().get(rtKey);

        if (refreshToken != null) {
            // 1) 기존 RT 삭제 (정상적인 갱신 차단)
            redisTemplate.delete(rtKey);

            // 2) 블랙리스트(BL) 등록 (만료 시간 없음)
            // 해커가 탈취한 토큰을 영구적으로 차단합니다.
            redisTemplate.opsForValue().set(blPrefix + refreshToken, "PASSWORD_RESET");
        }

        return "비밀번호 재설정이 정상 처리되었습니다.";
    }

    /**
     * 인증 정보의 유효성을 검사하는 내부 메서드입니다.
     * @param v 인증 정보 엔티티
     * @param code 사용자가 입력한 코드
     * @param purpose 요청된 인증 목적
     */
    private void validateVerification(UsersVerifications v, String code, UsersVerificationsPurPose purpose) {
        if (v.getStatus() == UsersVerificationsStatus.VERIFIED) throw new IllegalStateException("이미 완료된 인증입니다.");
        if (v.getPurpose() != purpose) throw new IllegalArgumentException("잘못된 접근입니다."); // 목적 불일치 (예: 가입용 코드로 비번 찾기 시도)
        if (v.getExpiresAt().isBefore(LocalDateTime.now())) throw new IllegalStateException("만료된 인증번호입니다.");
        if (!v.getCode().equals(code)) throw new IllegalArgumentException("인증번호가 일치하지 않습니다.");
    }

    /**
     * 트랜잭션 커밋 후 이메일을 발송하도록 스케줄링하는 내부 메서드입니다.
     * @param email 수신자 이메일
     * @param subject 메일 제목
     * @param content 메일 본문 (HTML)
     */
    private void sendEmailAfterCommit(String email, String subject, String content) {
        EmailMessage message = EmailMessage.builder()
                .to(email)
                .subject(subject)
                .message(content)
                .build();
        // DB 저장이 확실히 성공한 후에만 메일을 보내도록 설정
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() { mailUtil.sendEmail(message, true); }
        });
    }
}