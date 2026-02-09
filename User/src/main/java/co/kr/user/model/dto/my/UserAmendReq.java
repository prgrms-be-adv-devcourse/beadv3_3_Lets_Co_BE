package co.kr.user.model.dto.my;

import co.kr.user.model.vo.UsersInformationGender;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserAmendReq {
    private UsersInformationGender gender;
    private String name;
    private String phoneNumber;
    private String birth;
    private LocalDateTime agreeMarketingAt;
}