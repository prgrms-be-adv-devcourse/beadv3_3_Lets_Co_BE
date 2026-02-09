package co.kr.user.model.dto.retrieve;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RetrieveFirstDTO {

    private String ID;
    private LocalDateTime certificationTime;
}
