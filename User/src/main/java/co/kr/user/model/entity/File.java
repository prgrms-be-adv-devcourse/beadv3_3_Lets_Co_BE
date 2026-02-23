package co.kr.user.model.entity;

import co.kr.user.model.vo.PublicDel;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@DynamicInsert
@EntityListeners(AuditingEntityListener.class)
@Table(name = "File")
public class File {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "File_IDX")
    private Long fileIdx;

    @Column(name = "File_Origin", nullable = false)
    private String fileOrigin;

    @Column(name = "File_Name", nullable = false)
    private String fileName;

    @Column(name = "File_Type", nullable = false, length = 10)
    private String fileType;

    @Column(name = "File_Path")
    private String filePath;

    @Column(name = "Ref_Table", nullable = false, length = 100)
    private String refTable;

    @Column(name = "Ref_Index", nullable = false)
    private Long refIndex;

    @CreatedDate
    @Column(name = "Created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "Updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "Del", nullable = false, columnDefinition = "TINYINT")
    // 만약 별도의 Converter가 있다면 @Convert(converter = PublicDelConverter.class) 추가
    private PublicDel del;

    /** Soft Delete를 위한 메서드 */
    public void markAsDeleted() {
        this.del = PublicDel.DELETED;
    }
}