package co.kr.gateway.DTO;

/**
 * 토큰 인증/인가 실패 시 클라이언트에게 반환할 공통 응답 DTO
 * <p>
 * Java Record를 사용하여 불변(Immutable) 객체로 정의되었으며,
 * 생성자, Getter, toString() 등이 자동으로 생성됩니다.
 * </p>
 */
public record TokenAuthorizationResponse(
        // 클라이언트에게 전달할 구체적인 에러 메시지 (예: "인증 토큰이 없습니다.", "유효하지 않은 토큰입니다.")
        String message
) {
}