package co.kr.assistant.model.dto.list;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatListDTO {
    private String question;
    private String answer;
    private LocalDateTime time;
}