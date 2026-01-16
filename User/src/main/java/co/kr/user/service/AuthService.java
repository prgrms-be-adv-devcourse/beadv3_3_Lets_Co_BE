package co.kr.user.service;

import co.kr.user.DAO.UserRepository;
import co.kr.user.DAO.UsersLoginRepository;
import co.kr.user.model.entity.Users;
import co.kr.user.model.entity.Users_Login;
import co.kr.user.model.vo.UsersRole;
import co.kr.user.util.JWTUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService implements AuthServiceImpl {

    private final UserRepository userRepository;
    private final UsersLoginRepository usersLoginRepository;

    private final JWTUtil jwtUtil;

    @Override
    public UsersRole getRole(Long userIdx) {
        // 값이 없으면 바로 에러를 던지므로, 변수에 담긴 users는 무조건 null이 아님을 보장받음
        Users users = userRepository.findById(userIdx)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        return users.getRole();
    }

    /**
     * [토큰 재발급]
     * Refresh Token으로 검증 후 Access Token 재발급
     */
    @Transactional
    public String renewAccessToken(String refreshToken) {
        // 1. [1차 검증] 토큰 자체의 서명 및 만료 확인 (JWTUtil)
        if (!jwtUtil.validateRefreshToken(refreshToken)) {
            throw new IllegalArgumentException("유효하지 않은 토큰입니다.");
        }

        // 2. [2차 검증] DB에 해당 토큰이 존재하는지 확인
        Users_Login loginEntity = usersLoginRepository.findByToken(refreshToken)
                .orElseThrow(() -> new IllegalArgumentException("DB에 존재하지 않는 토큰입니다. (로그아웃 됨)"));

        // 3. [3차 검증] 로그아웃(Revoke)된 상태인지 확인
        if (loginEntity.getRevokedAt() != null) {
            log.warn("폐기된 토큰 사용 시도! UserIdx: {}", loginEntity.getUsersIdx());
            throw new IllegalArgumentException("이미 로그아웃된 토큰입니다.");
        }

        // 4. [교차 검증] 토큰 내 UserID와 DB 기록의 UserID 일치 여부
        Claims claims = jwtUtil.getRefreshTokenClaims(refreshToken);
        Long tokenUserIdx = Long.valueOf(claims.getSubject());

        if (!loginEntity.getUsersIdx().equals(tokenUserIdx)) {
            throw new IllegalArgumentException("토큰 소유자가 일치하지 않습니다.");
        }

        // 5. [갱신] 마지막 사용 시간 업데이트 (엔티티에 메서드 추가 필요 혹은 Setter 사용)
        // loginEntity.updateLastUsedAt(); // 이 기능을 엔티티에 추가하면 좋습니다.

        // 6. 유저 정보 조회 (Access Token 생성용)
        Users user = userRepository.findById(tokenUserIdx)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        loginEntity.updateLastUsedAt();

        // 7. 새 Access Token 발급
        return jwtUtil.createAccessToken(
                user.getUsersIdx(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    @Override
    public String renewRefreshToken(String refreshToken) {
        // 1. [1차 검증] 토큰 자체의 서명 및 만료 확인 (JWTUtil)
        if (!jwtUtil.validateRefreshToken(refreshToken)) {
            throw new IllegalArgumentException("유효하지 않은 토큰입니다.");
        }

        // 2. [2차 검증] DB에 해당 토큰이 존재하는지 확인
        Users_Login loginEntity = usersLoginRepository.findByToken(refreshToken)
                .orElseThrow(() -> new IllegalArgumentException("DB에 존재하지 않는 토큰입니다. (로그아웃 됨)"));

        // 3. [3차 검증] 로그아웃(Revoke)된 상태인지 확인
        if (loginEntity.getRevokedAt() != null) {
            log.warn("폐기된 토큰 사용 시도! UserIdx: {}", loginEntity.getUsersIdx());
            throw new IllegalArgumentException("이미 로그아웃된 토큰입니다.");
        }

        // 4. [교차 검증] 토큰 내 UserID와 DB 기록의 UserID 일치 여부
        Claims claims = jwtUtil.getRefreshTokenClaims(refreshToken);
        Long tokenUserIdx = Long.valueOf(claims.getSubject());

        if (!loginEntity.getUsersIdx().equals(tokenUserIdx)) {
            throw new IllegalArgumentException("토큰 소유자가 일치하지 않습니다.");
        }

        loginEntity.logout();

        // 6. 유저 정보 조회 (Access Token 생성용)
        Users users = userRepository.findById(tokenUserIdx)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        String renewRefreshToken = jwtUtil.createRefreshToken(
                users.getUsersIdx()
        );

        Users_Login usersLogin = Users_Login.builder()
                .usersIdx(users.getUsersIdx())
                .token(renewRefreshToken)
                .lastUsedAt(null) // 초기 생성 시점에는 사용 기록 없음 (또는 현재 시간)
                .build();

        usersLoginRepository.save(usersLogin);

        return renewRefreshToken;
    }
}
