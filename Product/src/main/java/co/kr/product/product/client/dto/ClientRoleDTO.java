package co.kr.product.product.client.dto;

import co.kr.product.common.vo.UserRole;

public record ClientRoleDTO(
        UserRole role,
        Long usersIdx,
        String userName,
        Long sellerIdx,
        String sellerName
) {
}
