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

/**
 * AuthService 인터페이스의 구현체입니다.
 * JWT 토큰 재발급(Refresh) 로직을 처리합니다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {
    private final UserQueryService userQueryService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final JWTUtil jwtUtil;

    // application.yml에서 설정된 Redis Key Prefix
    @Value("${custom.security.redis.rt-prefix}")
    private String rtPrefix; // Refresh Token 저장 키 접두사 (예: "RT:")

    @Value("${custom.security.redis.bl-prefix}")
    private String blPrefix; // Blacklist 저장 키 접두사 (예: "BL:")

    /**
     * Refresh Token을 이용해 Access Token을 재발급합니다.
     * 보안 강화를 위해 RTR(Refresh Token Rotation) 전략을 사용할 수도 있습니다.
     * @param refreshToken 클라이언트가 보낸 리프레시 토큰
     * @return 새로운 토큰 정보가 담긴 DTO
     */
    @Override
    @Transactional // 토큰 갱신 시 Redis 쓰기 작업 등이 발생하므로 트랜잭션 필요
    public TokenDto refreshToken(String refreshToken) {
        // 1. 토큰 자체의 유효성 검사 (서명 위조 여부 등)
        if (refreshToken == null || !jwtUtil.validateRefreshToken(refreshToken)) {
            throw new IllegalArgumentException("유효하지 않은 Refresh Token입니다.");
        }

        // 2. 해당 토큰이 블랙리스트에 있는지 확인 (로그아웃되었거나 탈퇴한 토큰인지)
        if (Boolean.TRUE.equals(redisTemplate.hasKey(blPrefix + refreshToken))) {
            throw new IllegalStateException("이미 로그아웃되거나 폐기된 토큰입니다. 다시 로그인해주세요.");
        }

        // 3. 토큰에서 사용자 식별자(userIdx) 추출
        Long userIdx = jwtUtil.getUserIdxFromToken(refreshToken, false);
        // 4. 사용자 상태 확인 (잠금 상태 등)
        Users user = userQueryService.findActiveUser(userIdx);

        if (user.isLocked()) {
            throw new IllegalStateException("계정이 잠금 상태입니다. 해제 일시: " + user.getLockedUntil());
        }

        // 5. 새로운 Access Token 발급
        String newAccessToken = jwtUtil.createAccessToken(user.getUsersIdx(), user.getCreatedAt(), user.getUpdatedAt());
        String newRefreshToken = null;

        // 6. Refresh Token Rotation (선택적): 만료가 임박한 경우 리프레시 토큰도 함께 교체
        if (TokenUtil.isTokenExpiringSoon(refreshToken)) {
            // 기존 토큰은 더 이상 못 쓰게 처리(Rotation 되었다고 표시)하거나 블랙리스트에 등록할 수 있음
            // 여기서는 블랙리스트에 'rotated' 상태로 등록하여 재사용 방지
            redisTemplate.opsForValue().set(blPrefix + refreshToken, "rotated");

            // 새 리프레시 토큰 발급
            newRefreshToken = jwtUtil.createRefreshToken(user.getUsersIdx());
            // Redis에 새 토큰 저장 (사용자별 최신 토큰 갱신)
            redisTemplate.opsForValue().set(rtPrefix + userIdx, newRefreshToken);
        }

        // 7. 결과 반환
        TokenDto tokenDto = new TokenDto();
        tokenDto.setAccessToken(newAccessToken);
        tokenDto.setRefreshToken(newRefreshToken); // 갱신 안 됐으면 null일 수 있음

        return tokenDto;
    }
}