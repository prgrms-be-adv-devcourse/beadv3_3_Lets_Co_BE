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
import co.kr.user.model.vo.PublicDel;
import co.kr.user.model.vo.UserDel;
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

/**
 * RegisterService 인터페이스의 구현체입니다.
 * 회원가입 프로세스(아이디 중복 확인, 가입 요청, 이메일 인증)를 처리합니다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RegisterServiceImpl implements RegisterService {
    // 회원 정보 저장을 위한 Repository들
    private final UserRepository userRepository;
    private final UserInformationRepository userInformationRepository;
    private final UserVerificationsRepository userVerificationsRepository;

    private final UserQueryService userQueryService;

    // 비밀번호 암호화, 랜덤 코드 생성, 메일 발송 등 유틸리티들
    private final BCryptUtil bCryptUtil;
    private final RandomCodeUtil randomCodeUtil;
    private final MailUtil mailUtil;
    private final EmailTemplateProvider emailTemplateProvider;

    /**
     * 아이디 중복 여부를 확인합니다.
     * @param id 확인할 아이디
     * @return 중복 여부 메시지
     */
    public String checkDuplicate(String id) {
        if (userQueryService.existsActiveId(id)) {
            return "ID 사용이 불가능합니다";
        }
        return "ID 사용이 가능합니다.";
    }

    /**
     * 회원 가입 요청을 처리합니다.
     * 사용자 정보를 DB에 저장하고(대기 상태), 인증 이메일을 발송합니다.
     * @param registerReq 회원 가입 정보
     * @return 가입 결과 정보 (이메일, 인증 만료 시간 등)
     */
    @Transactional // 데이터 저장 및 이메일 발송 연계 작업을 위해 트랜잭션 적용
    public RegisterDTO signup(RegisterReq registerReq) {
        // 아이디 중복 재확인 (동시성 문제 방지)
        if (userQueryService.existsActiveId(registerReq.getId())) {
            throw new IllegalStateException("이미 존재하는 ID입니다.");
        }

        // 비밀번호 암호화
        registerReq.setPw(bCryptUtil.encode(registerReq.getPw()));

        // Users 엔티티 생성 및 저장 (기본적으로 비활성/대기 상태로 저장됨)
        Users user = Users.builder()
                .id(registerReq.getId())
                .pw(registerReq.getPw())
                .build();
        Users savedUser = userRepository.save(user);

        // UsersInformation 엔티티 생성 및 저장
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

        // 이메일 인증을 위한 Verification 엔티티 생성 및 저장
        UsersVerifications usersVerifications = UsersVerifications.builder()
                .usersIdx(savedUser.getUsersIdx())
                .purpose(UsersVerificationsPurPose.SIGNUP) // 회원가입용 인증
                .code(randomCodeUtil.getCode()) // 랜덤 인증 코드 생성
                .expiresAt(LocalDateTime.now().plusMinutes(30)) // 유효시간 30분
                .status(UsersVerificationsStatus.PENDING)
                .build();
        userVerificationsRepository.save(usersVerifications);

        // 인증 이메일 메시지 구성
        EmailMessage emailMessage = EmailMessage.builder()
                .to(usersInformation.getMail())
                .subject("[GutJJeu] 회원가입 인증번호 안내해 드립니다.")
                .message(emailTemplateProvider.getSignupTemplate(usersVerifications.getCode()))
                .build();

        // 트랜잭션 커밋이 성공한 직후에 이메일을 발송하도록 설정
        // (DB 저장이 실패했는데 메일이 발송되는 것을 방지)
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() { mailUtil.sendEmail(emailMessage, true); }
        });

        // 결과 DTO 생성
        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setMail(usersInformation.getMail());
        registerDTO.setCertificationTime(usersVerifications.getExpiresAt());
        return registerDTO;
    }

    /**
     * 이메일 인증 코드를 검증하여 회원 가입을 완료합니다.
     * 인증에 성공하면 사용자와 정보 상태를 'ACTIVE'로 변경합니다.
     * @param code 인증 코드
     * @return 인증 결과 메시지
     */
    @Transactional
    public String signupAuthentication(String code) {
        // 인증 코드로 Verification 엔티티 조회 (최신순)
        UsersVerifications usersVerifications = userVerificationsRepository.findTopByCodeAndDelOrderByCreatedAtDesc(code, PublicDel.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 인증 코드입니다."));

        // 유효성 검증
        if (usersVerifications.getExpiresAt().isBefore(LocalDateTime.now())) {
            return "인증 시간이 만료되었습니다.";
        } else if (usersVerifications.getPurpose() != UsersVerificationsPurPose.SIGNUP) {
            throw new IllegalArgumentException("회원가입 인증 코드가 아닙니다.");
        } else if (usersVerifications.getStatus() == UsersVerificationsStatus.VERIFIED) {
            return "이미 인증 완료된 코드입니다.";
        }

        // 인증 완료 처리 (상태 변경)
        usersVerifications.confirmVerification();

        // 해당 사용자의 대기 상태인 엔티티 조회
        Users user = userQueryService.findWaitUser(usersVerifications.getUsersIdx());
        UsersInformation userInfo = userQueryService.findWaitUserInfo(usersVerifications.getUsersIdx());

        // 사용자 계정 및 정보를 활성화(ACTIVE)
        user.activateUsers();
        userInfo.activateInformation();

        return "인증 완료";
    }
}