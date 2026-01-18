package co.kr.user.service;

import co.kr.user.DAO.UserInformationRepository;
import co.kr.user.DAO.UserRepository;
import co.kr.user.DAO.UserVerificationsRepository;
import co.kr.user.model.DTO.mail.EmailMessage;
import co.kr.user.model.DTO.my.UserAmendReq;
import co.kr.user.model.DTO.my.UserDeleteDTO;
import co.kr.user.model.DTO.my.UserProfileDTO;
import co.kr.user.model.DTO.my.UserDTO;
import co.kr.user.model.entity.Users;
import co.kr.user.model.entity.UsersInformation;
import co.kr.user.model.entity.UsersVerifications;
import co.kr.user.model.vo.UsersVerificationsPurPose;
import co.kr.user.model.vo.UsersVerificationsStatus;
import co.kr.user.util.AESUtil;
import co.kr.user.util.EMailUtil;
import co.kr.user.util.RandomCodeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService implements UserServiceImpl{
    private final UserRepository userRepository;
    private final UserInformationRepository userInformationRepository;

    private final AESUtil aesUtil;
    private final RandomCodeUtil randomCodeUtil;
    private final UserVerificationsRepository userVerificationsRepository;
    private final EMailUtil eMailUtil;

    public UserDTO my(Long userIdx) {
        Users users = userRepository.findById(userIdx)
                .orElseThrow();

        if (users.getDel() == 1) {
            throw new IllegalStateException("탈퇴한 회원입니다.");
        }
        else if (users.getDel() == 2) {
            throw new IllegalStateException("인증을 먼저 시도해 주세요.");
        }

        UserDTO userDTO = new UserDTO();
        userDTO.setID(users.getID());
        userDTO.setRole(users.getRole());
        userDTO.setBalance(users.getBalance());
        userDTO.setCreatedAt(users.getCreatedAt());

        return userDTO;
    }

    public UserProfileDTO myDetails(Long userIdx) {
        Users users = userRepository.findById(userIdx)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이디입니다."));

        if (users.getDel() == 1) {
            throw new IllegalStateException("탈퇴한 회원입니다.");
        }
        else if (users.getDel() == 2) {
            throw new IllegalStateException("인증을 먼저 시도해 주세요.");
        }

        UsersInformation userInfo = userInformationRepository.findById(userIdx)
                .orElseThrow(() -> new IllegalArgumentException("상세 회원 정보를 찾을 수 없습니다. UserID: " + userIdx));

        if (userInfo.getDel() == 1) {
            throw new IllegalStateException("탈퇴한 회원입니다.");
        }

        UserProfileDTO userProfileDTO = new UserProfileDTO();
        userProfileDTO.setName(aesUtil.decrypt(userInfo.getName()));
        userProfileDTO.setPhoneNumber(aesUtil.decrypt(userInfo.getPhoneNumber()));
        userProfileDTO.setBirth(aesUtil.decrypt(userInfo.getBirth()));
        userProfileDTO.setGrade("STANDARD");

        return userProfileDTO;
    }

    @Override
    @Transactional
    public UserDeleteDTO myDelete(Long userIdx) {
        Users users = userRepository.findById(userIdx)
                .orElseThrow();

        if (users.getDel() == 1) {
            throw new IllegalStateException("탈퇴한 회원입니다.");
        }
        else if (users.getDel() == 2) {
            throw new IllegalStateException("인증을 먼저 시도해 주세요.");
        }

        UsersVerifications usersVerifications = co.kr.user.model.entity.UsersVerifications.builder()
                .usersIdx(users.getUsersIdx())
                .purPose(UsersVerificationsPurPose.DELETE_ACCOUNT)
                .code(randomCodeUtil.getCode())
                .expiresAt(LocalDateTime.now().plusMinutes(30))
                .status(UsersVerificationsStatus.PENDING)
                .build();

        UsersVerifications savedUserVerifications = userVerificationsRepository.save(usersVerifications);

        String accountDeletionTemplate = """
            <div style='background-color: #f6f7f9; padding: 40px 20px; font-family: "Apple SD Gothic Neo", "Malgun Gothic", sans-serif; line-height: 1.6;'>
                <div style='max-width: 500px; margin: 0 auto; background-color: #ffffff; border-radius: 8px; border: 1px solid #e0e0e0; overflow: hidden; box-shadow: 0 4px 12px rgba(0,0,0,0.05);'>
                    
                    <div style='background-color: #007bff; padding: 20px; text-align: center;'>
                        <h1 style='color: #ffffff; font-size: 20px; margin: 0; font-weight: 600;'>GutJJeu</h1>
                    </div>
            
                    <div style='padding: 30px;'>
                        <h2 style='color: #333; font-size: 22px; margin-top: 0; text-align: center;'>회원탈퇴 인증번호</h2>
                        <p style='color: #555; font-size: 16px; margin-bottom: 20px; text-align: center;'>
                            안녕하세요.<br>
                            회원탈퇴 처리를 위해 아래 인증번호를 입력해 주세요.
                        </p>
                        
                        <div style='background-color: #f0f4f8; padding: 20px; text-align: center; border-radius: 6px; margin: 30px 0; border: 1px dashed #007bff;'>
                            <span style='font-size: 18px; font-weight: bold; color: #007bff; word-break: break-all; display: inline-block;'>
                                %s
                            </span>
                        </div>
                        
                        <p style='color: #888; font-size: 13px; text-align: center; margin-top: 20px;'>
                            * 이 인증번호는 <strong>30분 동안만 유효</strong>합니다.<br>
                            * 본인이 요청하지 않은 경우, 절대 타인에게 공유하지 마세요.
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

        String finalContent = accountDeletionTemplate.formatted(savedUserVerifications.getCode());

        EmailMessage emailMessage = EmailMessage.builder()
                .to(users.getID())
                .subject("[GutJJeu] 회원탈퇴 인증번호 안내해 드립니다.")
                .message(finalContent)
                .build();

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                eMailUtil.sendEmail(emailMessage, true);
            }
        });

        UserDeleteDTO userDeleteDTO = new UserDeleteDTO();
        userDeleteDTO.setID(users.getID());
        userDeleteDTO.setCertificationTime(LocalDateTime.now());

        return userDeleteDTO;
    }

    @Override
    @Transactional
    public String myDelete(Long userIdx, String authCode) {
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

        if (verification.getPurPose() != UsersVerificationsPurPose.DELETE_ACCOUNT) {
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

        users.del();

        return "회원 탈퇴가 정상 처리되었습니다.";
    }

    @Override
    @Transactional
    public UserAmendReq myAmend(Long userIdx, UserAmendReq userAmendReq) {
        Users users = userRepository.findById(userIdx)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이디입니다."));

        if (users.getDel() == 1) {
            throw new IllegalStateException("탈퇴한 회원입니다.");
        }
        else if (users.getDel() == 2) {
            throw new IllegalStateException("인증을 먼저 시도해 주세요.");
        }

        UsersInformation userInfo = userInformationRepository.findById(userIdx)
                .orElseThrow(() -> new IllegalArgumentException("상세 회원 정보를 찾을 수 없습니다. UserID: " + userIdx));

        if (userInfo.getDel() == 1) {
            throw new IllegalStateException("탈퇴한 회원입니다.");
        }

        UserAmendReq amend = new UserAmendReq();
        amend.setName(userInfo.getName());
        amend.setPhoneNumber(userInfo.getPhoneNumber());
        amend.setBirth(userInfo.getBirth());
        amend.setGrade("STANDARD");


        log.info(amend.toString());
        if (!userAmendReq.getName().isEmpty()) {
            amend.setName(aesUtil.encrypt(userAmendReq.getName()));
        }
        if (!userAmendReq.getPhoneNumber().isEmpty()) {
            amend.setPhoneNumber(aesUtil.encrypt(userAmendReq.getPhoneNumber()));
        }
        if (!userAmendReq.getBirth().isEmpty()) {
            amend.setBirth(aesUtil.encrypt(userAmendReq.getBirth()));
        }
        log.info(amend.toString());

        userInfo.amend(amend.getName(), amend.getPhoneNumber(), amend.getBirth());

        return amend;
    }
}