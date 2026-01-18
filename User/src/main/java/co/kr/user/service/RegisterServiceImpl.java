package co.kr.user.service;

import co.kr.user.model.DTO.register.RegisterDTO;
import co.kr.user.model.DTO.register.RegisterReq;

public interface RegisterServiceImpl {
    String checkDuplicate(String email);

    RegisterDTO signup(RegisterReq registerReq);

    String signupAuthentication(String code);
}