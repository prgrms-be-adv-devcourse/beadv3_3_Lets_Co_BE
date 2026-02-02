package co.kr.user.model.dto.admin;

import co.kr.user.model.vo.UsersRole;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminUserListDTO {
    private String ID;
    private String name;
    private String phoneNumber;
    private String birth;
    private UsersRole role;
    private LocalDateTime lockedUntil;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
