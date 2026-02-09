package co.kr.user.service.Impl;

import co.kr.user.dao.UserRepository;
import co.kr.user.model.dto.auth.TokenDto;
import co.kr.user.model.entity.Users;
import co.kr.user.service.AuthService;
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

/**
 * 인증(Authentication) 및 권한(Authorization) 관련 공통 로직을 처리하는 서비스 클래스입니다.
 * 사용자 권한 조회와 JWT 토큰 재발급(Refresh) 기능을 제공합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;


    private final UserQueryServiceImpl userQueryServiceImpl;

    private final JWTUtil jwtUtil; // JWT 생성 및 검증 유틸리티
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * Access Token 만료 시, Refresh Token을 사용하여 토큰을 재발급하는 메서드입니다.
     * Refresh Token Rotation(RTR) 정책을 적용하여, Refresh Token이 만료 임박한 경우 함께 교체합니다.
     * 계정이 정지된 상태인 경우 재발급을 거부합니다.
     *
     * @param refreshToken 클라이언트로부터 전달받은 Refresh Token
     * @return TokenDto (새로운 AccessToken, [선택]새로운 RefreshToken)
     */
    @Override
    @Transactional
    public TokenDto refreshToken(String refreshToken) {
        // 1. BL(Blacklist)에 토큰이 있는지 확인
        Boolean isBlacklisted = redisTemplate.hasKey("BL:" + refreshToken);
        if (Boolean.TRUE.equals(isBlacklisted)) {
            throw new IllegalStateException("이미 로그아웃되거나 폐기된 토큰입니다. 다시 로그인해주세요.");
        }

        Claims claims = jwtUtil.getRefreshTokenClaims(refreshToken);

        //리팩토링 과정에서 claims.getSubject()에 타입변환이 필요함을 확인하여 Long.valueOf()를 붙임
        Users users = userQueryServiceImpl.findActiveUser(Long.valueOf(claims.getSubject()));

        // 계정 정지(Lock) 여부 확인
        if (users.getLockedUntil() != null) {
            if (users.getLockedUntil().isAfter(users.getUpdatedAt())) {
                throw new IllegalStateException("계정이 " + users.getLockedUntil() + "까지 정지되었습니다.");
            }
        }

        // 새로운 Access Token 발급
        String newAccessToken = jwtUtil.createAccessToken(users.getUsersIdx(), users.getCreatedAt(), users.getUpdatedAt());

        String newRefreshToken = null;

        // 6. Refresh Token 만료 임박 확인 (Rotation 정책)
        if (TokenUtil.isTokenExpiringSoon(refreshToken)) {
            // [기존 토큰 처리] RT 삭제 및 BL 탑재 (영구 저장)
            redisTemplate.delete("RT:" + users.getUsersIdx());
            redisTemplate.opsForValue().set("BL:" + refreshToken, "rotated");

            // [새 토큰 발급]
            newRefreshToken = jwtUtil.createRefreshToken(users.getUsersIdx());

            // [새 RT 추가] Redis 저장 (7일 만료)
            redisTemplate.opsForValue().set(
                    "RT:" + users.getUsersIdx(),
                    newRefreshToken,
                    Duration.ofSeconds(CookieUtil.REFRESH_TOKEN_EXPIRY)
            );
        }

        TokenDto tokenDto = new TokenDto();
        tokenDto.setAccessToken(newAccessToken);
        tokenDto.setRefreshToken(newRefreshToken); // 교체되지 않았으면 null일 수 있음 (클라이언트 처리 필요)

        return tokenDto;
    }
}