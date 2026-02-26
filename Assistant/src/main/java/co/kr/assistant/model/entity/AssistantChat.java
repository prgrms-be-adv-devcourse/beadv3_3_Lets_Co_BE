package co.kr.assistant.model.entity;

import co.kr.assistant.model.vo.PublicDel;
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

    @Column(name = "Prompt", columnDefinition = "MEDIUMTEXT")
    private String prompt;

    @Column(name = "Question", nullable = false, columnDefinition = "MEDIUMTEXT")
    private String question;

    @Column(name = "Answer", columnDefinition = "MEDIUMTEXT")
    private String answer;

    // [추가] 모니터링용 컬럼
    @Column(name = "Prompt_Tokens")
    private Integer promptTokens;

    @Column(name = "Answer_Tokens")
    private Integer answerTokens;

    @Column(name = "Total_Tokens")
    private Integer totalTokens;

    @Column(name = "Duration_MS")
    private Long durationMs;

    @Column(name = "Created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "Del", nullable = false, columnDefinition = "TINYINT")
    private PublicDel del;

    @Builder
    public AssistantChat(Assistant assistant, String question) {
        this.assistant = assistant;
        this.question = question;
        this.createdAt = LocalDateTime.now();
        this.del = PublicDel.ACTIVE;
    }

    /**
     * AI 응답 정보와 모니터링 데이터를 업데이트합니다.
     */
    public void updateResponse(String prompt, String answer, Integer promptTokens, Integer answerTokens, Integer totalTokens, Long durationMs) {
        this.prompt = prompt;
        this.answer = answer;
        this.promptTokens = promptTokens;
        this.answerTokens = answerTokens;
        this.totalTokens = totalTokens;
        this.durationMs = durationMs;
    }
}