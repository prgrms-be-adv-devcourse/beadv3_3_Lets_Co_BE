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
import co.kr.user.util.AESUtil;
import co.kr.user.util.BCryptUtil;
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
public class SellerServiceImpl implements SellerService {
    private final UserRepository userRepository;
    private final SellerRepository sellerRepository;
    private final UserVerificationsRepository userVerificationsRepository;
    private final UserInformationRepository userInformationRepository;

    private final UserQueryServiceImpl userQueryServiceImpl;

    private final MailUtil mailUtil; // 이메일 발송 유틸
    private final RandomCodeUtil randomCodeUtil; // 인증번호 생성 유틸
    private final AESUtil aesUtil; // 양방향 암호화 (이름 복호화용)
    private final BCryptUtil bCryptUtil; // 단방향 암호화 (계좌 토큰용)

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
                .businessLicense(aesUtil.encrypt(sellerRegisterReq.getBusinessLicense()))
                .bankBrand(aesUtil.encrypt(sellerRegisterReq.getBankBrand()))
                .bankName(aesUtil.encrypt(sellerRegisterReq.getBankName()))
                .bankToken(aesUtil.encrypt(sellerRegisterReq.getBankToken()))
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

        // 이메일 템플릿 구성
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
                .to(users.getId())
                .subject("[GutJJeu] 판매자 등록 인증번호 안내해 드립니다.")
                .message(finalContent)
                .build();

        // 트랜잭션 커밋 후 이메일 발송
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                mailUtil.sendEmail(emailMessage, true);
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

        // 승인 완료 이메일 발송을 위해 사용자 이름 조회(복호화 필요)
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
                .to(users.getId())
                .subject("[GutJJeu] 판매자 등록 승인 안내해 드립니다.")
                .message(finalContent)
                .build();

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                mailUtil.sendEmail(emailMessage, true);
            }
        });

        return "판매자 등록이 완료되었습니다.";
    }
}