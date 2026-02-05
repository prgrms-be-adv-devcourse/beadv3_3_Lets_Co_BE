package co.kr.user.model.dto.register;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RegisterDTO {
    private String ID;
    private LocalDateTime certificationTime;
}