package co.kr.user.model.DTO.my;

import co.kr.user.model.vo.UsersRole;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UserDTO {

    private String ID;
    private UsersRole role;
    private BigDecimal balance;
    private LocalDateTime createdAt;


}