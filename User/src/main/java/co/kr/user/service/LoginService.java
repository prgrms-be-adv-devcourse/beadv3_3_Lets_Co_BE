package co.kr.user.service;

import co.kr.user.model.dto.login.LoginDTO;
import co.kr.user.model.dto.login.LoginReq;

public interface LoginService {
    LoginDTO login(LoginReq loginReq);

    String logout(String refreshToken);
}