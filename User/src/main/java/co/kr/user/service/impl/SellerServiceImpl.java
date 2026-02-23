package co.kr.user.service.impl;

import co.kr.user.dao.FileRepository;
import co.kr.user.dao.SellerRepository;
import co.kr.user.dao.UserVerificationsRepository;
import co.kr.user.model.dto.mail.EmailMessage;
import co.kr.user.model.dto.my.UserDeleteDTO;
import co.kr.user.model.dto.seller.SellerAmendReq;
import co.kr.user.model.dto.seller.SellerProfileDTO;
import co.kr.user.model.dto.seller.SellerRegisterDTO;
import co.kr.user.model.dto.seller.SellerRegisterReq;
import co.kr.user.model.entity.*;
import co.kr.user.model.vo.*;
import co.kr.user.service.S3Service;
import co.kr.user.service.SellerService;
import co.kr.user.service.UserQueryService;
import co.kr.user.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * SellerService 인터페이스의 구현체입니다.
 * 판매자 등록(입점), 정보 수정, 조회, 탈퇴 등 판매자 관련 비즈니스 로직을 처리합니다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SellerServiceImpl implements SellerService {
    private final SellerRepository sellerRepository;
    private final FileRepository fileRepository;
    private final UserVerificationsRepository userVerificationsRepository;

    private final UserQueryService userQueryService;
    private final S3Service s3Service;

    private final MailUtil mailUtil;
    private final RandomCodeUtil randomCodeUtil;
    private final BCryptUtil bCryptUtil;
    private final EmailTemplateProvider emailTemplateProvider;
    private final FileUtil fileUtil;

    // 기본 이미지 경로
    private static final String DEFAULT_IMAGE = "seller/profile/default_index.png";

    @Value("${custom.security.verification.expiration-minutes}")
    private long expirationMinutes;

    @Value("${custom.mail.subject.seller-reg}")
    private String sellerRegSubject;

    @Value("${custom.mail.subject.delete-account}")
    private String deleteSellerSubject;

    @Override
    @Transactional
    public SellerRegisterDTO sellerRegister(Long userIdx, SellerRegisterReq req) {
        UsersInformation userInfo = userQueryService.findActiveUserInfo(userIdx);

        if (sellerRepository.existsByUsersIdxAndDel(userIdx, UserDel.ACTIVE)) {
            throw new IllegalArgumentException("이미 판매자 등록이 완료된 사용자입니다.");
        }

        if (sellerRepository.existsByUsersIdxAndDel(userIdx, UserDel.PENDING)) {
            throw new IllegalArgumentException("이미 판매자 등록을 신청하여 승인 대기 중입니다. 이메일을 확인해주세요.");
        }

        Seller seller = Seller.builder()
                .usersIdx(userIdx)
                .sellerName(req.getSellerName())
                .businessLicense(req.getBusinessLicense())
                .bankBrand(req.getBankBrand())
                .bankName(req.getBankName())
                .bankToken(bCryptUtil.encode(req.getBankToken()))
                .build();
        sellerRepository.save(seller);

        UsersVerifications verification = UsersVerifications.builder()
                .usersIdx(userIdx)
                .purpose(UsersVerificationsPurPose.SELLER_SIGNUP)
                .code(randomCodeUtil.getCode())
                .expiresAt(LocalDateTime.now().plusMinutes(expirationMinutes))
                .status(UsersVerificationsStatus.PENDING)
                .build();
        userVerificationsRepository.save(verification);

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

    @Override
    @Transactional
    public String sellerRegisterCheck(Long userIdx, String authCode) {
        Users user = userQueryService.findActiveUser(userIdx);
        UsersInformation userInfo = userQueryService.findActiveUserInfo(userIdx);

        UsersVerifications verification = userVerificationsRepository.findTopByUsersIdxAndDelOrderByCreatedAtDesc(userIdx, PublicDel.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("인증 요청 내역이 존재하지 않습니다."));

        if (verification.getPurpose() != UsersVerificationsPurPose.SELLER_SIGNUP) {
            throw new IllegalArgumentException("올바르지 않은 인증 요청입니다.");
        } else if (verification.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("인증 시간이 만료되었습니다.");
        } else if (!verification.getCode().equals(authCode)) {
            throw new IllegalArgumentException("인증번호가 일치하지 않습니다.");
        }

        verification.confirmVerification();

        Seller seller = sellerRepository.findByUsersIdxAndDel(userIdx, UserDel.PENDING)
                .orElseThrow(() -> new IllegalArgumentException("대기 중인 판매자 정보를 찾을 수 없습니다."));

        user.assignRole(UsersRole.SELLER);
        seller.activateSeller();

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

    @Override
    @Transactional
    public String myAmend(Long userIdx, SellerAmendReq req) {
        userQueryService.findActiveUser(userIdx);
        Seller seller = sellerRepository.findByUsersIdxAndDel(userIdx, UserDel.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("판매자 정보를 찾을 수 없습니다."));

        String encodedBankToken = StringUtils.hasText(req.getBankToken()) ? bCryptUtil.encode(req.getBankToken()) : null;
        seller.updateSeller(req.getSellerName(), req.getBankBrand(), req.getBankName(), encodedBankToken);

        return "판매자 정보가 수정되었습니다.";
    }

    @Override
    @Transactional
    public UserDeleteDTO myDelete(Long userIdx) {
        userQueryService.findActiveUser(userIdx);
        UsersInformation userInfo = userQueryService.findActiveUserInfo(userIdx);

        sellerRepository.findByUsersIdxAndDel(userIdx, UserDel.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("판매자 정보를 찾을 수 없습니다."));

        UsersVerifications verification = UsersVerifications.builder()
                .usersIdx(userIdx)
                .purpose(UsersVerificationsPurPose.SELLER_DELETE)
                .code(randomCodeUtil.getCode())
                .expiresAt(LocalDateTime.now().plusMinutes(expirationMinutes))
                .status(UsersVerificationsStatus.PENDING)
                .build();
        userVerificationsRepository.save(verification);

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

    @Override
    @Transactional
    public String myDelete(Long userIdx, String authCode) {
        userQueryService.findActiveUser(userIdx);

        UsersVerifications verification = userVerificationsRepository.findTopByUsersIdxAndDelOrderByCreatedAtDesc(userIdx, PublicDel.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("인증 요청 내역이 존재하지 않습니다."));

        if (verification.getPurpose() != UsersVerificationsPurPose.SELLER_DELETE) {
            throw new IllegalArgumentException("인증 코드가 일치하지 않습니다.");
        } else if (verification.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("인증 시간이 만료되었습니다.");
        } else if (!verification.getCode().equals(authCode)) {
            throw new IllegalArgumentException("인증번호가 일치하지 않습니다.");
        }

        verification.confirmVerification();

        Seller seller = sellerRepository.findByUsersIdxAndDel(userIdx, UserDel.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("판매자 정보를 찾을 수 없습니다."));

        seller.deleteSeller();
        return "판매자 탈퇴가 정상 처리되었습니다.";
    }

    @Override
    @Transactional
    public String updateProfileImage(Long userIdx, MultipartFile file) throws IOException {
        Seller seller = sellerRepository.findByUsersIdxAndDel(userIdx, UserDel.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("판매자 정보를 찾을 수 없습니다."));

        byte[] safeData = fileUtil.validateAndProcessImage(file);

        String originName = file.getOriginalFilename();
        String extension = originName.substring(originName.lastIndexOf(".") + 1).toLowerCase();

        // 경로와 파일명을 분리하여 정의
        String s3Path = "seller/profile/";
        String uuidName = UUID.randomUUID() + "." + extension;

        // 기존 파일 Soft Delete
        fileRepository.findByRefTableAndRefIndexAndDel("Seller", seller.getSellerIdx(), PublicDel.ACTIVE)
                .ifPresent(File::markAsDeleted);

        // S3에는 전체 경로(경로+파일명)로 업로드
        s3Service.putObject(safeData, s3Path + uuidName, file.getContentType());

        // DB에는 filePath에 경로만, fileName에 UUID 파일명만 저장
        fileRepository.save(File.builder()
                .fileOrigin(originName)
                .fileName(uuidName)
                .fileType(extension)
                .filePath(s3Path)
                .refTable("Seller")
                .refIndex(seller.getSellerIdx())
                .del(PublicDel.ACTIVE)
                .build());

        return "프로필 이미지가 변경되었습니다.";
    }

    @Override
    public String getMyProfileImage(Long userIdx) {
        Seller seller = sellerRepository.findByUsersIdxAndDel(userIdx, UserDel.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("판매자 정보를 찾을 수 없습니다."));

        // DB에서 경로와 파일명을 가져와서 합친 후 S3 URL 생성
        return fileRepository.findByRefTableAndRefIndexAndDel("Seller", seller.getSellerIdx(), PublicDel.ACTIVE)
                .map(f -> s3Service.getPresignedUrl(f.getFilePath() + f.getFileName()))
                .orElse(s3Service.getPresignedUrl(DEFAULT_IMAGE));
    }
}