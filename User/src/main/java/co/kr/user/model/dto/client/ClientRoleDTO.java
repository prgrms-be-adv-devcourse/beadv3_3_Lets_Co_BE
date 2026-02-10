package co.kr.user.model.dto.client;

import co.kr.user.model.vo.UsersRole;
import lombok.Data;

@Data
public class ClientRoleDTO {
    private Long idx;
    private UsersRole role;
}
