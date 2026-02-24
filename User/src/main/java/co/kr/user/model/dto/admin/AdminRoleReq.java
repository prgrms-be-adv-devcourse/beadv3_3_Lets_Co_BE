package co.kr.user.model.dto.admin;

import co.kr.user.model.vo.UsersRole;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 관리자가 회원의 권한(Role)을 변경하고자 할 때 사용하는 DTO입니다.
 */
@Data
public class AdminRoleReq {
    /**
     * 변경할 새로운 권한 정보(예: ADMIN, USERS, SELLER)입니다. (필수 값)
     */
    @NotNull(message = "권한(Role)은 필수 값입니다.")
    private UsersRole role;
}