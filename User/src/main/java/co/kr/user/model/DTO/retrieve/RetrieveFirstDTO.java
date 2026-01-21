package co.kr.user.model.DTO.retrieve;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RetrieveFirstDTO {

    private String ID;
    private LocalDateTime certificationTime;
}
