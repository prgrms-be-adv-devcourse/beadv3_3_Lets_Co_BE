package co.kr.user.service;

import co.kr.user.model.DTO.auth.TokenDto;
import co.kr.user.model.vo.UsersRole;

public interface AuthServiceImpl {
    UsersRole getRole(Long userIdx);

    TokenDto refreshToken(String refreshToken);
}