package co.kr.user.model.entity;

import co.kr.user.model.vo.PublicDel;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 데이터베이스의 'File' 테이블과 매핑되는 엔티티 클래스입니다.
 * 시스템 내 모든 업로드 파일의 경로, 원본명, 참조 정보 등을 관리합니다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 무분별한 객체 생성을 막기 위해 protected로 설정합니다.
@AllArgsConstructor
@Builder
@DynamicInsert // null인 필드를 제외하고 insert 쿼리를 생성하여 DB 기본값 활용을 최적화합니다.
@EntityListeners(AuditingEntityListener.class) // 생성 및 수정 시간을 자동으로 기록하기 위한 리스너입니다.
@Table(name = "File")
public class File {

    /**
     * 파일 고유 식별자 (Primary Key)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "File_IDX")
    private Long fileIdx;

    /**
     * 사용자가 업로드한 원본 파일명
     */
    @Column(name = "File_Origin", nullable = false)
    private String fileOrigin;

    /**
     * 서버 또는 저장소(S3)에 저장된 고유 파일명
     */
    @Column(name = "File_Name", nullable = false)
    private String fileName;

    /**
     * 파일 확장자 또는 타입 (최대 10자)
     */
    @Column(name = "File_Type", nullable = false, length = 10)
    private String fileType;

    /**
     * 저장소 내 파일의 물리적 경로
     */
    @Column(name = "File_Path")
    private String filePath;

    /**
     * 이 파일을 소유하거나 참조하는 테이블의 이름 (예: 'Seller', 'Product')
     */
    @Column(name = "Ref_Table", nullable = false, length = 100)
    private String refTable;

    /**
     * 참조하는 테이블의 레코드 고유 식별자(PK)
     */
    @Column(name = "Ref_Index", nullable = false)
    private Long refIndex;

    /**
     * 레코드 생성 일시 (자동 기록)
     */
    @CreatedDate
    @Column(name = "Created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * 레코드 최종 수정 일시 (자동 기록)
     */
    @LastModifiedDate
    @Column(name = "Updated_at")
    private LocalDateTime updatedAt;

    /**
     * 파일의 삭제 상태 (열거형 사용)
     */
    @Column(name = "Del", nullable = false, columnDefinition = "TINYINT")
    private PublicDel del;

    /**
     * 파일을 '삭제됨' 상태로 변경하는 Soft Delete 메서드입니다.
     */
    public void markAsDeleted() {
        this.del = PublicDel.DELETED;
    }
}