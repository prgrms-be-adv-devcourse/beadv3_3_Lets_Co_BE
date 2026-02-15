package co.kr.user.model.dto.my;

import co.kr.user.model.vo.UsersInformationGender;
import lombok.Data;

@Data
public class UserAmendDTO {
    private String mail;
    private UsersInformationGender gender;
    private String name;
    private String phoneNumber;
    private String birth;
}