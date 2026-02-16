package co.kr.user.service.Impl;

import co.kr.user.dao.SellerRepository;
import co.kr.user.dao.UserVerificationsRepository;
import co.kr.user.model.dto.mail.EmailMessage;
import co.kr.user.model.dto.my.UserDeleteDTO;
import co.kr.user.model.dto.seller.SellerAmendReq;
import co.kr.user.model.dto.seller.SellerProfileDTO;
import co.kr.user.model.dto.seller.SellerRegisterDTO;
import co.kr.user.model.dto.seller.SellerRegisterReq;
import co.kr.user.model.entity.Seller;
import co.kr.user.model.entity.Users;
import co.kr.user.model.entity.UsersInformation;
import co.kr.user.model.entity.UsersVerifications;
import co.kr.user.model.vo.UserDel;
import co.kr.user.model.vo.UsersRole;
import co.kr.user.model.vo.UsersVerificationsPurPose;
import co.kr.user.model.vo.UsersVerificationsStatus;
import co.kr.user.service.SellerService;
import co.kr.user.service.UserQueryService;
import co.kr.user.util.BCryptUtil;
import co.kr.user.util.EmailTemplateProvider;
import co.kr.user.util.MailUtil;
import co.kr.user.util.RandomCodeUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * SellerService 인터페이스의 구현체입니다.
 * 판매자 등록(입점), 정보 수정, 조회, 탈퇴 등 판매자 관련 비즈니스 로직을 처리합니다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SellerServiceImpl implements SellerService {
    private final SellerRepository sellerRepository;
    private final UserVerificationsRepository userVerificationsRepository;
    private final UserQueryService userQueryService;

    // 유틸리티 주입
    private final MailUtil mailUtil;
    private final RandomCodeUtil randomCodeUtil;
    private final BCryptUtil bCryptUtil;
    private final EmailTemplateProvider emailTemplateProvider;

    // 인증 유효 시간
    @Value("${custom.security.verification.expiration-minutes}")
    private long expirationMinutes;

    // 판매자 등록 안내 메일 제목
    @Value("${custom.mail.subject.seller-reg}")
    private String sellerRegSubject;

    // 판매자 탈퇴 안내 메일 제목
    @Value("${custom.mail.subject.delete-account}")
    private String deleteSellerSubject;

    /**
     * 판매자 등록을 신청합니다.
     * 판매자 정보를 저장하고 본인 확인을 위한 인증 메일을 발송합니다.
     * @param userIdx 신청자 식별자 (PK)
     * @param req 판매자 등록 요청 정보
     * @return 등록 신청 결과 DTO
     */
    @Override
    @Transactional
    public SellerRegisterDTO sellerRegister(Long userIdx, SellerRegisterReq req) {
        UsersInformation userInfo = userQueryService.findActiveUserInfo(userIdx);

        // 이미 정식 판매자(ACTIVE)인지 확인
        if (sellerRepository.existsByUsersIdxAndDel(userIdx, UserDel.ACTIVE)) {
            throw new IllegalArgumentException("이미 판매자 등록이 완료된 사용자입니다.");
        }

        // 이미 신청 후 승인 대기 중(PENDING)인지 추가 확인 (이 부분이 누락되어 있었음)
        if (sellerRepository.existsByUsersIdxAndDel(userIdx, UserDel.PENDING)) {
            throw new IllegalArgumentException("이미 판매자 등록을 신청하여 승인 대기 중입니다. 이메일을 확인해주세요.");
        }

        // 판매자 엔티티 생성 및 저장 (은행 토큰은 암호화)
        Seller seller = Seller.builder()
                .usersIdx(userIdx)
                .sellerName(req.getSellerName())
                .businessLicense(req.getBusinessLicense())
                .bankBrand(req.getBankBrand())
                .bankName(req.getBankName())
                .bankToken(bCryptUtil.encode(req.getBankToken()))
                .build();
        sellerRepository.save(seller);

        // 판매자 등록용(SELLER_SIGNUP) 인증 정보 생성
        UsersVerifications verification = UsersVerifications.builder()
                .usersIdx(userIdx)
                .purpose(UsersVerificationsPurPose.SELLER_SIGNUP)
                .code(randomCodeUtil.getCode())
                .expiresAt(LocalDateTime.now().plusMinutes(expirationMinutes))
                .status(UsersVerificationsStatus.PENDING)
                .build();
        userVerificationsRepository.save(verification);

        // 인증 메일 발송 설정
        EmailMessage emailMessage = EmailMessage.builder()
                .to(userInfo.getMail())
                .subject(sellerRegSubject)
                .message(emailTemplateProvider.getSellerRegisterTemplate(verification.getCode()))
                .build();

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() { mailUtil.sendEmail(emailMessage, true); }
        });

        SellerRegisterDTO dto = new SellerRegisterDTO();
        dto.setMail(userInfo.getMail());
        dto.setCertificationTime(verification.getExpiresAt());
        return dto;
    }

    /**
     * 판매자 등록 인증 코드를 검증하고, 승인 처리를 완료합니다.
     * @param userIdx 신청자 식별자
     * @param authCode 인증 코드
     * @return 처리 결과 메시지
     */
    @Override
    @Transactional
    public String sellerRegisterCheck(Long userIdx, String authCode) {
        Users user = userQueryService.findActiveUser(userIdx);
        UsersInformation userInfo = userQueryService.findActiveUserInfo(userIdx);

        // 최신 인증 요청 조회
        UsersVerifications verification = userVerificationsRepository.findTopByUsersIdxAndDelOrderByCreatedAtDesc(userIdx, UserDel.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("인증 요청 내역이 존재하지 않습니다."));

        // 유효성 검증
        if (verification.getPurpose() != UsersVerificationsPurPose.SELLER_SIGNUP) {
            throw new IllegalArgumentException("올바르지 않은 인증 요청입니다.");
        } else if (verification.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("인증 시간이 만료되었습니다.");
        } else if (!verification.getCode().equals(authCode)) {
            throw new IllegalArgumentException("인증번호가 일치하지 않습니다.");
        }

        // 인증 완료 처리
        verification.confirmVerification();

        // 대기 상태(PENDING)인 판매자 정보를 찾아 활성화
        Seller seller = sellerRepository.findByUsersIdxAndDel(userIdx, UserDel.PENDING)
                .orElseThrow(() -> new IllegalArgumentException("대기 중인 판매자 정보를 찾을 수 없습니다."));

        // 사용자 권한을 SELLER로 변경
        user.assignRole(UsersRole.SELLER);
        // 판매자 상태 활성화
        seller.activateSeller();

        // 승인 완료 안내 메일 발송
        EmailMessage emailMessage = EmailMessage.builder()
                .to(userInfo.getMail())
                .subject("[GutJJeu] 판매자 등록 승인 안내해 드립니다.")
                .message(emailTemplateProvider.getSellerApprovalTemplate(seller.getSellerName()))
                .build();

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() { mailUtil.sendEmail(emailMessage, true); }
        });

        return "판매자 등록이 완료되었습니다.";
    }

    /**
     * 판매자 정보를 조회합니다.
     * @param userIdx 판매자 식별자
     * @return 판매자 프로필 정보 DTO
     */
    @Override
    public SellerProfileDTO my(Long userIdx) {
        userQueryService.findActiveUser(userIdx);
        Seller seller = sellerRepository.findByUsersIdxAndDel(userIdx, UserDel.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("판매자 정보를 찾을 수 없습니다."));

        SellerProfileDTO dto = new SellerProfileDTO();
        dto.setSellerName(seller.getSellerName());
        dto.setBusinessLicense(seller.getBusinessLicense());
        dto.setBankBrand(seller.getBankBrand());
        dto.setBankName(seller.getBankName());
        dto.setCreateAt(seller.getCreatedAt());
        dto.setUpdateAt(seller.getUpdatedAt());
        return dto;
    }

    /**
     * 판매자 정보를 수정합니다.
     * @param userIdx 판매자 식별자
     * @param req 수정 요청 정보
     * @return 성공 메시지
     */
    @Override
    @Transactional
    public String myAmend(Long userIdx, SellerAmendReq req) {
        userQueryService.findActiveUser(userIdx);
        Seller seller = sellerRepository.findByUsersIdxAndDel(userIdx, UserDel.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("판매자 정보를 찾을 수 없습니다."));

        // 은행 토큰이 입력된 경우 암호화, 아니면 null
        String encodedBankToken = StringUtils.hasText(req.getBankToken()) ? bCryptUtil.encode(req.getBankToken()) : null;

        // 정보 업데이트 (Dirty Checking)
        seller.updateSeller(req.getSellerName(), req.getBankBrand(), req.getBankName(), encodedBankToken);

        return "판매자 정보가 수정되었습니다.";
    }

    /**
     * [판매자 탈퇴 1단계]
     * 판매자 탈퇴를 위한 인증 메일을 발송합니다.
     * @param userIdx 판매자 식별자
     * @return 탈퇴 요청 결과 DTO
     */
    @Override
    @Transactional
    public UserDeleteDTO myDelete(Long userIdx) {
        userQueryService.findActiveUser(userIdx);
        UsersInformation userInfo = userQueryService.findActiveUserInfo(userIdx);

        // 판매자 정보 존재 확인
        sellerRepository.findByUsersIdxAndDel(userIdx, UserDel.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("판매자 정보를 찾을 수 없습니다."));

        // 판매자 탈퇴용(SELLER_DELETE) 인증 정보 생성
        UsersVerifications verification = UsersVerifications.builder()
                .usersIdx(userIdx)
                .purpose(UsersVerificationsPurPose.SELLER_DELETE)
                .code(randomCodeUtil.getCode())
                .expiresAt(LocalDateTime.now().plusMinutes(expirationMinutes))
                .status(UsersVerificationsStatus.PENDING)
                .build();
        userVerificationsRepository.save(verification);

        // 인증 메일 발송
        EmailMessage emailMessage = EmailMessage.builder()
                .to(userInfo.getMail())
                .subject(deleteSellerSubject)
                .message(emailTemplateProvider.getDeleteSellerTemplate(verification.getCode()))
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

    /**
     * [판매자 탈퇴 2단계]
     * 인증 코드를 검증하고 판매자 정보를 삭제 처리합니다.
     * @param userIdx 판매자 식별자
     * @param authCode 인증 코드
     * @return 성공 메시지
     */
    @Override
    @Transactional
    public String myDelete(Long userIdx, String authCode) {
        userQueryService.findActiveUser(userIdx);

        // 인증 요청 확인
        UsersVerifications verification = userVerificationsRepository.findTopByUsersIdxAndDelOrderByCreatedAtDesc(userIdx, UserDel.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("인증 요청 내역이 존재하지 않습니다."));

        // 유효성 검증
        if (verification.getPurpose() != UsersVerificationsPurPose.SELLER_DELETE) {
            throw new IllegalArgumentException("인증 코드가 일치하지 않습니다.");
        } else if (verification.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("인증 시간이 만료되었습니다.");
        } else if (!verification.getCode().equals(authCode)) {
            throw new IllegalArgumentException("인증번호가 일치하지 않습니다.");
        }

        // 인증 완료
        verification.confirmVerification();

        // 판매자 정보 조회
        Seller seller = sellerRepository.findByUsersIdxAndDel(userIdx, UserDel.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("판매자 정보를 찾을 수 없습니다."));

        // 판매자 정보 삭제 (논리적 삭제)
        seller.deleteSeller();
        return "판매자 탈퇴가 정상 처리되었습니다.";
    }
}