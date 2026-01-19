package co.kr.user.service;

import co.kr.user.DAO.SellerRepository;
import co.kr.user.DAO.UserInformationRepository;
import co.kr.user.DAO.UserRepository;
import co.kr.user.DAO.UserVerificationsRepository;
import co.kr.user.model.DTO.mail.EmailMessage;
import co.kr.user.model.DTO.seller.SellerRegisterDTO;
import co.kr.user.model.DTO.seller.SellerRegisterReq;
import co.kr.user.model.entity.Seller;
import co.kr.user.model.entity.Users;
import co.kr.user.model.entity.UsersInformation;
import co.kr.user.model.entity.UsersVerifications;
import co.kr.user.model.vo.UsersRole;
import co.kr.user.model.vo.UsersVerificationsPurPose;
import co.kr.user.model.vo.UsersVerificationsStatus;
import co.kr.user.util.AESUtil;
import co.kr.user.util.BCryptUtil;
import co.kr.user.util.EMailUtil;
import co.kr.user.util.RandomCodeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class SellerService implements SellerServiceImpl {
    private final UserRepository userRepository;
    private final SellerRepository sellerRepository;
    private final UserVerificationsRepository userVerificationsRepository;
    private final UserInformationRepository userInformationRepository;

    private final EMailUtil eMailUtil;
    private final RandomCodeUtil randomCodeUtil;
    private final AESUtil aesUtil;
    private final BCryptUtil bCryptUtil;

    @Override
    @Transactional
    public SellerRegisterDTO sellerRegister(Long userIdx, SellerRegisterReq sellerRegisterReq) {
        Users users = userRepository.findById(userIdx)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이디입니다."));

        if (users.getDel() == 1) {
            throw new IllegalStateException("탈퇴한 회원입니다.");
        }
        else if (users.getDel() == 2) {
            throw new IllegalStateException("인증을 먼저 시도해 주세요.");
        }

        Seller seller =Seller.builder()
            .sellerIdx(users.getUsersIdx())
            .businessLicense(sellerRegisterReq.getBusinessLicense())
            .bankBrand(sellerRegisterReq.getBankBrand())
            .bankName(sellerRegisterReq.getBankName())
            .bankToken(bCryptUtil.encode(sellerRegisterReq.getBankToken()))
            .build();

        sellerRepository.save(seller);

        UsersVerifications usersVerifications = co.kr.user.model.entity.UsersVerifications.builder()
                .usersIdx(users.getUsersIdx())
                .purPose(UsersVerificationsPurPose.SELLER_SIGNUP)
                .code(randomCodeUtil.getCode())
                .expiresAt(LocalDateTime.now().plusMinutes(30))
                .status(UsersVerificationsStatus.PENDING)
                .build();

        UsersVerifications savedUserVerifications = userVerificationsRepository.save(usersVerifications);

        String htmlTemplate = """
        <div style='background-color: #f6f7f9; padding: 40px 20px; font-family: "Apple SD Gothic Neo", "Malgun Gothic", sans-serif; line-height: 1.6;'>
            <div style='max-width: 500px; margin: 0 auto; background-color: #ffffff; border-radius: 8px; border: 1px solid #e0e0e0; overflow: hidden; box-shadow: 0 4px 12px rgba(0,0,0,0.05);'>
                
                <div style='background-color: #007bff; padding: 20px; text-align: center;'>
                    <h1 style='color: #ffffff; font-size: 20px; margin: 0; font-weight: 600;'>GutJJeu</h1>
                </div>
        
                <div style='padding: 30px;'>
                    <h2 style='color: #333; font-size: 22px; margin-top: 0; text-align: center;'>판매자 등록 인증 안내</h2>
                    <p style='color: #555; font-size: 16px; margin-bottom: 20px; text-align: center;'>
                        안녕하세요.<br>
                        GutJJeu 판매자 입점을 환영합니다.<br>
                        본인 확인을 위해 아래 인증번호를 입력해 주세요.
                    </p>
                    
                    <div style='background-color: #f0f4f8; padding: 20px; text-align: center; border-radius: 6px; margin: 30px 0; border: 1px dashed #007bff;'>
                        <span style='font-size: 18px; font-weight: bold; color: #007bff; word-break: break-all; display: inline-block;'>
                            %s
                        </span>
                    </div>
                    
                    <p style='color: #888; font-size: 13px; text-align: center; margin-top: 20px;'>
                        * 이 인증번호는 <strong>30분 동안만 유효</strong>합니다.<br>
                        * 인증이 완료되어야 판매자 정보를 입력할 수 있습니다.
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

        EmailMessage emailMessage = EmailMessage.builder()
                .to(users.getID())
                .subject("[GutJJeu] 판매자 등록 인증번호 안내해 드립니다.")
                .message(finalContent)
                .build();

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                eMailUtil.sendEmail(emailMessage, true);
            }
        });

        SellerRegisterDTO sellerRegisterDTO = new SellerRegisterDTO();
        sellerRegisterDTO.setID(users.getID());
        sellerRegisterDTO.setCertificationTime(savedUserVerifications.getExpiresAt());

        return sellerRegisterDTO;
    }

    @Override
    @Transactional
    public String sellerRegisterCheck(Long userIdx, String authCode) {
        Users users = userRepository.findById(userIdx)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이디입니다."));

        if (users.getDel() == 1) {
            throw new IllegalStateException("탈퇴한 회원입니다.");
        }
        else if (users.getDel() == 2) {
            throw new IllegalStateException("인증을 먼저 시도해 주세요.");
        }

        UsersVerifications verification = userVerificationsRepository.findTopByUsersIdxOrderByCreatedAtDesc(users.getUsersIdx())
                .orElseThrow(() -> new IllegalArgumentException("인증 요청 내역이 존재하지 않습니다."));

        if (!Objects.equals(verification.getUsersIdx(), users.getUsersIdx())) {
            throw new IllegalArgumentException("잘못된 인증 요청입니다.");
        }

        if (verification.getPurPose() != UsersVerificationsPurPose.SELLER_SIGNUP) {
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

        Seller seller = sellerRepository.findBySellerIdx(users.getUsersIdx())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이디입니다."));

        users.setRole(UsersRole.SELLER);

        seller.confirmVerification();

        UsersInformation usersInformation = userInformationRepository.findById(users.getUsersIdx())
                .orElseThrow();

        String htmlTemplate = """
        <div style='background-color: #f6f7f9; padding: 40px 20px; font-family: "Apple SD Gothic Neo", "Malgun Gothic", sans-serif; line-height: 1.6;'>
            <div style='max-width: 500px; margin: 0 auto; background-color: #ffffff; border-radius: 8px; border: 1px solid #e0e0e0; overflow: hidden; box-shadow: 0 4px 12px rgba(0,0,0,0.05);'>
                
                <div style='background-color: #007bff; padding: 20px; text-align: center;'>
                    <h1 style='color: #ffffff; font-size: 20px; margin: 0; font-weight: 600;'>GutJJeu</h1>
                </div>
        
                <div style='padding: 30px;'>
                    <h2 style='color: #333; font-size: 22px; margin-top: 0; text-align: center;'>판매자 등록 완료 안내</h2>
                    <p style='color: #555; font-size: 16px; margin-bottom: 20px; text-align: center;'>
                        축하합니다!<br>
                        신청하신 판매자 권한 승인이 완료되었습니다.
                    </p>
                    
                    <div style='background-color: #f0f4f8; padding: 20px; text-align: center; border-radius: 6px; margin: 30px 0; border: 1px dashed #007bff;'>
                        <span style='font-size: 18px; font-weight: bold; color: #007bff; word-break: break-all; display: inline-block;'>
                            %s
                        </span>
                    </div>
                    
                    <p style='color: #888; font-size: 13px; text-align: center; margin-top: 20px;'>
                        * 위 계정(상점명)으로 판매 활동을 시작하실 수 있습니다.<br>
                        * 판매자 센터에 로그인하여 상품을 등록해 보세요.
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

        String finalContent = htmlTemplate.formatted(aesUtil.decrypt(usersInformation.getName()));

        EmailMessage emailMessage = EmailMessage.builder()
                .to(users.getID())
                .subject("[GutJJeu] 판매자 등록 승인 안내해 드립니다.")
                .message(finalContent)
                .build();

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                eMailUtil.sendEmail(emailMessage, true);
            }
        });

        return "판매자 등록이 완료되었습니다.";
    }
}