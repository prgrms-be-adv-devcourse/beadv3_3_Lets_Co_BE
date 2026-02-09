package co.kr.product.product.model.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "File")
public class FileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "File_IDX")
    private Long id;

    @Column(name = "File_Origin")
    private String originalFileName;

    @Column(name = "File_Name")
    private String storedFileName;

    @Column(name = "File_Type", length = 10)
    private String fileType; // ex) JPG, PNG

    @Column(name = "File_Path")
    private String filePath;

    // 다형성을 위한 컬럼 (어떤 테이블의 파일인가?)
    @Column(name = "Ref_Table", length = 100)
    private String refTable;

    // 다형성을 위한 컬럼 (해당 테이블의 어떤 ID인가?)
    @Column(name = "Ref_Index")
    private Long refIndex;

    @CreatedDate
    @Column(name = "Created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "Updated_at")
    private LocalDateTime updatedAt;

    // Soft Delete 처리를 위한 컬럼 (0: 정상, 1: 삭제)
    @Column(name = "Del", columnDefinition = "TINYINT(1) DEFAULT 0")
    private boolean del = false;


    @Builder
    public FileEntity(Long refIndex,
                      String filePath,
                      String fileType,
                      String storedFileName,
                      String originalFileName) {

        this.refIndex = refIndex;
        this.refTable = "Products";
        this.filePath = filePath;
        this.fileType = fileType;
        this.storedFileName = storedFileName;
        this.originalFileName = originalFileName;
    }
}
