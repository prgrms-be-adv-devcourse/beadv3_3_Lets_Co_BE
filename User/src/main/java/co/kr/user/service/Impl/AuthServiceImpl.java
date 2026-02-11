package co.kr.user.service.Impl;

import co.kr.user.model.dto.auth.TokenDto;
import co.kr.user.model.entity.Users;
import co.kr.user.service.AuthService;
import co.kr.user.service.UserQueryService;
import co.kr.user.util.CookieUtil;
import co.kr.user.util.JWTUtil;
import co.kr.user.util.TokenUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserQueryService userQueryService;

    private final RedisTemplate<String, Object> redisTemplate;

    private final JWTUtil jwtUtil;

    @Override
    @Transactional
    public TokenDto refreshToken(String refreshToken) {
        if (Boolean.TRUE.equals(redisTemplate.hasKey("BL:" + refreshToken))) {
            throw new IllegalStateException("이미 로그아웃되거나 폐기된 토큰입니다. 다시 로그인해주세요.");
        }

        Claims claims = jwtUtil.getRefreshTokenClaims(refreshToken);
        Users user = userQueryService.findActiveUser(Long.valueOf(claims.getSubject()));

        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now())) {
            throw new IllegalStateException("계정이 " + user.getLockedUntil() + "까지 정지되었습니다.");
        }

        String newAccessToken = jwtUtil.createAccessToken(user.getUsersIdx(), user.getCreatedAt(), user.getUpdatedAt());
        String newRefreshToken = null;

        if (TokenUtil.isTokenExpiringSoon(refreshToken)) {
            redisTemplate.delete("RT:" + user.getUsersIdx());
            redisTemplate.opsForValue().set("BL:" + refreshToken, "rotated");

            newRefreshToken = jwtUtil.createRefreshToken(user.getUsersIdx());
            redisTemplate.opsForValue().set(
                    "RT:" + user.getUsersIdx(),
                    newRefreshToken,
                    Duration.ofSeconds(CookieUtil.REFRESH_TOKEN_EXPIRY)
            );
        }

        TokenDto tokenDto = new TokenDto();
        tokenDto.setAccessToken(newAccessToken);
        tokenDto.setRefreshToken(newRefreshToken);

        return tokenDto;
    }
}