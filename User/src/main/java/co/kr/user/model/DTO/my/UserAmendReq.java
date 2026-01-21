package co.kr.user.model.DTO.my;

import lombok.Data;

@Data
public class UserAmendReq {
    private String name;
    private String phoneNumber;
    private String birth;
    private String grade;
}