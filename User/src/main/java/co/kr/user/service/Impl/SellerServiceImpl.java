package co.kr.user.service.Impl;

import co.kr.user.dao.SellerRepository;
import co.kr.user.dao.UserInformationRepository;
import co.kr.user.dao.UserRepository;
import co.kr.user.dao.UserVerificationsRepository;
import co.kr.user.model.dto.mail.EmailMessage;
import co.kr.user.model.dto.seller.SellerRegisterDTO;
import co.kr.user.model.dto.seller.SellerRegisterReq;
import co.kr.user.model.entity.Seller;
import co.kr.user.model.entity.Users;
import co.kr.user.model.entity.UsersInformation;
import co.kr.user.model.entity.UsersVerifications;
import co.kr.user.model.vo.UsersRole;
import co.kr.user.model.vo.UsersVerificationsPurPose;
import co.kr.user.model.vo.UsersVerificationsStatus;
import co.kr.user.service.SellerService;
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
import java.util.Objects;

/**
 * 판매자(Seller) 등록 및 인증 관련 비즈니스 로직을 처리하는 서비스 클래스입니다.
 * 일반 사용자가 판매자 전환 신청을 할 때의 정보 저장, 이메일 인증 발송, 인증 확인 및 권한 변경 등을 수행합니다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SellerServiceImpl implements SellerService {
    private final UserRepository userRepository;
    private final SellerRepository sellerRepository;
    private final UserVerificationsRepository userVerificationsRepository;
    private final UserInformationRepository userInformationRepository;

    private final UserQueryServiceImpl userQueryServiceImpl;

    private final MailUtil mailUtil; // 이메일 발송 유틸
    private final RandomCodeUtil randomCodeUtil; // 인증번호 생성 유틸
    private final BCryptUtil bCryptUtil; // 단방향 암호화 (계좌 토큰용)
    private final EmailTemplateProvider emailTemplateProvider;

    /**
     * 판매자 등록 신청 메서드입니다.
     * 판매자 정보(사업자 번호, 계좌 정보 등)를 저장하고, 본인 확인을 위한 인증 이메일을 발송합니다.
     *
     * @param userIdx 로그인한 사용자의 식별자
     * @param sellerRegisterReq 판매자 등록 요청 정보
     * @return SellerRegisterDTO (인증 요청 결과 및 만료 시간)
     */
    @Override
    @Transactional
    public SellerRegisterDTO sellerRegister(Long userIdx, SellerRegisterReq sellerRegisterReq) {
        Users users = userQueryServiceImpl.findActiveUser(userIdx);

        // Seller 엔티티 생성 및 저장
        // 계좌 토큰 등 민감 정보는 암호화(BCrypt)하여 저장
        Seller seller = Seller.builder()
                .sellerIdx(users.getUsersIdx())
                .sellerName(sellerRegisterReq.getSellerName())
                .businessLicense(sellerRegisterReq.getBusinessLicense())
                .bankBrand(sellerRegisterReq.getBankBrand())
                .bankName(sellerRegisterReq.getBankName())
                .bankToken(sellerRegisterReq.getBankToken())
                .build();

        sellerRepository.save(seller);

        // 인증 코드 생성 및 저장 (목적: SELLER_SIGNUP)
        UsersVerifications usersVerifications = co.kr.user.model.entity.UsersVerifications.builder()
                .usersIdx(users.getUsersIdx())
                .purpose(UsersVerificationsPurPose.SELLER_SIGNUP)
                .code(randomCodeUtil.getCode())
                .expiresAt(LocalDateTime.now().plusMinutes(30)) // 유효기간 30분
                .status(UsersVerificationsStatus.PENDING)
                .build();

        UsersVerifications savedUserVerifications = userVerificationsRepository.save(usersVerifications);

        // [수정] 하드코딩된 HTML 제거 -> 템플릿 프로바이더 호출
        String finalContent = emailTemplateProvider.getSellerRegisterTemplate(savedUserVerifications.getCode());

        EmailMessage emailMessage = EmailMessage.builder()
                .to(users.getId())
                .subject("[GutJJeu] 판매자 등록 인증번호 안내해 드립니다.")
                .message(finalContent)
                .build();

        // 트랜잭션 커밋 후 이메일 발송
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                mailUtil.sendEmail(emailMessage, true); // 여기서 비동기로 호출됨
            }
        });

        SellerRegisterDTO sellerRegisterDTO = new SellerRegisterDTO();
        sellerRegisterDTO.setID(users.getId());
        sellerRegisterDTO.setCertificationTime(savedUserVerifications.getExpiresAt());

        return sellerRegisterDTO;
    }

    /**
     * 판매자 등록 인증 확인 메서드입니다.
     * 이메일로 전송된 인증 코드를 검증하고, 성공 시 사용자의 권한(Role)을 SELLER로 변경합니다.
     * 승인 완료 알림 이메일도 함께 발송합니다.
     *
     * @param userIdx 로그인한 사용자의 식별자
     * @param authCode 사용자가 입력한 인증 코드
     * @return 처리 결과 메시지
     */
    @Override
    @Transactional
    public String sellerRegisterCheck(Long userIdx, String authCode) {
        Users users = userQueryServiceImpl.findActiveUser(userIdx);

        // 최신 인증 내역 조회
        UsersVerifications verification = userVerificationsRepository.findTopByUsersIdxAndDelOrderByCreatedAtDesc(users.getUsersIdx(), 0)
                .orElseThrow(() -> new IllegalArgumentException("인증 요청 내역이 존재하지 않습니다."));

        // 요청자 본인 확인
        if (!Objects.equals(verification.getUsersIdx(), users.getUsersIdx())) {
            throw new IllegalArgumentException("잘못된 인증 요청입니다.");
        }

        // 인증 목적, 만료 시간, 코드 일치 여부 검증
        if (verification.getPurpose() != UsersVerificationsPurPose.SELLER_SIGNUP) {
            throw new IllegalArgumentException("올바르지 않은 인증 요청입니다.");
        }

        if (verification.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("인증 시간이 만료되었습니다. 인증번호를 다시 요청해주세요.");
        }

        if (!verification.getCode().equals(authCode)) {
            throw new IllegalArgumentException("인증번호가 일치하지 않습니다.");
        }

        if (verification.getStatus() == UsersVerificationsStatus.VERIFIED) {
            return "이미 인증 완료된 코드입니다.";
        }

        // 인증 완료 처리
        verification.confirmVerification();

        // 판매자 정보 조회 및 승인 처리
        Seller seller = sellerRepository.findBySellerIdxAndDel(users.getUsersIdx(), 0)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이디입니다."));

        // 사용자 권한을 SELLER로 변경
        users.assignRole(UsersRole.SELLER);

        seller.activateSeller();

        String finalContent = emailTemplateProvider.getSellerApprovalTemplate(seller.getSellerName());

        EmailMessage emailMessage = EmailMessage.builder()
                .to(users.getId())
                .subject("[GutJJeu] 판매자 등록 승인 안내해 드립니다.")
                .message(finalContent)
                .build();

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                mailUtil.sendEmail(emailMessage, true); // 여기서 비동기로 호출됨
            }
        });

        return "판매자 등록이 완료되었습니다.";
    }
}