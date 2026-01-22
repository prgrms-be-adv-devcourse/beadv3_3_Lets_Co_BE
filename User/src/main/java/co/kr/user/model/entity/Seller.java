package co.kr.user.model.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * 판매자 정보를 관리하는 엔티티 클래스입니다.
 * 일반 사용자가 판매자 권한을 획득했을 때 생성되며, 사업자 등록 번호와 정산 받을 계좌 정보 등을 저장합니다.
 * Users 엔티티와 식별자(PK)를 공유하는 방식으로 1:1 관계를 형성하는 구조로 보입니다.
 */
@Entity // JPA 엔티티임을 명시합니다.
@Getter // 모든 필드의 Getter 메서드를 자동 생성합니다.
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 기본 생성자의 접근을 PROTECTED로 제한합니다.
@EntityListeners(AuditingEntityListener.class) // 엔티티 생성/수정 시점을 자동 감지하기 위한 리스너를 등록합니다.
@DynamicInsert // INSERT 쿼리 생성 시 null인 필드를 제외하여 DB 기본값(Default)을 적용합니다.
@Table(name = "Seller") // 데이터베이스의 "Seller" 테이블과 매핑됩니다.
public class Seller {
    /**
     * 판매자 고유 식별자(PK)입니다.
     * Users 테이블의 Users_IDX와 동일한 값을 사용하여, 해당 유저가 판매자임을 나타냅니다.
     * (별도의 Auto Increment를 사용하지 않고 Users의 ID를 그대로 사용하는 식별 관계 전략으로 추정됩니다.)
     */
    @Id
    @Column(name = "Seller_IDX", nullable = false)
    private Long sellerIdx;

    /**
     * 사업자 등록 번호입니다.
     * 판매 활동을 위한 필수 정보입니다.
     */
    @Column(name = "Business_License", nullable = false, length = 255)
    private String businessLicense;

    /**
     * 정산 받을 은행명(브랜드)입니다.
     * 예: 신한은행, 국민은행 등
     */
    @Column(name = "Bank_Brand", nullable = false, length = 255)
    private String bankBrand;

    /**
     * 예금주 명 또는 은행 지점명 등 구체적인 계좌 명칭입니다.
     */
    @Column(name = "Bank_Name", nullable = false, length = 255)
    private String bankName;

    /**
     * 암호화된 계좌 정보(계좌 번호 또는 핀테크 토큰)입니다.
     * 민감한 금융 정보이므로 반드시 암호화되어 저장되어야 합니다.
     */
    @Column(name = "Bank_Token", nullable = false, length = 1024)
    private String bankToken;

    /**
     * 판매자 등록 신청 일시(생성 일시)입니다.
     * JPA Auditing에 의해 자동으로 값이 설정됩니다.
     * (String 타입으로 선언되어 있어 날짜 포맷팅된 문자열이 저장될 것으로 보입니다.)
     */
    @CreatedDate
    @Column(name = "Created_at", nullable = false, updatable = false)
    private String createdAt;

    /**
     * 정보 수정 일시입니다.
     * (주의: 현재 코드에서는 @CreatedDate가 붙어 있어 생성 시점에만 값이 설정되고 이후 자동 갱신되지 않을 수 있습니다.
     * 통상적으로는 @LastModifiedDate를 사용합니다.)
     */
    @CreatedDate
    @Column(name = "Updated_at", nullable = false, updatable = false)
    private String updatedAt;

    /**
     * 판매자 계정의 상태를 나타내는 플래그입니다.
     * 0: 정상 (인증 완료, 활동 가능)
     * 1: 삭제됨/탈퇴
     * 2: 심사 대기/인증 대기
     * 기본값은 2로 설정되어 등록 직후에는 승인이 필요한 상태임을 나타냅니다.
     */
    @Column(name = "Del")
    @ColumnDefault("2")
    private int del;

    /**
     * 판매자 정보 생성을 위한 빌더입니다.
     *
     * @param sellerIdx 유저 식별자 (FK이자 PK)
     * @param businessLicense 사업자 등록 번호
     * @param bankBrand 은행명
     * @param bankName 예금주/계좌명
     * @param bankToken 암호화된 계좌 정보
     */
    @Builder
    public Seller(Long sellerIdx, String businessLicense, String bankBrand, String bankName, String bankToken) {
        this.sellerIdx = sellerIdx;
        this.businessLicense = businessLicense;
        this.bankBrand = bankBrand;
        this.bankName = bankName;
        this.bankToken = bankToken;
    }

    /**
     * 판매자 등록 승인(인증 완료) 처리 메서드입니다.
     * 관리자 승인이나 이메일 인증 등이 완료되었을 때 호출되어 상태(Del)를 0(정상)으로 변경합니다.
     */
    public void confirmVerification() {
        this.del = 0;
    }
}