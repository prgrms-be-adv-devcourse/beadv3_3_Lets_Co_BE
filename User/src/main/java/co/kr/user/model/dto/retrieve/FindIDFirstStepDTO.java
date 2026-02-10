package co.kr.user.model.dto.retrieve;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FindIDFirstStepDTO {
    private String mail;
    private LocalDateTime certificationTime;
}