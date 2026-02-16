package co.kr.gateway.filter;

import co.kr.gateway.DTO.TokenAuthorizationResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.util.Optional;

/**
 * JWT 토큰 기반 인증 필터 (Gateway Filter)
 * <p>
 * 클라이언트의 요청 헤더(Authorization) 또는 쿠키(accessToken)에서 JWT를 추출하여 유효성을 검증합니다.
 * 검증에 성공하면 사용자 ID(Subject)를 파싱하여 다운스트림 서비스(Microservices)에
 * 'X-USERS-IDX' 헤더로 전달합니다.
 */
@Slf4j
@Component
public class TokenAuthenticationFilter extends AbstractGatewayFilterFactory<TokenAuthenticationFilter.Config> {

    // JWT 서명 검증을 위한 비밀키 객체
    private final SecretKey accessKey;
    // JSON 응답 변환을 위한 ObjectMapper (Thread-safe)
    private final ObjectMapper om = new ObjectMapper();

    /**
     * 필터 설정 클래스 (현재는 별도의 설정 필드가 없으나 확장성을 위해 존재)
     */
    public static class Config {
    }

    /**
     * 생성자: 설정 파일(application.yml)에서 비밀키를 주입받아 초기화합니다.
     *
     * @param accessSecret Base64로 인코딩된 JWT 비밀키 문자열
     */
    public TokenAuthenticationFilter(@Value("${custom.security.jwt.access.secret}") String accessSecret) {
        super(Config.class);
        // Base64 문자열을 디코딩하여 HMAC-SHA 알고리즘용 SecretKey 생성
        byte[] accessKeyBytes = Decoders.BASE64.decode(accessSecret);
        this.accessKey = Keys.hmacShaKeyFor(accessKeyBytes);
    }

    /**
     * 필터의 메인 로직을 수행합니다.
     *
     * @param config 필터 설정 정보
     * @return GatewayFilter (인증 로직이 포함된 필터 체인)
     */
    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            ServerHttpResponse response = exchange.getResponse();

