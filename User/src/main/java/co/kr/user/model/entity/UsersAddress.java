package co.kr.user.model.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * 사용자의 배송지(주소) 정보를 관리하는 엔티티 클래스입니다.
 * 쇼핑몰 등에서 상품 배송을 위해 필요한 수령인, 주소, 연락처 등을 저장합니다.
 * 한 명의 사용자가 여러 개의 주소를 가질 수 있으며, 그중 하나를 기본 배송지로 설정할 수 있습니다.
 */
@Entity // JPA 엔티티임을 명시합니다.
@Getter // 모든 필드의 Getter 메서드를 자동 생성합니다.
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 기본 생성자의 접근 제어를 PROTECTED로 설정하여 무분별한 객체 생성을 방지합니다.
@EntityListeners(AuditingEntityListener.class) // 엔티티의 변경 사항(Auditing)을 감지하는 리스너를 등록합니다.
@DynamicInsert // INSERT 시 null인 필드를 제외하고 쿼리를 생성하여, DB 또는 @ColumnDefault 값이 적용되게 합니다.
@Table(name = "Users_Address") // DB의 "Users_Address" 테이블과 매핑됩니다.
public class UsersAddress {
    /**
     * 주소 정보의 고유 식별자(PK)입니다.
     * 데이터베이스에서 자동으로 생성되는 번호(Auto Increment)를 사용합니다.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Address_IDX")
    private Long addressIdx;

    /**
     * 해당 주소를 소유한 사용자의 식별자(FK)입니다.
     * Users 테이블의 PK를 참조합니다.
     */
    @Column(name = "Users_IDX", nullable = false)
    private Long usersIdx;

    /**
     * 주소 관리를 위한 고유 코드(UUID 등)입니다.
     * API 통신 등에서 내부 PK(addressIdx) 대신 사용하여 보안성을 높일 수 있습니다.
     */
    @Column(name = "Address_Code", nullable = false)
    private String addressCode;

    /**
     * 기본 배송지 여부를 나타냅니다.
     * 0: 일반 주소
     * 1: 기본 배송지
     * 기본값은 0입니다.
     */
    @Column(name = "Default_Address", nullable = false)
    @ColumnDefault("0")
    private int defaultAddress = 0;

    /**
     * 택배 수령인 이름입니다.
     * 개인정보 보호를 위해 암호화되어 저장될 수 있습니다.
     */
    @Column(name = "Recipient", nullable = false)
    private String recipient;

    /**
     * 기본 주소(도로명 주소, 지번 주소 등)입니다.
     * 개인정보 보호를 위해 암호화되어 저장될 수 있습니다.
     */
    @Column(name = "Address", nullable = false, length = 512)
    private String address;

    /**
     * 상세 주소(동, 호수 등)입니다.
     * 개인정보 보호를 위해 암호화되어 저장될 수 있습니다.
     */
    @Column(name = "Address_Detail", nullable = false, length = 512)
    private String addressDetail;

    /**
     * 수령인 연락처입니다.
     * 개인정보 보호를 위해 암호화되어 저장될 수 있습니다.
     */
    @Column(name = "Phone_Number", nullable = false)
    private String phoneNumber;

    /**
     * 주소 삭제 여부를 나타내는 플래그입니다.
     * 0: 정상 (사용 가능)
     * 1: 삭제됨
     * 기본값은 0입니다.
     */
    @Column(name = "Del", nullable = false)
    @ColumnDefault("0")
    private int del = 0;

    /**
     * 주소 객체 생성을 위한 빌더입니다.
     *
     * @param usersIdx 사용자 식별자
     * @param addressCode 주소 코드
     * @param defaultAddress 기본 배송지 여부
     * @param recipient 수령인
     * @param address 주소
     * @param addressDetail 상세 주소
     * @param phoneNumber 연락처
     */
    @Builder
    public UsersAddress(Long usersIdx, String addressCode, int defaultAddress, String recipient, String address, String addressDetail, String phoneNumber) {
        this.usersIdx = usersIdx;
        this.addressCode = addressCode;
        this.defaultAddress = defaultAddress;
        this.recipient = recipient;
        this.address = address;
        this.addressDetail = addressDetail;
        this.phoneNumber = phoneNumber;
    }

    /**
     * 주소 정보를 수정하는 메서드입니다.
     *
     * @param defaultAddress 기본 배송지 설정 여부
     * @param recipient 수령인 변경
     * @param address 주소 변경
     * @param addressDetail 상세 주소 변경
     * @param phoneNumber 연락처 변경
     */
    public void updateAddress(int defaultAddress, String recipient, String address, String addressDetail, String phoneNumber) {
        this.defaultAddress = defaultAddress;
        this.recipient = recipient;
        this.address = address;
        this.addressDetail = addressDetail;
        this.phoneNumber = phoneNumber;
    }

    /**
     * 해당 주소를 기본 배송지로 설정하는 메서드입니다.
     * Default_Address 값을 1로 변경합니다.
     * (참고: 기존 기본 배송지를 해제하는 로직은 서비스 계층에서 별도로 처리해야 합니다.)
     */
    public void defaultAddress() {
        this.defaultAddress = 1;
    }

    /**
     * 주소를 삭제 처리하는 메서드입니다.
     * DB에서 데이터를 완전히 삭제(Hard Delete)하지 않고,
     * Del 플래그를 1로 변경하여 화면에 노출되지 않도록 처리(Soft Delete)합니다.
     */
    public void del() {
        this.del = 1;
    }
}