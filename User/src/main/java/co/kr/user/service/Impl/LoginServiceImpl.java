package co.kr.user.service.Impl;

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

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LoginServiceImpl implements LoginService {
    private final RedisTemplate<String, Object> redisTemplate;

    private final UserQueryServiceImpl userQueryServiceImpl;

    private final BCryptUtil bCryptUtil;
    private final JWTUtil jwtUtil;

    @Override
    @Transactional
    public LoginDTO login(LoginReq loginReq) {
        Users users = userQueryServiceImpl.findActiveUserById(loginReq.getId());

        if (users.getFailedLoginAttempts() >= 5) {
            users.lockAccount();
            throw new IllegalStateException("보안을 위해 계정이 일시 정지되었습니다. 15분 후에 다시 시도해주세요.");
        }

        if (users.getLockedUntil() != null && users.getLockedUntil().isAfter(users.getUpdatedAt())) {
                throw new IllegalStateException("계정이 " + users.getLockedUntil() + "까지 정지되었습니다.");
            }


        if (!bCryptUtil.check(loginReq.getPw(), users.getPw())) {
            users.increaseLoginFailCount();
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        String accessToken = jwtUtil.createAccessToken(users.getUsersIdx(), users.getCreatedAt(), users.getUpdatedAt());
        String refreshToken = jwtUtil.createRefreshToken(users.getUsersIdx());

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

    @Override
    @Transactional
    public String logout(String refreshToken) {
        if (refreshToken == null) {
            throw new IllegalArgumentException("로그아웃할 토큰이 없습니다.");
        }

        String userIdx = jwtUtil.getRefreshTokenClaims(refreshToken).getSubject();

        redisTemplate.delete("RT:" + userIdx);

        redisTemplate.opsForValue().set("BL:" + refreshToken, "logout");

        return "로그아웃 되었습니다.";
    }
}