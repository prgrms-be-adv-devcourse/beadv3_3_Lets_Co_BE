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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SellerServiceImpl implements SellerService {
    private final SellerRepository sellerRepository;
    private final UserVerificationsRepository userVerificationsRepository;

    private final UserQueryService userQueryService;

    private final MailUtil mailUtil;
    private final RandomCodeUtil randomCodeUtil;
    private final BCryptUtil bCryptUtil;
    private final EmailTemplateProvider emailTemplateProvider;

    @Override
    @Transactional
    public SellerRegisterDTO sellerRegister(Long userIdx, SellerRegisterReq req) {
        UsersInformation userInfo = userQueryService.findActiveUserInfo(userIdx);

        if (sellerRepository.existsByUsersIdxAndDel(userIdx, 0)) {
            throw new IllegalArgumentException("이미 판매자 등록이 완료된 사용자입니다.");
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
                .expiresAt(LocalDateTime.now().plusMinutes(30))
                .status(UsersVerificationsStatus.PENDING)
                .build();
        userVerificationsRepository.save(verification);

        EmailMessage emailMessage = EmailMessage.builder()
                .to(userInfo.getMail())
                .subject("[GutJJeu] 판매자 등록 인증번호 안내해 드립니다.")
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

        UsersVerifications verification = userVerificationsRepository.findTopByUsersIdxAndDelOrderByCreatedAtDesc(userIdx, 0)
                .orElseThrow(() -> new IllegalArgumentException("인증 요청 내역이 존재하지 않습니다."));

        if (verification.getPurpose() != UsersVerificationsPurPose.SELLER_SIGNUP) {
            throw new IllegalArgumentException("올바르지 않은 인증 요청입니다.");
        } else if (verification.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("인증 시간이 만료되었습니다. 인증번호를 다시 요청해주세요.");
        } else if (!verification.getCode().equals(authCode)) {
            throw new IllegalArgumentException("인증번호가 일치하지 않습니다.");
        }

        verification.confirmVerification();
        Seller seller = sellerRepository.findByUsersIdxAndDel(userIdx, 2)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이디입니다."));

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
        Seller seller = sellerRepository.findByUsersIdxAndDel(userIdx, 0)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이디입니다."));

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
        Seller seller = sellerRepository.findByUsersIdxAndDel(userIdx, 0)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 판매자입니다."));

        String sellerName = StringUtils.hasText(req.getSellerName()) ? req.getSellerName().trim() : seller.getSellerName();
        String bankBrand = StringUtils.hasText(req.getBankBrand()) ? req.getBankBrand().trim() : seller.getBankBrand();
        String bankName = StringUtils.hasText(req.getBankName()) ? req.getBankName().trim() : seller.getBankName();
        String bankToken = StringUtils.hasText(req.getBankToken()) ? bCryptUtil.encode(req.getBankToken()) : seller.getBankToken();

        seller.updateSeller(sellerName, bankBrand, bankName, bankToken);
        return "판매자 정보가 수정되었습니다.";
    }

    @Override
    @Transactional
    public UserDeleteDTO myDelete(Long userIdx) {
        userQueryService.findActiveUser(userIdx);
        UsersInformation userInfo = userQueryService.findActiveUserInfo(userIdx);

        sellerRepository.findByUsersIdxAndDel(userIdx, 0)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이디입니다."));

        UsersVerifications verification = UsersVerifications.builder()
                .usersIdx(userIdx)
                .purpose(UsersVerificationsPurPose.SELLER_DELETE)
                .code(randomCodeUtil.getCode())
                .expiresAt(LocalDateTime.now().plusMinutes(30))
                .status(UsersVerificationsStatus.PENDING)
                .build();
        userVerificationsRepository.save(verification);

        EmailMessage emailMessage = EmailMessage.builder()
                .to(userInfo.getMail())
                .subject("[GutJJeu] 판매자 탈퇴 인증번호 안내해 드립니다.")
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

        UsersVerifications verification = userVerificationsRepository.findTopByUsersIdxAndDelOrderByCreatedAtDesc(userIdx, 0)
                .orElseThrow(() -> new IllegalArgumentException("인증 요청 내역이 존재하지 않습니다."));

        if (verification.getPurpose() != UsersVerificationsPurPose.SELLER_DELETE) {
            throw new IllegalArgumentException("판매자 탈퇴 인증 코드가 아닙니다.");
        } else if (verification.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("인증 시간이 만료되었습니다.");
        } else if (!verification.getCode().equals(authCode)) {
            throw new IllegalArgumentException("인증번호가 일치하지 않습니다.");
        }

        verification.confirmVerification();
        Seller seller = sellerRepository.findByUsersIdxAndDel(userIdx, 0)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이디입니다."));

        seller.deleteSeller();
        return "판매자 탈퇴가 정상 처리되었습니다.";
    }
}