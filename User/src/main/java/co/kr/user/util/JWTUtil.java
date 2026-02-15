package co.kr.user.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * JWT(Json Web Token) 생성, 검증, 파싱을 담당하는 유틸리티 클래스입니다.
 * JJWT 라이브러리를 사용하여 구현되었습니다.
 */
@Component
public class JWTUtil {
    // 액세스 토큰 서명에 사용할 비밀키
    private final SecretKey accessKey;
    // 리프레시 토큰 서명에 사용할 비밀키
    private final SecretKey refreshKey;
    // 액세스 토큰 만료 시간 (밀리초)
    private final long accessTokenExp;
    // 리프레시 토큰 만료 시간 (밀리초)
    private final long refreshTokenExp;

    /**
     * 생성자: application.yml 설정 파일에서 비밀키와 만료 시간을 주입받습니다.
     * @param accessSecret 액세스 토큰용 비밀키 문자열 (Base64 인코딩됨)
     * @param accessTokenExp 액세스 토큰 유효 시간
     * @param refreshSecret 리프레시 토큰용 비밀키 문자열 (Base64 인코딩됨)
     * @param refreshTokenExp 리프레시 토큰 유효 시간
     */
    public JWTUtil(
            @Value("${custom.security.jwt.access.secret}") String accessSecret,
            @Value("${custom.security.jwt.access.expiration}") long accessTokenExp,
            @Value("${custom.security.jwt.refresh.secret}") String refreshSecret,
            @Value("${custom.security.jwt.refresh.expiration}") long refreshTokenExp
    ) {
        // application.yml의 secret 값은 반드시 'Base64로 인코딩된 문자열'이어야 합니다.
        // 일반 평문 문자열을 넣으면 디코딩 에러가 발생합니다.
        this.accessKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(accessSecret));
        this.refreshKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(refreshSecret));
        this.accessTokenExp = accessTokenExp;
        this.refreshTokenExp = refreshTokenExp;
    }

    /**
     * 사용자 정보를 담은 액세스 토큰(Access Token)을 생성합니다.
     * @param userIDX 사용자 식별자 (PK)
     * @param userCreatedAt 사용자 생성일
     * @param userUpdatedAt 사용자 수정일
     * @return 생성된 JWT 액세스 토큰 문자열
     */
    public String createAccessToken(Long userIDX, LocalDateTime userCreatedAt, LocalDateTime userUpdatedAt) {
        Date now = new Date();
        // LocalDateTime을 Date 객체로 변환 (JWT Claims는 Date 타입을 사용)
        Date createdAtDate = (userCreatedAt != null) ? Date.from(userCreatedAt.atZone(ZoneId.systemDefault()).toInstant()) : now;
        Date updatedAtDate = (userUpdatedAt != null) ? Date.from(userUpdatedAt.atZone(ZoneId.systemDefault()).toInstant()) : now;

        return Jwts.builder()
                .subject(String.valueOf(userIDX)) // 토큰의 주체(Subject)로 사용자 ID 설정
                .claim("createdAt", createdAtDate) // 커스텀 클레임: 생성일
                .claim("updatedAt", updatedAtDate) // 커스텀 클레임: 수정일
                .issuedAt(now) // 토큰 발급 시간
                .expiration(new Date(now.getTime() + accessTokenExp)) // 토큰 만료 시간
                .signWith(accessKey, Jwts.SIG.HS256) // HS256 알고리즘과 액세스 키로 서명
                .compact();
    }

    /**
     * 토큰 갱신을 위한 리프레시 토큰(Refresh Token)을 생성합니다.
     * 액세스 토큰보다 유효 기간이 길며, 최소한의 정보만 포함합니다.
     * @param userIDX 사용자 식별자
     * @return 생성된 JWT 리프레시 토큰 문자열
     */
    public String createRefreshToken(Long userIDX) {
        Date now = new Date();
        return Jwts.builder()
                .subject(String.valueOf(userIDX)) // 토큰의 주체로 사용자 ID 설정
                .issuedAt(now)
                .expiration(new Date(now.getTime() + refreshTokenExp)) // 리프레시 토큰 만료 시간 설정
                .signWith(refreshKey, Jwts.SIG.HS256) // HS256 알고리즘과 리프레시 키로 서명
                .compact();
    }

    /**
     * 리프레시 토큰의 유효성을 검증합니다.
     * @param token 검증할 리프레시 토큰
     * @return 유효하면 true, 그렇지 않으면 false
     */
    public boolean validateRefreshToken(String token) {
        return validateToken(token, refreshKey);
    }

    /**
     * 토큰에서 사용자 식별자(User IDX)를 추출합니다.
     * @param token JWT 토큰
     * @param isAccessToken 액세스 토큰 여부 (true: 액세스 토큰 키 사용, false: 리프레시 토큰 키 사용)
     * @return 추출된 사용자 식별자 (Long)
     */
    public Long getUserIdxFromToken(String token, boolean isAccessToken) {
        // 토큰 종류에 따라 알맞은 키를 사용하여 클레임을 파싱하고 Subject(userIdx)를 가져옴
        return Long.parseLong(parseClaims(token, isAccessToken ? accessKey : refreshKey).getSubject());
    }

    /**
     * 토큰의 서명과 유효성을 검증하는 내부 메서드입니다.
     * @param token JWT 토큰
     * @param key 서명 검증에 사용할 비밀키
     * @return 검증 성공 시 true, 예외 발생 시 false
     */
    private boolean validateToken(String token, SecretKey key) {
        try {
            // 서명 키를 설정하고 토큰을 파싱하여 검증 수행
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            // 서명이 맞지 않거나, 만료되었거나, 형식이 잘못된 경우 등 모든 JWT 관련 예외 처리
            return false;
        }
    }

    /**
     * 토큰을 파싱하여 클레임 정보를 가져오는 내부 메서드입니다.
     * @param token JWT 토큰
     * @param key 파싱에 사용할 비밀키
     * @return 파싱된 Claims Payload
     */
    private Claims parseClaims(String token, SecretKey key) {
        // 서명 검증 후 페이로드(Payload) 부분의 클레임 반환
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }
}