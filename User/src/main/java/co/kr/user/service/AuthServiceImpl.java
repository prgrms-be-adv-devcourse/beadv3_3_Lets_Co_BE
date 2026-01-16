package co.kr.user.service;

import co.kr.user.model.vo.UsersRole;

public interface AuthServiceImpl {

    UsersRole getRole(Long userIdx);

    String renewAccessToken(String refreshToken);

    String renewRefreshToken(String refreshToken);
}
