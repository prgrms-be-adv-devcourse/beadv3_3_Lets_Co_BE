package co.kr.user.model.DTO.admin;

import co.kr.user.model.vo.UsersRole;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdminRoleReq {
    @NotNull(message = "권한(Role)은 필수 값입니다.")
    private UsersRole role;
}