            // =================================================================
            // Step 1: 토큰 존재 여부 1차 확인 (헤더와 쿠키 모두 없는 경우)
            // =================================================================
            if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)
                    && !request.getCookies().containsKey("accessToken")) {
                log.error("Token is empty or cookie is empty");
                return writeUnauthorizedResponse(response, "인증 토큰이 없습니다.");
            }

            // =================================================================
            // Step 2: 실제 토큰 문자열 추출 (Bearer 제거 등 전처리)
            // =================================================================
            Optional<String> tokenOptional = resolveToken(request);
            if (tokenOptional.isEmpty()) {

                // 헤더 키는 있었으나 값이 비정상적인 경우 등
                return writeUnauthorizedResponse(response, "인증 토큰을 찾을 수 없습니다.");
            }

            String accessToken = tokenOptional.get();

            // =================================================================
            // Step 3: 토큰 검증 및 사용자 정보(Claims) 추출
            // =================================================================
            try {
                // Jwts.parser()를 통해 서명 검증과 만료 확인을 동시에 수행
                // 유효하지 않으면 JwtException 발생
                Claims claims = parseClaims(accessToken, accessKey);

                // Step 4: User ID 파싱 (Subject에서 추출)
                String subject = claims.getSubject();
                // Subject가 숫자가 아닐 경우를 대비해 파싱 시도 (보안 강화를 위한 타입 체크)
                Long tokenUserIdx = Long.valueOf(subject);

                log.info("Authenticated UserIdx : {}", tokenUserIdx);

                // Step 5: 다운스트림 서비스로 전달할 요청 헤더 변조
                // 'X-USERS-IDX' 헤더에 사용자 식별자를 담아서 전달
                ServerHttpRequest mutatedRequest = request.mutate()
                        .header("X-USERS-IDX", String.valueOf(tokenUserIdx))
                        .build();

                // 변조된 요청으로 다음 필터 체인 실행
                return chain.filter(exchange.mutate().request(mutatedRequest).build());

            } catch (JwtException e) {
                // 1. 토큰 만료 (ExpiredJwtException)
                // 2. 서명 불일치 (SignatureException)
                // 3. 잘못된 토큰 형식 (MalformedJwtException)
                log.info("Invalid JWT Token: {} msg: {}", accessToken, e.getMessage());
                return writeUnauthorizedResponse(response, "유효하지 않은 토큰입니다.");
            } catch (NumberFormatException e) {
                // 토큰은 유효하나, Subject(ID)가 숫자 형식이 아닌 경우 (비정상 토큰)
                log.error("Token subject is not a number. subject: {}", e.getMessage());
                return writeUnauthorizedResponse(response, "토큰 형식이 올바르지 않습니다.");
            } catch (Exception e) {
                // 그 외 예상치 못한 서버 에러
                log.error("Authentication Error: {}", e.getMessage());
                return writeUnauthorizedResponse(response, "인증 처리 중 오류가 발생했습니다.");
            }
        };
    }

    /**
     * 인증 실패 시 401 Unauthorized 응답을 JSON 형태로 반환합니다.
     * WebFlux 환경이므로 Mono<Void>를 반환하여 리액티브 흐름을 유지합니다.
     *
     * @param response ServerHttpResponse 객체
     * @param message  클라이언트에게 전달할 에러 메시지
     * @return Mono<Void> (응답 완료 신호)
     */
    private Mono<Void> writeUnauthorizedResponse(ServerHttpResponse response, String message) {
        // 상태 코드 401 설정
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        // Content-Type: application/json 설정
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        // 응답 바디 생성 (DTO -> JSON Bytes)
        TokenAuthorizationResponse body = new TokenAuthorizationResponse(message);
        byte[] bytes = writeResponseBody(body);

        // DataBuffer로 래핑하여 응답 쓰기
        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }

    /**
     * DTO 객체를 JSON 바이트 배열로 직렬화합니다.
     */
    private byte[] writeResponseBody(TokenAuthorizationResponse body) {
        try {
            return om.writeValueAsBytes(body);
        } catch (JsonProcessingException e) {
            // 직렬화 실패 시 로그를 남기고 빈 배열 반환 (실제로는 발생 확률 낮음)
            log.error("Failed to serialize response body : {}", body);
            return new byte[0];
        }
    }

    /**
     * 요청에서 JWT 토큰 문자열을 추출합니다.
     * 우선순위: 1. Authorization 헤더 (Bearer) -> 2. 쿠키 (accessToken)
     *
     * @param request ServerHttpRequest
     * @return 추출된 토큰 문자열 (Optional)
     */
    private Optional<String> resolveToken(ServerHttpRequest request) {
        // 1. Authorization 헤더 확인
        String bearerToken = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            // "Bearer " 접두사 제거 (7글자)
            return Optional.of(bearerToken.substring(7));
        }

        // 2. 쿠키 확인 (헤더에 없을 경우)
        HttpCookie cookie = request.getCookies().getFirst("accessToken");
        if (cookie != null) {
            return Optional.of(cookie.getValue());
        }

        return Optional.empty();
    }

    /**
     * JJWT 라이브러리를 사용하여 토큰을 파싱하고 검증합니다.
     * * @param token 검증할 JWT 토큰 문자열
     * @param key   서명 검증에 사용할 SecretKey
     * @return 파싱된 Claims (Payload 정보)
     * @throws JwtException 토큰이 유효하지 않거나 만료된 경우 발생
     */
    private Claims parseClaims(String token, SecretKey key) {
        return Jwts.parser()
                .verifyWith(key) // 서명 검증 키 설정
                .build()
                .parseSignedClaims(token) // 파싱 및 검증 수행
                .getPayload(); // Claims 반환
    }
}