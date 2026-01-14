package co.kr.user.service;

import co.kr.user.DAO.UserInformationRepository;
import co.kr.user.DAO.UserRepository;
import co.kr.user.DAO.UserVerificationsRepository;
import co.kr.user.model.DTO.mail.EmailMessage;
import co.kr.user.model.DTO.register.RegisterDTO;
import co.kr.user.model.DTO.register.RegisterReq;
import co.kr.user.model.entity.Users;
import co.kr.user.model.entity.Users_Information;
import co.kr.user.model.entity.Users_Verifications;
import co.kr.user.model.vo.UsersVerificationsPurPose;
import co.kr.user.model.vo.UsersVerificationsStatus;
import co.kr.user.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Member;
import java.time.LocalDateTime;

/**
 * [회원가입 서비스 구현체]
 * 실제 회원가입과 관련된 모든 데이터 처리, 암호화, DB 저장, 메일 발송 로직이 모여있는 곳입니다.
 * @Service: 스프링이 이 클래스를 비즈니스 로직을 담당하는 빈(Bean)으로 관리하도록 설정합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor // final 필드에 대한 생성자 자동 주입
public class RegisterService implements RegisterServiceImpl{

    // [Repository] DB에 접근하기 위한 객체들 (JPA 사용)
    private final UserRepository userRepository;                   // 기본 계정 정보(ID, PW 등) 저장소
    private final UserInformationRepository userInformationRepository; // 상세 개인정보(이름, 연락처 등) 저장소
    private final UserVerificationsRepository userVerificationsRepository; // 인증 코드 내역 저장소

    // [Utility] 암호화 및 기타 기능을 위한 도구들
    private final BCryptUtil bCryptUtil;   // 단방향 암호화 (비밀번호용 - 복호화 불가)
    private final AesUtil aesUtil;         // 양방향 암호화 (개인정보용 - 복호화 가능)
    private final RandomCodeUtil randomCodeUtil; // 인증 코드 생성기
    private final EMailUtil eMailUtil;     // 이메일 발송기

    /**
     * [이메일 중복 체크]
     * DB를 조회하여 해당 이메일이 이미 존재하는지 확인합니다.
     * @Transactional(readOnly = true): 조회 전용 쿼리이므로 성능 최적화를 위해 읽기 전용으로 설정합니다.
     */
    @Transactional(readOnly = true)
    public String checkDuplicate(String email) {
        // DB에 해당 ID(이메일)가 있는지 확인 (exists 쿼리 실행)
        boolean isDuplicate = userRepository.existsByID(email);

        log.info("Check duplicate email: {}", isDuplicate);

        String result;
        if (isDuplicate) {
            result = "이메일 사용이 불가능합니다";
        } else {
            result = "이메일 사용이 가능합니다.";
        }

        log.info(result);
        return result;
    }

    /**
     * [회원가입 로직 실행]
     * 1. 중복/약관 동의 검사
     * 2. 중요 정보 암호화 (비밀번호, 개인정보)
     * 3. DB 저장 (Users -> Users_Information -> Users_Verifications 순서)
     * 4. 인증 메일 발송
     * * @Transactional: 이 메서드 내의 모든 DB 작업은 하나의 작업 단위(트랜잭션)로 묶입니다.
     * 중간에 에러가 발생하면 저장했던 모든 데이터가 자동으로 롤백(취소)됩니다.
     */
    @Transactional
    public RegisterDTO signup(RegisterReq registerReq) {
        // 1. [유효성 재확인] 컨트롤러에서 검증했지만, 비즈니스 로직상 안전을 위해 한 번 더 체크
        boolean isDuplicate = userRepository.existsByID(registerReq.getID());
        if (isDuplicate) {
            throw new IllegalStateException("이미 존재하는 이메일입니다.");
        }

        // 약관 동의 여부 체크 (필수 항목 누락 시 예외 발생)
        if (registerReq.getAgreeTermsAt() == null) {
            throw new IllegalStateException("이용약관 동의는 필수입니다.");
        }
        if (registerReq.getAgreePrivateAt() == null) {
            throw new IllegalStateException("개인정보 처리방침 동의는 필수입니다.");
        }

        // 2. [암호화 수행] 보안이 필요한 데이터 변환
        // 비밀번호 -> BCrypt 해시 (복구 불가능, 로그인 시 비교만 가능)
        registerReq.setPW(bCryptUtil.setPassword(registerReq.getPW()));

        // 이름, 전화번호, 생년월일 -> AES 암호화 (관리자 페이지 등에서 복호화해서 볼 수 있음)
        registerReq.setName(aesUtil.encrypt(registerReq.getName()));
        registerReq.setPhoneNumber(aesUtil.encrypt(registerReq.getPhoneNumber()));
        registerReq.setBirth(aesUtil.encrypt(registerReq.getBirth().toString()));

        log.info("RegisterReq : {}", registerReq); // (운영 시 암호화된 값이라도 로그 주의 필요)

        // 3-1. [Users 엔티티 생성 및 저장] (기본 회원 정보)
        Users user = Users.builder()
                .ID(registerReq.getID())
                .PW(registerReq.getPW())
                .agreeTermsAt(LocalDateTime.now()) // 현재 시간 기록
                .agreePrivacyAt(LocalDateTime.now())
                .agreeMarketingAt(registerReq.getAgreeMarketingAt()) // 선택 동의는 null일 수 있음
                .build();

        Users savedUser = userRepository.save(user); // DB Insert 실행
        log.info("Saved User : {}", savedUser);

        // 3-2. [Users_Information 엔티티 생성 및 저장] (부가 개인 정보)
        // Users 테이블과 1:1 관계이며, Users의 PK(usersIdx)를 외래키로 사용
        Users_Information usersInformation = Users_Information.builder()
                .usersIdx(savedUser.getUsersIdx()) // 방금 저장된 회원의 ID 연결
                .name(registerReq.getName())
                .phoneNumber(registerReq.getPhoneNumber())
                .birth(registerReq.getBirth())
                .build();

        Users_Information savedUserInformation = userInformationRepository.save(usersInformation);
        log.info("Saved User Information : {}", savedUserInformation);

        // 3-3. [인증 정보 생성 및 저장]
        // 이메일 인증을 위한 랜덤 코드를 생성하여 DB에 저장
        Users_Verifications usersVerifications = Users_Verifications.builder()
                .usersIdx(savedUser.getUsersIdx())
                .purPose(UsersVerificationsPurPose.SIGNUP) // 목적: 회원가입 인증
                .code(randomCodeUtil.getCode()) // 랜덤 코드 생성 유틸 호출
                .expiresAt(LocalDateTime.now().plusMinutes(30)) // 유효기간: 30분 뒤
                .status(UsersVerificationsStatus.PENDING) // 상태: 대기 중
                .build();

        Users_Verifications savedUserVerifications = userVerificationsRepository.save(usersVerifications);
        log.info("Saved User Verifications : {}", savedUserVerifications);

        // 4. [이메일 발송]
        // HTML 템플릿에 생성된 인증 코드를 삽입하여 메일 객체 생성

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

        // 메일 전송 객체 생성
        EmailMessage emailMessage = EmailMessage.builder()
                .to(savedUser.getID()) // 수신자: 가입한 이메일 ID
                .subject("[GutJJeu] 회원가입 인증번호 안내해 드립니다.")
                .message(finalContent)
                .build();

        // 비동기(@Async)로 메일 발송 (사용자는 메일 발송 완료를 기다리지 않고 바로 응답을 받음)
        eMailUtil.sendEmail(emailMessage, true);

        // 5. [결과 반환]
        // 클라이언트에게 가입된 ID와 인증 만료 시간을 알려줌
        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setID(savedUser.getID());
        registerDTO.setCertificationTime(savedUserVerifications.getExpiresAt());

        log.info("RegisterDTO : {}", registerDTO);

        return registerDTO;
    }

    /**
     * [인증 코드 확인 로직]
     * 사용자가 이메일을 보고 입력한 코드가 유효한지 검사합니다.
     */
    @Transactional
    public String signupAuthentication(String code) {
        // 1. [DB 조회] 해당 코드를 가진 인증 내역 중 가장 최신 것 조회
        // (코드가 같을 수 있으므로 생성 시간 내림차순으로 1개만 가져옴)
        Users_Verifications usersVerifications = userVerificationsRepository.findTopByCodeOrderByCreatedAtDesc(code)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 인증 코드입니다."));

        // 2. [만료 시간 검사] 현재 시간이 만료 시간보다 지났는지 확인
        if (usersVerifications.getExpiresAt().isBefore(LocalDateTime.now())) {
            return "인증 시간이 만료되었습니다.";
        }

        // 3. [중복 인증 방지] 이미 인증된 코드인지 확인
        if (usersVerifications.getStatus() == UsersVerificationsStatus.VERIFIED) {
            return "이미 인증 완료된 코드입니다.";
        }

        // 4. [인증 완료 처리] (핵심!)
        // 별도의 save() 호출 없이, 엔티티의 상태 값을 변경하면
        // 트랜잭션 종료 시 JPA의 Dirty Checking 기능이 작동하여 자동으로 Update 쿼리가 실행됩니다.
        usersVerifications.confirmVerification();

        // =========================================================
        // 5. [회원 상태 변경] (Users 테이블의 Del 값을 2 -> 0 으로 변경)
        // =========================================================

        // 5-1. 인증 정보에 있는 userIdx를 통해 Users 엔티티 조회
        Users user = userRepository.findById(usersVerifications.getUsersIdx())
                .orElseThrow(() -> new IllegalArgumentException("가입된 회원 정보를 찾을 수 없습니다."));

        // 5-2. Users 엔티티의 상태 변경 메서드 호출 (del = 0)
        // (@Transactional 안에서 엔티티의 값을 바꾸면 DB Update 쿼리가 자동 실행됩니다)
        user.confirmVerification();

        return "인증 완료";
    }
}