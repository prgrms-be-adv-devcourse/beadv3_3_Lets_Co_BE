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

@Component
public class JWTUtil {
    private final SecretKey accessKey;
    private final SecretKey refreshKey;
    private final long accessTokenExp;
    private final long refreshTokenExp;

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
        byte[] accessKeyBytes = Decoders.BASE64.decode(accessSecret);
        this.accessKey = Keys.hmacShaKeyFor(accessKeyBytes);
        this.accessTokenExp = accessTokenExp;

        byte[] refreshKeyBytes = Decoders.BASE64.decode(refreshSecret);
        this.refreshKey = Keys.hmacShaKeyFor(refreshKeyBytes);
        this.refreshTokenExp = refreshTokenExp;
    }

    public String createAccessToken(
            Long userIDX,
            LocalDateTime userCreatedAt,
            LocalDateTime userUpdatedAt
    ) {
        Date now = new Date();

        Date createdAtDate = (userCreatedAt != null)
                ? Date.from(userCreatedAt.atZone(ZoneId.systemDefault()).toInstant())
                : new Date();

        Date updatedAtDate = (userUpdatedAt != null)
                ? Date.from(userUpdatedAt.atZone(ZoneId.systemDefault()).toInstant())
                : new Date();

        return Jwts.builder()
                .subject(String.valueOf(userIDX))

                .claim("createdAt", createdAtDate)
                .claim("updatedAt", updatedAtDate)

                .issuedAt(now)
                .expiration(new Date(now.getTime() + accessTokenExp))
                .signWith(accessKey, Jwts.SIG.HS256)
                .compact();
    }

    public String createRefreshToken(Long userIDX) {
        Date now = new Date();

        return Jwts.builder()
                .subject(String.valueOf(userIDX))
                .issuedAt(now)
                .expiration(new Date(now.getTime() + refreshTokenExp))
                .signWith(refreshKey, Jwts.SIG.HS256)
                .compact();
    }

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