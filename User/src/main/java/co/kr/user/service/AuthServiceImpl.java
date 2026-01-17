package co.kr.user.service;

import co.kr.user.model.DTO.auth.TokenDto;
import co.kr.user.model.vo.UsersRole;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthServiceImpl {

    UsersRole getRole(Long userIdx);

    String accessToken(String AccessToken);

    // 반환 타입을 String -> TokenDto로 변경
    TokenDto refreshToken(String refreshToken);
}
