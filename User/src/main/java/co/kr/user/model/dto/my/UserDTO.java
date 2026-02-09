package co.kr.user.model.dto.my;

import co.kr.user.model.vo.UsersMembership;
import co.kr.user.model.vo.UsersRole;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UserDTO {

    private String id;
    private UsersRole role;
    private UsersMembership membership;
    private LocalDateTime createdAt;


}