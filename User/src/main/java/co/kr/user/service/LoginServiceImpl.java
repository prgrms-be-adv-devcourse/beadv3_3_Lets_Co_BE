package co.kr.user.service;

import co.kr.user.model.DTO.login.LoginDTO;
import co.kr.user.model.DTO.login.LoginReq;

public interface LoginServiceImpl {
    LoginDTO login(LoginReq loginReq);

    String logout(String refreshToken);
}
