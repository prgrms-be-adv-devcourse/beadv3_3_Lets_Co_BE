package co.kr.user.service;

import co.kr.user.dao.UserRepository;
import co.kr.user.dao.UsersLoginRepository;
import co.kr.user.model.dto.auth.TokenDto;
import co.kr.user.model.entity.Users;
import co.kr.user.model.entity.UsersLogin;
import co.kr.user.model.vo.UsersRole;
import co.kr.user.util.JWTUtil;
import co.kr.user.util.TokenUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 인증(Authentication) 및 권한(Authorization) 관련 공통 로직을 처리하는 서비스 클래스입니다.
 * 사용자 권한 조회와 JWT 토큰 재발급(Refresh) 기능을 제공합니다.
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final UsersLoginRepository usersLoginRepository;

    private final JWTUtil jwtUtil; // JWT 생성 및 검증 유틸리티

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
        // DB에서 Refresh Token 유효성 검증
        UsersLogin usersLogin = usersLoginRepository.findFirstByTokenOrderByLoginIdxDesc(refreshToken)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 Refresh Token입니다."));

        Users users = userRepository.findById(usersLogin.getUsersIdx())
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보를 찾을 수 없습니다."));

        // 계정 정지(Lock) 여부 확인
        if (users.getLockedUntil() != null) {
            if (users.getLockedUntil().isAfter(users.getUpdatedAt())) {
                throw new IllegalStateException("계정이 " + users.getLockedUntil() + "까지 정지되었습니다.");
            }
        }

        // 새로운 Access Token 발급
        String newAccessToken = jwtUtil.createAccessToken(users.getUsersIdx(), users.getCreatedAt(), users.getUpdatedAt());

        String newRefreshToken = null;

        // Refresh Token 만료 임박 여부 확인 (Rotation 정책)
        if (TokenUtil.isTokenExpiringSoon(refreshToken)) {
            // 기존 토큰 만료 처리
            usersLogin.maturity();

            // 새 Refresh Token 발급
            newRefreshToken = jwtUtil.createRefreshToken(users.getUsersIdx());

            // DB 업데이트 (새 토큰 교체)
            usersLogin.updateToken(newRefreshToken);
        }

        TokenDto tokenDto = new TokenDto();
        tokenDto.setAccessToken(newAccessToken);
        tokenDto.setRefreshToken(newRefreshToken); // 교체되지 않았으면 null일 수 있음 (클라이언트 처리 필요)

        return tokenDto;
    }
}