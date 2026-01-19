package co.kr.user.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Slf4j
@Component
public class JWTUtil {

    private final SecretKey accessKey;
    private final SecretKey refreshKey;
    private final long accessTokenExp;
    private final long refreshTokenExp;

    public JWTUtil(
            @Value("${custom.security.jwt.access.secret}") String accessSecret,
            @Value("${custom.security.jwt.access.expiration}") long accessTokenExp,
            @Value("${custom.security.jwt.refresh.secret}") String refreshSecret,
            @Value("${custom.security.jwt.refresh.expiration}") long refreshTokenExp
    ) {
        byte[] accessKeyBytes = Decoders.BASE64.decode(accessSecret);
        this.accessKey = Keys.hmacShaKeyFor(accessKeyBytes);
        this.accessTokenExp = accessTokenExp;

        byte[] refreshKeyBytes = Decoders.BASE64.decode(refreshSecret);
        this.refreshKey = Keys.hmacShaKeyFor(refreshKeyBytes);
        this.refreshTokenExp = refreshTokenExp;
    }

    /**
     * Access Token 생성 (최소 정보 버전)
     * [Payload]
     * 1. sub: ID (Email) -> 유일한 식별자
     * 2. createdAt, updatedAt -> 유효성 검증용
     */
    public String createAccessToken(
            Long userIDX,           // ID
            LocalDateTime userCreatedAt,     // 가입일
            LocalDateTime userUpdatedAt      // 수정일
    ) {
        Date now = new Date();

        // [핵심] LocalDateTime -> java.util.Date 변환
        // (null 체크는 필요에 따라 추가하세요)
        Date createdAtDate = (userCreatedAt != null)
                ? Date.from(userCreatedAt.atZone(ZoneId.systemDefault()).toInstant())
                : new Date();

        Date updatedAtDate = (userUpdatedAt != null)
                ? Date.from(userUpdatedAt.atZone(ZoneId.systemDefault()).toInstant())
                : new Date();

        return Jwts.builder()
                .subject(String.valueOf(userIDX))                         // ID (Email)

                .claim("createdAt", createdAtDate)      // 유효성 검증용
                .claim("updatedAt", updatedAtDate)      // 유효성 검증용

                .issuedAt(now)
                .expiration(new Date(now.getTime() + accessTokenExp))
                .signWith(accessKey, Jwts.SIG.HS256)
                .compact();
    }

    /**
     * 2. Refresh Token 생성 (변경 없음)
     * Payload: ID(Email) (최소 정보)
     */
    public String createRefreshToken(Long userIDX) {
        Date now = new Date();

        return Jwts.builder()
                .subject(String.valueOf(userIDX))
                .issuedAt(now)
                .expiration(new Date(now.getTime() + refreshTokenExp)) // 만료: 7일
                .signWith(refreshKey, Jwts.SIG.HS256)   // Refresh Key 사용
                .compact();
    }

    // --- 검증 및 파싱 로직 (동일) ---

    public boolean validateAccessToken(String token) {
        return validateToken(token, accessKey);
    }

    public Claims getAccessTokenClaims(String token) {
        return parseClaims(token, accessKey);
    }

    public boolean validateRefreshToken(String token) {
        return validateToken(token, refreshKey);
    }

    public Claims getRefreshTokenClaims(String token) {
        return parseClaims(token, refreshKey);
    }

    private boolean validateToken(String token, SecretKey key) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            // 로그 생략 또는 필요시 추가
            return false;
        }
    }

    private Claims parseClaims(String token, SecretKey key) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}