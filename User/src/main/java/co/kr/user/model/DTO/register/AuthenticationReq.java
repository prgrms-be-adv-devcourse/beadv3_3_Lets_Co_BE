package co.kr.user.model.DTO.register;

import lombok.Data;

@Data
public class AuthenticationReq {
    private String ID;
    private String code;
}
