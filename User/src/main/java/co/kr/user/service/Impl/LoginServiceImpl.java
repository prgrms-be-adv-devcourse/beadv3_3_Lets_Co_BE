package co.kr.user.service.Impl;

import co.kr.user.model.dto.login.LoginDTO;
import co.kr.user.model.dto.login.LoginReq;
import co.kr.user.model.entity.Users;
import co.kr.user.service.LoginService;
import co.kr.user.service.UserQueryService;
import co.kr.user.util.BCryptUtil;
import co.kr.user.util.JWTUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * LoginService 인터페이스의 구현체입니다.
 * 로그인 및 로그아웃과 관련된 비즈니스 로직(인증, 토큰 발급/폐기 등)을 처리합니다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LoginServiceImpl implements LoginService {
    // 비밀번호 암호화 및 검증 유틸리티
    private final BCryptUtil bCryptUtil;
    // JWT 토큰 생성 및 검증 유틸리티
    private final JWTUtil jwtUtil;
    // Redis 작업을 위한 템플릿 (토큰 저장 및 관리)
    private final RedisTemplate<String, Object> redisTemplate;
    // 사용자 정보 조회를 위한 공통 서비스
    private final UserQueryService userQueryService;

    // application.yml에서 설정된 로그인 실패 허용 횟수
    @Value("${custom.security.login.max-attempts}")
    private int maxAttempts;

    // 로그인 실패로 인한 계정 잠금 시간 (분 단위)
    @Value("${custom.security.login.lock-minutes}")
    private int lockMinutes;

    // Redis에 저장될 Refresh Token 키의 접두사
    @Value("${custom.security.redis.rt-prefix}")
    private String rtPrefix;

    // Redis에 저장될 Blacklist 키의 접두사
    @Value("${custom.security.redis.bl-prefix}")
    private String blPrefix;

    /**
     * 로그인을 수행합니다.
     * 아이디와 비밀번호를 검증하고, 성공 시 JWT 토큰(Access, Refresh)을 발급합니다.
     * @param loginReq 로그인 요청 정보 (아이디, 비밀번호)
     * @return 발급된 토큰 정보가 담긴 DTO
     */
    @Override
    @Transactional // 로그인 성공 시 실패 횟수 초기화, 토큰 저장 등 쓰기 작업이 필요하므로 트랜잭션 적용
    public LoginDTO login(LoginReq loginReq) {
        // 아이디로 활성 사용자 조회 (없으면 예외 발생)
        Users user = userQueryService.findActiveUserById(loginReq.getId());

        // 계정이 잠금 상태인지 확인 (비밀번호 5회 오류 등)
        if (user.isLocked()) {
            throw new IllegalStateException("계정이 잠금 상태입니다. 해제 일시: " + user.getLockedUntil());
        }

        // 비밀번호 검증: 입력된 비밀번호와 저장된 암호화된 비밀번호 비교
        if (!bCryptUtil.check(loginReq.getPw(), user.getPw())) {
            // 비밀번호 불일치 시 실패 처리 로직 수행 (실패 횟수 증가, 임계치 초과 시 잠금 설정)
            user.handleLoginFailure(maxAttempts, lockMinutes);
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 로그인 성공 처리 (실패 횟수 0으로 초기화)
        user.completeLogin();

        // 중복 로그인 방지 또는 기존 세션 정리:
        // 해당 사용자의 기존 Refresh Token이 Redis에 있다면 삭제하고 블랙리스트에 추가
        String rtKey = rtPrefix + user.getUsersIdx();
        String oldRefreshToken = (String) redisTemplate.opsForValue().get(rtKey);

        if (oldRefreshToken != null) {
            redisTemplate.delete(rtKey);
            redisTemplate.opsForValue().set(blPrefix + oldRefreshToken, "logout");
        }

        // 새로운 Access Token 생성
        String accessToken = jwtUtil.createAccessToken(
                user.getUsersIdx(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
        // 새로운 Refresh Token 생성
        String refreshToken = jwtUtil.createRefreshToken(user.getUsersIdx());

        // Redis에 새로운 Refresh Token 저장 (키: RT:userIdx, 값: token)
        redisTemplate.opsForValue().set(rtKey, refreshToken);

        // 응답 DTO 생성 및 반환
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setAccessToken(accessToken);
        loginDTO.setRefreshToken(refreshToken);

        return loginDTO;
    }

    /**
     * 로그아웃을 수행합니다.
     * Refresh Token을 무효화(삭제 및 블랙리스트 등록)합니다.
     * @param refreshToken 로그아웃할 사용자의 리프레시 토큰
     * @return 로그아웃 결과 메시지
     */
    @Override
    @Transactional
    public String logout(String refreshToken) {
        // 토큰 유효성 검증
        if (refreshToken == null || !jwtUtil.validateRefreshToken(refreshToken)) {
            throw new IllegalArgumentException("유효하지 않은 토큰입니다.");
        }

        // 토큰에서 사용자 식별자 추출
        Long userIdx = jwtUtil.getUserIdxFromToken(refreshToken, false);
        String rtKey = rtPrefix + userIdx;

        // Redis에 저장된 해당 사용자의 Refresh Token 삭제
        if (Boolean.TRUE.equals(redisTemplate.hasKey(rtKey))) {
            redisTemplate.delete(rtKey);
        }

        // 해당 토큰을 블랙리스트에 추가하여 재사용 방지 (남은 유효시간 동안만 저장하면 더 좋음)
        redisTemplate.opsForValue().set(blPrefix + refreshToken, "logout");

        return "로그아웃 되었습니다.";
    }
}