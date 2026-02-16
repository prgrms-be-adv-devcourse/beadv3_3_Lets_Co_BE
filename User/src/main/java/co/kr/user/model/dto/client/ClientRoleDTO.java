package co.kr.user.model.dto.client;

import co.kr.user.model.vo.UsersRole;
import lombok.Data;

/**
 * 시스템 내부적으로 사용자의 현재 권한(Role) 상태를 확인하기 위해 주고받는 DTO입니다.
 * 권한 기반 서비스 제공 여부를 판단할 때 사용됩니다.
 */
@Data
public class ClientRoleDTO {
    /** 사용자의 시스템 권한 (ADMIN, USERS, SELLER) */
    private UsersRole role;
}