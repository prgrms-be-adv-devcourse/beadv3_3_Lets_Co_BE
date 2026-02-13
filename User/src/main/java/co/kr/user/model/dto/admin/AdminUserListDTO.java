package co.kr.user.model.dto.admin;

import co.kr.user.model.vo.UsersMembership;
import co.kr.user.model.vo.UsersRole;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminUserListDTO {
    private UsersRole role;
    private String id;
    private UsersMembership membership;
    private String name;
    private LocalDateTime lockedUntil;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
