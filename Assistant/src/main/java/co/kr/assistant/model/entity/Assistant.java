package co.kr.assistant.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;
import java.time.LocalDateTime;

@Entity
@Table(name = "Assistant")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicUpdate // 변경된 필드만 update문에 포함
public class Assistant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Assistant_IDX")
    private Long assistantIdx;

    @Column(name = "Assistant_Code", nullable = false, unique = true, length = 64)
    private String assistantCode; // UUID 토큰

    @Column(name = "Users_IDX")
    private Long usersIdx; // 회원인 경우 저장, 비회원은 null

    @Column(name = "IP_Address", nullable = false, length = 45)
    private String ipAddress;

    @Column(name = "User_Agent", length = 512)
    private String userAgent;

    @Column(name = "Created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "Updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "Last_Activity_at", nullable = false)
    private LocalDateTime lastActivityAt;

    @Column(name = "Del", nullable = false, columnDefinition = "TINYINT")
    private Integer del = 0;

    @Builder
    public Assistant(String assistantCode, Long usersIdx, String ipAddress, String userAgent) {
        this.assistantCode = assistantCode;
        this.usersIdx = usersIdx;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.lastActivityAt = LocalDateTime.now();
        this.del = 0;
    }

    // 활동 시간 갱신 메서드
    public void updateActivity() {
        this.lastActivityAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}