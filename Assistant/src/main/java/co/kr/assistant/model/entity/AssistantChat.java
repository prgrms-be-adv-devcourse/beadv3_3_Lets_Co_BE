package co.kr.assistant.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Assistant_Chat")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AssistantChat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Chat_IDX")
    private Long chatIdx;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "Assistant_IDX", nullable = false)
    private Assistant assistant;

    @Column(name = "Prompt", nullable = false, columnDefinition = "MEDIUMTEXT")
    private String prompt;

    @Column(name = "Question", nullable = false, columnDefinition = "MEDIUMTEXT")
    private String question;

    @Column(name = "Answer", nullable = false, columnDefinition = "MEDIUMTEXT")
    private String answer;

    @Column(name = "Created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "Del", nullable = false, columnDefinition = "TINYINT")
    private Integer del = 0;

    @Builder
    public AssistantChat(Assistant assistant, String prompt, String question, String answer) {
        this.assistant = assistant;
        this.prompt = prompt;
        this.question = question;
        this.answer = answer;
        this.createdAt = LocalDateTime.now();
        this.del = 0;
    }
}