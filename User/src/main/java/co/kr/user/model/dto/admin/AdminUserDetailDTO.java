package co.kr.user.model.dto.admin;

import co.kr.user.model.vo.UsersRole;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AdminUserDetailDTO {
    private String ID;
    private LocalDateTime lockedUntil;
    private UsersRole role;
    private BigDecimal balance;
    private LocalDateTime agreeTermsAt;
    private LocalDateTime agreePrivacyAt;
    private LocalDateTime agreeMarketingAt;
    private String name;
    private String phoneNumber;
    private String birth;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
