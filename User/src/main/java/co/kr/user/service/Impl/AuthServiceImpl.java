package co.kr.user.service.Impl;

import co.kr.user.model.dto.auth.TokenDto;
import co.kr.user.model.entity.Users;
import co.kr.user.service.AuthService;
import co.kr.user.service.UserQueryService;
import co.kr.user.util.JWTUtil;
import co.kr.user.util.TokenUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {
    private final UserQueryService userQueryService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final JWTUtil jwtUtil;

    @Value("${custom.security.redis.rt-prefix}")
    private String rtPrefix;

    @Value("${custom.security.redis.bl-prefix}")
    private String blPrefix;

    @Override
    @Transactional
    public TokenDto refreshToken(String refreshToken) {
        if (refreshToken == null || !jwtUtil.validateRefreshToken(refreshToken)) {
            throw new IllegalArgumentException("유효하지 않은 Refresh Token입니다.");
        }

        if (Boolean.TRUE.equals(redisTemplate.hasKey(blPrefix + refreshToken))) {
            throw new IllegalStateException("이미 로그아웃되거나 폐기된 토큰입니다. 다시 로그인해주세요.");
        }

        Long userIdx = jwtUtil.getUserIdxFromToken(refreshToken, false);
        Users user = userQueryService.findActiveUser(userIdx);

        if (user.isLocked()) {
            throw new IllegalStateException("계정이 잠금 상태입니다. 해제 일시: " + user.getLockedUntil());
        }

        String newAccessToken = jwtUtil.createAccessToken(user.getUsersIdx(), user.getCreatedAt(), user.getUpdatedAt());
        String newRefreshToken = null;

        if (TokenUtil.isTokenExpiringSoon(refreshToken)) {
            redisTemplate.opsForValue().set(blPrefix + refreshToken, "rotated");

            newRefreshToken = jwtUtil.createRefreshToken(user.getUsersIdx());
            redisTemplate.opsForValue().set(rtPrefix + userIdx, newRefreshToken);
        }

        TokenDto tokenDto = new TokenDto();
        tokenDto.setAccessToken(newAccessToken);
        tokenDto.setRefreshToken(newRefreshToken);

        return tokenDto;
    }
}