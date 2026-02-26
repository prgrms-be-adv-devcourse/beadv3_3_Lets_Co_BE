package co.kr.customerservice.client.dto;

import co.kr.customerservice.common.model.vo.UserRole;

public record ClientRoleDTO(
        UserRole role,
        Long usersIdx,
        String userName,
        Long sellerIdx,
        String sellerName
) {
}
