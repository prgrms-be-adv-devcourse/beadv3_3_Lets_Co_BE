package co.kr.user.model.dto.my;

import co.kr.user.model.vo.UsersInformationGender;
import lombok.*;

import java.time.LocalDateTime;

@Data
public class UserProfileDTO {
    private String name;
    private String phoneNumber;
    private String birth;
    private UsersInformationGender gender;
    private String mail;
    private LocalDateTime agreeMarketingAt;
}