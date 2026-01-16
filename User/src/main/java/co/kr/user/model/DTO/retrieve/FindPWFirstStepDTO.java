package co.kr.user.model.DTO.retrieve;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FindPWFirstStepDTO {

    private String ID;
    private LocalDateTime certificationTime;
}
