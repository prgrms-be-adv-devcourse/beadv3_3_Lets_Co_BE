package co.kr.user.service.Impl;

import co.kr.user.dao.UserRepository;
import co.kr.user.model.dto.login.LoginDTO;
import co.kr.user.model.dto.login.LoginReq;
import co.kr.user.model.entity.Users;
import co.kr.user.service.LoginService;
import co.kr.user.util.BCryptUtil;
import co.kr.user.util.JWTUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 로그인 및 로그아웃 관련 비즈니스 로직을 처리하는 서비스 클래스입니다.
 * 사용자 인증, 로그인 실패 시 보안 정책(잠금 등), JWT 토큰(Access/Refresh) 발급 및 관리를 담당합니다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LoginServiceImpl implements LoginService {
    private final UserRepository userRepository;

    private final RedisTemplate<String, Object> redisTemplate;

    private final UserQueryServiceImpl userQueryServiceImpl;

    private final BCryptUtil bCryptUtil; // 비밀번호 검증 유틸리티
    private final JWTUtil jwtUtil; // JWT 생성 및 검증 유틸리티

    /**
     * 로그인 처리 메서드입니다.
     * 사용자 인증을 수행하고, 성공 시 JWT 토큰(Access, Refresh)을 발급합니다.
     * 5회 이상 실패 시 계정을 잠그는 보안 로직이 포함되어 있습니다.
     *
     * @param loginReq 로그인 요청 정보 (아이디, 비밀번호)
     * @return LoginDTO (생성된 AccessToken, RefreshToken)
     */
    @Override
    @Transactional
    public LoginDTO login(LoginReq loginReq) {
        Users users = userQueryServiceImpl.findActiveUserById(loginReq.getId());

        // 로그인 실패 횟수 확인 (5회 이상이면 잠금 시도)
        if (users.getFailedLoginAttempts() >= 5) {
            users.lockAccount(); // 15분간 잠금 처리
            throw new IllegalStateException("보안을 위해 계정이 일시 정지되었습니다. 15분 후에 다시 시도해주세요.");
        }

        // 계정 잠금 시간 확인 (현재 시간이 잠금 해제 시간 이전인지 체크)
        if (users.getLockedUntil() != null) {
            if (users.getLockedUntil().isAfter(users.getUpdatedAt())) {
                throw new IllegalStateException("계정이 " + users.getLockedUntil() + "까지 정지되었습니다.");
            }
        }

        // 비밀번호 일치 여부 확인
        if (!bCryptUtil.check(loginReq.getPw(), users.getPw())) {
            users.increaseLoginFailCount(); // 실패 횟수 증가
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 3. 토큰 생성
        String accessToken = jwtUtil.createAccessToken(users.getUsersIdx(), users.getCreatedAt(), users.getUpdatedAt());
        String refreshToken = jwtUtil.createRefreshToken(users.getUsersIdx());

        // 4. Redis에 Refresh Token 저장 (RT:userIdx)
        // Key는 RT:유저식별자, 만료 시간은 7일로 설정합니다.
        redisTemplate.opsForValue().set(
                "RT:" + users.getUsersIdx(),
                refreshToken
        );

        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setAccessToken(accessToken);
        loginDTO.setRefreshToken(refreshToken);

        users.completeLogin();
        return loginDTO;
    }

    /**
     * 로그아웃 처리 메서드입니다.
     * DB에 저장된 Refresh Token을 찾아 만료(Logout) 상태로 변경하여 더 이상 토큰 갱신이 불가능하게 만듭니다.
     *
     * @param refreshToken 클라이언트 쿠키에 저장되어 있던 Refresh Token
     * @return 로그아웃 결과 메시지
     */
// LoginServiceImpl.java 수정 제안
    @Override
    @Transactional
    public String logout(String refreshToken) {
        if (refreshToken == null) {
            throw new IllegalArgumentException("로그아웃할 토큰이 없습니다.");
        }

        // 1. 토큰에서 사용자 식별자 추출 (RT 삭제를 위함)
        String userIdx = jwtUtil.getRefreshTokenClaims(refreshToken).getSubject();

        // 2. Redis의 RT(Refresh Token) 삭제
        redisTemplate.delete("RT:" + userIdx);

        // 3. Redis의 BL(Blacklist)에 해당 refreshToken 영구 저장
        // TTL을 설정하지 않아 영구 보관됩니다.
        redisTemplate.opsForValue().set("BL:" + refreshToken, "logout");

        return "로그아웃 되었습니다.";
    }
}