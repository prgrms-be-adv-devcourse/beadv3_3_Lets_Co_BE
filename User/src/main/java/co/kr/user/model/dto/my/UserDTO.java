package co.kr.user.model.dto.my;

import co.kr.user.model.vo.UsersMembership;
import co.kr.user.model.vo.UsersRole;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 사용자의 계정 기본 정보와 현재 자산 상태를 포함하는 응답 DTO입니다.
 * 마이페이지 메인 화면 등에서 사용자의 등급, 잔액, 가입일 등을 보여줄 때 사용됩니다.
 */
@Data
public class UserDTO {
    /** * 사용자의 로그인 아이디입니다.
     * 화면에 표시하거나, 계정을 식별하는 용도로 사용됩니다.
     */
    private String id;

    /** * 사용자의 시스템 권한(Role)입니다. (예: USERS, ADMIN, SELLER)
     * 클라이언트에서 메뉴 노출 여부나 기능 접근 권한을 제어할 때 활용됩니다.
     */
    private UsersRole role;

    /** * 사용자의 현재 멤버십 등급입니다. (예: STANDARD, VIP 등)
     * 등급에 따른 혜택이나 아이콘을 표시하는 데 사용됩니다.
     */
    private UsersMembership membership;

    /** * 계정 생성(회원가입) 일시입니다.
     * 가입 기간에 따른 혜택 제공이나 정보를 표시할 때 사용됩니다.
     */
    private LocalDateTime createdAt;

    /** * 사용자가 현재 보유하고 있는 예치금 또는 포인트 잔액입니다.
     * 결제나 충전 시 사용 가능한 금액을 사용자에게 보여줍니다.
     */
    private BigDecimal balance;
}