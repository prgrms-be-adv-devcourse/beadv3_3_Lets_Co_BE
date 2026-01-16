package co.kr.user.model.DTO.my;

import co.kr.user.model.vo.UsersRole;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * [마이페이지 기본 응답 DTO]
 * UserController의 getMyPage() 요청에 대한 응답 객체
 * 보안상 민감한 정보(비밀번호 등)를 제외하고 화면 표시에 필요한 데이터만 담음
 */
@Data
public class UserDTO {

    private String ID;     // 로그인 아이디
    private UsersRole role;        // 권한 (USER, ADMIN 등)
    private BigDecimal balance; // 현재 잔액 (금융 정보이므로 BigDecimal 권장)
    private LocalDateTime createdAt; // 가입 일자


}




