package co.kr.user.model.dto.my;

import lombok.Data;

@Data
public class UserAmendReq {
    private String name;
    private String phoneNumber;
    private String birth;
    private String grade;
}