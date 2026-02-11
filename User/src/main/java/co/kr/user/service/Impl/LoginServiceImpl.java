package co.kr.user.service.Impl;

import co.kr.user.model.dto.login.LoginDTO;
import co.kr.user.model.dto.login.LoginReq;
import co.kr.user.model.entity.Users;
import co.kr.user.service.LoginService;
import co.kr.user.service.UserQueryService;
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
    private final BCryptUtil bCryptUtil;
    private final JWTUtil jwtUtil;
    private final RedisTemplate<String, Object> redisTemplate;
    private final UserQueryService userQueryService;

    @Override
    @Transactional
    public LoginDTO login(LoginReq loginReq) {
        Users user = userQueryService.findActiveUserById(loginReq.getId());

        if (user.isLocked()) {
            throw new IllegalStateException("계정이 잠금 상태입니다. 해제 일시: " + user.getLockedUntil());
        }

        if (!bCryptUtil.check(loginReq.getPw(), user.getPw())) {
            user.handleLoginFailure();
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        user.completeLogin();

        String rtKey = "RT:" + user.getUsersIdx();
        String oldRefreshToken = (String) redisTemplate.opsForValue().get(rtKey);

        if (oldRefreshToken != null) {
            redisTemplate.delete(rtKey);
            redisTemplate.opsForValue().set("BL:" + oldRefreshToken, "logout");
        }

        String accessToken = jwtUtil.createAccessToken(
                user.getUsersIdx(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
        String refreshToken = jwtUtil.createRefreshToken(user.getUsersIdx());

        redisTemplate.opsForValue().set(rtKey, refreshToken);

        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setAccessToken(accessToken);
        loginDTO.setRefreshToken(refreshToken);

        return loginDTO;
    }

    @Override
    public String logout(String refreshToken) {
        if (refreshToken == null || !jwtUtil.validateRefreshToken(refreshToken)) {
            throw new IllegalArgumentException("유효하지 않은 토큰입니다.");
        }

        Long userIdx = Long.parseLong(jwtUtil.getRefreshTokenClaims(refreshToken).getSubject());
        String rtKey = "RT:" + userIdx;

        if (Boolean.TRUE.equals(redisTemplate.hasKey(rtKey))) {
            redisTemplate.delete(rtKey);
        }

        redisTemplate.opsForValue().set("BL:" + refreshToken, "logout");

        return "로그아웃 되었습니다.";
    }
}