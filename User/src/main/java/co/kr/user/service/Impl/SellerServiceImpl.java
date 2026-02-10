package co.kr.user.service.Impl;

import co.kr.user.dao.SellerRepository;
import co.kr.user.dao.UserInformationRepository;
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
import co.kr.user.util.BCryptUtil;
import co.kr.user.util.EmailTemplateProvider;
import co.kr.user.util.MailUtil;
import co.kr.user.util.RandomCodeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SellerServiceImpl implements SellerService {
    private final SellerRepository sellerRepository;
    private final UserVerificationsRepository userVerificationsRepository;
    private final UserInformationRepository userInformationRepository;

    private final UserQueryServiceImpl userQueryServiceImpl;

    private final MailUtil mailUtil;
    private final RandomCodeUtil randomCodeUtil;
    private final BCryptUtil bCryptUtil;
    private final EmailTemplateProvider emailTemplateProvider;

    private static final String BANK_TOKEN_PATTERN = "^[0-9-]+$";

    @Override
    @Transactional
    public SellerRegisterDTO sellerRegister(Long userIdx, SellerRegisterReq sellerRegisterReq) {
        Users users = userQueryServiceImpl.findActiveUser(userIdx);
        UsersInformation usersInformation = userInformationRepository.findByUsersIdxAndDel(users.getUsersIdx(), 0)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        if (sellerRepository.existsByUsersIdxAndDel(users.getUsersIdx(), 0)) {
            throw new IllegalArgumentException("이미 판매자 등록이 완료된 사용자입니다.");
        }

        Seller seller = Seller.builder()
                .usersIdx(users.getUsersIdx())
                .sellerName(sellerRegisterReq.getSellerName())
                .businessLicense(sellerRegisterReq.getBusinessLicense())
                .bankBrand(sellerRegisterReq.getBankBrand())
                .bankName(sellerRegisterReq.getBankName())
                .bankToken(bCryptUtil.encode(sellerRegisterReq.getBankToken()))
                .build();

        sellerRepository.save(seller);

        UsersVerifications usersVerifications = co.kr.user.model.entity.UsersVerifications.builder()
                .usersIdx(usersInformation.getUsersIdx())
                .purpose(UsersVerificationsPurPose.SELLER_SIGNUP)
                .code(randomCodeUtil.getCode())
                .expiresAt(LocalDateTime.now().plusMinutes(30)) // 유효기간 30분
                .status(UsersVerificationsStatus.PENDING)
                .build();

        UsersVerifications savedUserVerifications = userVerificationsRepository.save(usersVerifications);

        String finalContent = emailTemplateProvider.getSellerRegisterTemplate(savedUserVerifications.getCode());
        EmailMessage emailMessage = EmailMessage.builder()
                .to(usersInformation.getMail())
                .subject("[GutJJeu] 판매자 등록 인증번호 안내해 드립니다.")
                .message(finalContent)
                .build();

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                mailUtil.sendEmail(emailMessage, true);
            }
        });

        SellerRegisterDTO sellerRegisterDTO = new SellerRegisterDTO();
        sellerRegisterDTO.setMail(usersInformation.getMail());
        sellerRegisterDTO.setCertificationTime(savedUserVerifications.getExpiresAt());

        return sellerRegisterDTO;
    }

    @Override
    @Transactional
    public String sellerRegisterCheck(Long userIdx, String authCode) {
        Users users = userQueryServiceImpl.findActiveUser(userIdx);
        UsersVerifications verification = userVerificationsRepository.findTopByUsersIdxAndDelOrderByCreatedAtDesc(users.getUsersIdx(), 0)
                .orElseThrow(() -> new IllegalArgumentException("인증 요청 내역이 존재하지 않습니다."));

        if (!Objects.equals(verification.getUsersIdx(), users.getUsersIdx())) {
            throw new IllegalArgumentException("잘못된 인증 요청입니다.");
        }

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

        verification.confirmVerification();

        Seller seller = sellerRepository.findByUsersIdxAndDel(users.getUsersIdx(), 2)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이디입니다."));

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

    @Override
    public SellerProfileDTO my(Long userIdx) {
        Users users = userQueryServiceImpl.findActiveUser(userIdx);
        Seller seller = sellerRepository.findByUsersIdxAndDel(users.getUsersIdx(), 0)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이디입니다."));

        SellerProfileDTO sellerProfileDTO = new SellerProfileDTO();
        sellerProfileDTO.setSellerName(seller.getSellerName());
        sellerProfileDTO.setBusinessLicense(seller.getBusinessLicense());
        sellerProfileDTO.setBankBrand(seller.getBankBrand());
        sellerProfileDTO.setBankName(seller.getBankName());
        sellerProfileDTO.setCreateAt(seller.getCreatedAt());
        sellerProfileDTO.setUpdateAt(seller.getUpdatedAt());

        return sellerProfileDTO;
    }

    @Override
    @Transactional
    public String myAmend(Long userIdx, SellerAmendReq sellerAmendReq) {
        // 1. 엔티티 조회
        Users users = userQueryServiceImpl.findActiveUser(userIdx);
        Seller seller = sellerRepository.findByUsersIdxAndDel(users.getUsersIdx(), 0)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 판매자입니다."));

        // 2. 각 필드별 검증 및 값 결정 (값이 없으면 기존 값 유지)

        // 판매자명
        String sellerName = seller.getSellerName();
        if (StringUtils.hasText(sellerAmendReq.getSellerName())) {
            String trimmed = sellerAmendReq.getSellerName().trim();
            if (trimmed.length() < 2 || trimmed.length() > 50) throw new IllegalArgumentException("유효하지 않은 이름입니다.");
            sellerName = trimmed;
        }

        // 은행 브랜드
        String bankBrand = seller.getBankBrand();
        if (StringUtils.hasText(sellerAmendReq.getBankBrand())) {
            String trimmed = sellerAmendReq.getBankBrand().trim();
            if (trimmed.length() < 2 || trimmed.length() > 50) throw new IllegalArgumentException("유효하지 않은 은행 브랜드입니다.");
            bankBrand = trimmed;
        }

        // 예금주 (기존 sellerName으로 잘못 할당되었던 로직 수정)
        String bankName = seller.getBankName();
        if (StringUtils.hasText(sellerAmendReq.getBankName())) {
            String trimmed = sellerAmendReq.getBankName().trim();
            if (trimmed.length() > 20) throw new IllegalArgumentException("유효하지 않은 예금주명입니다.");
            bankName = trimmed;
        }

        // 뱅크 토큰 (정규식 검증 포함)
        String bankToken = seller.getBankToken(); // 기본값은 기존의 암호화된 토큰
        if (StringUtils.hasText(sellerAmendReq.getBankToken())) {
            String trimmed = sellerAmendReq.getBankToken().trim();

            // 길이 및 정규식 검사
            if (trimmed.length() < 10 || trimmed.length() > 30) throw new IllegalArgumentException("토큰 길이는 10~30자여야 합니다.");
            if (!trimmed.matches("^[0-9-]+$")) throw new IllegalArgumentException("토큰은 숫자와 하이픈만 가능합니다.");

            // 새 값이 들어온 경우에만 암호화
            bankToken = bCryptUtil.encode(trimmed);
        }

        // 3. 한 번에 업데이트
        seller.updateSeller(
                sellerName,
                bankBrand,
                bankName,
                bankToken
        );

        return "판매자 정보가 수정되었습니다.";
    }

    @Override
    public UserDeleteDTO myDelete(Long userIdx) {
        Users users = userQueryServiceImpl.findActiveUser(userIdx);
        UsersInformation usersInformation = userInformationRepository.findByUsersIdxAndDel(users.getUsersIdx(), 0)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        sellerRepository.findByUsersIdxAndDel(users.getUsersIdx(), 0)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이디입니다."));

        UsersVerifications usersVerifications = co.kr.user.model.entity.UsersVerifications.builder()
                .usersIdx(users.getUsersIdx())
                .purpose(UsersVerificationsPurPose.SELLER_DELETE)
                .code(randomCodeUtil.getCode())
                .expiresAt(LocalDateTime.now().plusMinutes(30))
                .status(UsersVerificationsStatus.PENDING)
                .build();

        UsersVerifications savedUserVerifications = userVerificationsRepository.save(usersVerifications);

        String finalContent = emailTemplateProvider.getDeleteSellerTemplate(savedUserVerifications.getCode());

        EmailMessage emailMessage = EmailMessage.builder()
                .to(usersInformation.getMail())
                .subject("[GutJJeu] 판매자 탈퇴 인증번호 안내해 드립니다.")
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
    public String myDelete(Long userIdx, String authCode) {
        Users users = userQueryServiceImpl.findActiveUser(userIdx);

        UsersVerifications verification = userVerificationsRepository.findTopByUsersIdxAndDelOrderByCreatedAtDesc(users.getUsersIdx(), 0)
                .orElseThrow(() -> new IllegalArgumentException("인증 요청 내역이 존재하지 않습니다."));

        if (verification.getPurpose() != UsersVerificationsPurPose.SELLER_DELETE) {
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

        Seller seller = sellerRepository.findByUsersIdxAndDel(users.getUsersIdx(), 0)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이디입니다."));

        seller.deleteSeller();

        return "판매자 탈퇴가 정상 처리되었습니다.";
    }
}