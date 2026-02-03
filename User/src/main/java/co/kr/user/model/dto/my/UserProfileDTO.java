package co.kr.user.model.dto.my;

import co.kr.user.model.vo.UsersInformationGender;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UserProfileDTO {

    private UsersInformationGender gender;
    private BigDecimal balance;
    private String name;
    private String phoneNumber;
    private String birth;
    private LocalDateTime agreeMarketingAt;
}