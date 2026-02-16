package co.kr.user.model.entity;

import co.kr.user.model.vo.UserDel;
import co.kr.user.util.CryptoConverter;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.springframework.util.StringUtils;

import java.util.UUID;

/**
 * 사용자의 배송지(주소록) 정보를 관리하는 Entity 클래스입니다.
 * 한 명의 사용자는 여러 개의 배송지를 가질 수 있습니다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert
@Table(name = "Users_Address")
public class UsersAddress {
    /** 주소 고유 식별자 (PK) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Address_IDX")
    private Long addressIdx;

    /** 소유자 식별자 (Users 엔티티와의 연관 관계) */
    @Column(name = "Users_IDX", nullable = false)
    private Long usersIdx;

    /** 외부 노출용 주소 고유 코드 (UUID) */
    @Column(name = "Address_Code", nullable = false)
    private String addressCode;

    /** 수령인 이름 (암호화 저장) */
    @Convert(converter = CryptoConverter.GcmConverter.class)
    @Column(name = "Recipient", nullable = false, length = 512)
    private String recipient;

    /** 기본 주소 (암호화 저장) */
    @Convert(converter = CryptoConverter.GcmConverter.class)
    @Column(name = "Address", nullable = false, length = 2048)
    private String address;

    /** 상세 주소 (암호화 저장) */
    @Convert(converter = CryptoConverter.GcmConverter.class)
    @Column(name = "Address_Detail", nullable = false, length = 2048)
    private String addressDetail;

    /** 수령인 연락처 (암호화 저장) */
    @Convert(converter = CryptoConverter.GcmConverter.class)
    @Column(name = "Phone_Number", nullable = false, length = 512)
    private String phoneNumber;

    /** 삭제 여부 상태 */
    @Column(name = "Del", nullable = false, columnDefinition = "TINYINT")
    private UserDel del;

    @Builder
    public UsersAddress(Long usersIdx, String recipient, String address, String addressDetail, String phoneNumber) {
        if (usersIdx == null) throw new IllegalArgumentException("사용자 식별자는 필수입니다.");

        this.usersIdx = usersIdx;
        this.addressCode = UUID.randomUUID().toString(); // 생성 시 랜덤 코드 자동 할당
        this.recipient = recipient;
        this.address = address;
        this.addressDetail = addressDetail;
        this.phoneNumber = phoneNumber;
        this.del = UserDel.ACTIVE;
    }

    /** 주소 정보를 업데이트합니다. */
    public void updateAddress(String recipient, String address, String addressDetail, String phoneNumber) {
        if (StringUtils.hasText(recipient)) {
            this.recipient = recipient.trim();
        }
        if (StringUtils.hasText(address)) {
            this.address = address.trim();
        }
        if (StringUtils.hasText(addressDetail)) {
            this.addressDetail = addressDetail.trim();
        }
        if (StringUtils.hasText(phoneNumber)) {
            this.phoneNumber = phoneNumber.trim();
        }
    }

    /** 주소를 논리적으로 삭제 처리합니다. */
    public void deleteAddress() {
        this.del = UserDel.DELETED;
    }
}