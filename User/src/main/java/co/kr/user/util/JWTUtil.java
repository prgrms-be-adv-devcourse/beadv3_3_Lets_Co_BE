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
 * JWT(Json Web Token) 생성 및 검증을 담당하는 유틸리티 클래스입니다.
 * Access Token과 Refresh Token의 발급, 검증, Claims 파싱 기능을 제공합니다.
 * Spring Security 및 JJWT 라이브러리를 기반으로 동작합니다.
 */
@Component
public class JWTUtil {

    // Access Token 서명 및 검증에 사용할 비밀키 객체
    private final SecretKey accessKey;

    // Refresh Token 서명 및 검증에 사용할 비밀키 객체
    private final SecretKey refreshKey;

    // Access Token 유효 시간 (밀리초 단위)
    private final long accessTokenExp;

    // Refresh Token 유효 시간 (밀리초 단위)
    private final long refreshTokenExp;

    /**
     * 생성자: application.properties(yml)에서 설정값을 주입받아 초기화합니다.
     * Base64로 인코딩된 비밀키 문자열을 디코딩하여 HMAC-SHA 암호화 키로 변환합니다.
     *
     * @param accessSecret Access Token용 비밀키 문자열
     * @param accessTokenExp Access Token 만료 시간
     * @param refreshSecret Refresh Token용 비밀키 문자열
     * @param refreshTokenExp Refresh Token 만료 시간
     */
    public JWTUtil(
            @Value("${custom.security.jwt.access.secret}")
            String accessSecret,
            @Value("${custom.security.jwt.access.expiration}")
            long accessTokenExp,
            @Value("${custom.security.jwt.refresh.secret}")
            String refreshSecret,
            @Value("${custom.security.jwt.refresh.expiration}")
            long refreshTokenExp
    ) {
        // Access Token 키 생성 (Base64 디코딩 -> Key 객체 변환)
        byte[] accessKeyBytes = Decoders.BASE64.decode(accessSecret);
        this.accessKey = Keys.hmacShaKeyFor(accessKeyBytes);
        this.accessTokenExp = accessTokenExp;

        // Refresh Token 키 생성
        byte[] refreshKeyBytes = Decoders.BASE64.decode(refreshSecret);
        this.refreshKey = Keys.hmacShaKeyFor(refreshKeyBytes);
        this.refreshTokenExp = refreshTokenExp;
    }

    /**
     * Access Token을 생성하는 메서드입니다.
     * 사용자 식별자(userIDX)를 Subject로 설정하고, 생성일/수정일 정보를 커스텀 Claim으로 포함합니다.
     *
     * @param userIDX 사용자 고유 식별자
     * @param userCreatedAt 계정 생성일 (LocalDateTime)
     * @param userUpdatedAt 계정 수정일 (LocalDateTime)
     * @return 생성된 JWT Access Token 문자열
     */
    public String createAccessToken(
            Long userIDX,
            LocalDateTime userCreatedAt,
            LocalDateTime userUpdatedAt
    ) {
        Date now = new Date();

        // LocalDateTime을 java.util.Date로 변환 (null일 경우 현재 시간 사용)
        Date createdAtDate = (userCreatedAt != null)
                ? Date.from(userCreatedAt.atZone(ZoneId.systemDefault()).toInstant())
                : new Date();

        Date updatedAtDate = (userUpdatedAt != null)
                ? Date.from(userUpdatedAt.atZone(ZoneId.systemDefault()).toInstant())
                : new Date();

        return Jwts.builder()
                .subject(String.valueOf(userIDX)) // 토큰 주체(Subject) 설정

                .claim("createdAt", createdAtDate) // 생성일 정보 추가
                .claim("updatedAt", updatedAtDate) // 수정일 정보 추가

                .issuedAt(now) // 토큰 발행 시간
                .expiration(new Date(now.getTime() + accessTokenExp)) // 토큰 만료 시간 설정
                .signWith(accessKey, Jwts.SIG.HS256) // Access Key로 서명 (HMAC SHA-256)
                .compact(); // 토큰 생성 및 직렬화
    }

    /**
     * Refresh Token을 생성하는 메서드입니다.
     * Access Token 재발급을 위한 용도로, 최소한의 정보(Subject)만 포함합니다.
     *
     * @param userIDX 사용자 고유 식별자
     * @return 생성된 JWT Refresh Token 문자열
     */
    public String createRefreshToken(Long userIDX) {
        Date now = new Date();

        return Jwts.builder()
                .subject(String.valueOf(userIDX))
                .issuedAt(now)
                .expiration(new Date(now.getTime() + refreshTokenExp))
                .signWith(refreshKey, Jwts.SIG.HS256) // Refresh Key로 서명
                .compact();
    }

    /**
     * Access Token의 유효성을 검증하는 메서드입니다.
     *
     * @param token 검증할 Access Token
     * @return 유효하면 true, 만료되거나 위변조되었으면 false
     */
    public boolean validateAccessToken(String token) {
        return validateToken(token, accessKey);
    }

    /**
     * Access Token에서 Payload(Claims)를 추출하는 메서드입니다.
     *
     * @param token Access Token
     * @return 토큰에 포함된 Claims 객체
     */
    public Claims getAccessTokenClaims(String token) {
        return parseClaims(token, accessKey);
    }

    /**
     * Refresh Token의 유효성을 검증하는 메서드입니다.
     *
     * @param token 검증할 Refresh Token
     * @return 유효하면 true, 아니면 false
     */
    public boolean validateRefreshToken(String token) {
        return validateToken(token, refreshKey);
    }

    /**
     * Refresh Token에서 Payload(Claims)를 추출하는 메서드입니다.
     *
     * @param token Refresh Token
     * @return 토큰에 포함된 Claims 객체
     */
    public Claims getRefreshTokenClaims(String token) {
        return parseClaims(token, refreshKey);
    }

    /**
     * 내부적으로 사용되는 토큰 검증 메서드입니다.
     * 지정된 비밀키(Key)를 사용하여 서명을 확인하고 토큰을 파싱합니다.
     *
     * @param token 검증할 JWT 토큰
     * @param key 서명 검증에 사용할 비밀키
     * @return 검증 성공 시 true, 실패 시 false
     */
    private boolean validateToken(String token, SecretKey key) {
        try {
            Jwts.parser()
                    .verifyWith(key) // 서명 검증을 위한 키 설정
                    .build()
                    .parseSignedClaims(token); // 파싱 시도 (실패 시 예외 발생)

            return true;

        } catch (JwtException | IllegalArgumentException e) {
            // 서명 불일치, 만료, 구조적 문제 등 예외 발생 시 유효하지 않음으로 처리
            return false;
        }
    }

    /**
     * 내부적으로 사용되는 Claims 파싱 메서드입니다.
     * 토큰의 Payload 부분을 추출하여 반환합니다.
     *
     * @param token 파싱할 JWT 토큰
     * @param key 서명 검증 키
     * @return 파싱된 Claims 객체
     */
    private Claims parseClaims(String token, SecretKey key) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload(); // Body(Payload) 반환
    }
}