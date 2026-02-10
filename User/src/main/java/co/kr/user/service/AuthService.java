package co.kr.user.service;

import co.kr.user.model.dto.auth.TokenDto;

public interface AuthService {
    TokenDto refreshToken(String refreshToken);
}