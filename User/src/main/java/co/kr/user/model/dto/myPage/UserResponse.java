package co.kr.user.model.dto.myPage;

import co.kr.user.model.entity.Users;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * [마이페이지 기본 응답 DTO]
 * UserController의 getMyPage() 요청에 대한 응답 객체
 * 보안상 민감한 정보(비밀번호 등)를 제외하고 화면 표시에 필요한 데이터만 담음
 */
@Getter
@NoArgsConstructor  // JSON 역직렬화(Request Body 파싱 등)를 위해 기본 생성자 필요
@AllArgsConstructor // 모든 필드를 파라미터로 받는 생성자 (Builder 패턴에서 사용)
@Builder            // 객체 생성 시 가독성을 높이기 위해 빌더 패턴 적용
public class UserResponse {

    private Long userId;        // 사용자 고유 식별자 (PK)
    private String loginId;     // 로그인 아이디
    private String role;        // 권한 (USER, ADMIN 등)
    private BigDecimal balance; // 현재 잔액 (금융 정보이므로 BigDecimal 권장)
    private LocalDateTime createdAt; // 가입 일자

    /**
     * [Entity -> DTO 변환 메서드]
     * User 엔티티(DB 원본 데이터)를 받아서 응답용 DTO(UserResponse)로 변환
     * 정적 팩토리 메서드 패턴(from)을 사용하여 객체 생성 로직을 캡슐화
     */
    public static UserResponse from(Users user) {
        return UserResponse.builder()
                .userId(user.getUsersIdx())
                .loginId(user.getID())
                .role(user.getRole().name()) // Enum 타입을 문자열로 변환
                .balance(user.getBalance())
                .createdAt(user.getCreatedAt())
                .build();
    }
}




