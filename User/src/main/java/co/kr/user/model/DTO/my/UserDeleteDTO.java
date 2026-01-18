package co.kr.user.model.DTO.my;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserDeleteDTO {
    private String ID;
    private LocalDateTime certificationTime;
}