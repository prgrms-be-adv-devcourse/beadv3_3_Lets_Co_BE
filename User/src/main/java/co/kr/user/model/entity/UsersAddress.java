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

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicInsert
@Table(name = "Users_Address")
public class UsersAddress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Address_IDX")
    private Long addressIdx;

    @Column(name = "Users_IDX", nullable = false)
    private Long usersIdx;

    @Column(name = "Address_Code", nullable = false)
    private String addressCode;

    @Convert(converter = CryptoConverter.GcmConverter.class)
    @Column(name = "Recipient", nullable = false, length = 512)
    private String recipient;

    @Convert(converter = CryptoConverter.GcmConverter.class)
    @Column(name = "Address", nullable = false, length = 2048)
    private String address;

    @Convert(converter = CryptoConverter.GcmConverter.class)
    @Column(name = "Address_Detail", nullable = false, length = 2048)
    private String addressDetail;

    @Convert(converter = CryptoConverter.GcmConverter.class)
    @Column(name = "Phone_Number", nullable = false, length = 512)
    private String phoneNumber;

    @Column(name = "Del", nullable = false, columnDefinition = "TINYINT")
    private UserDel del;

    @Builder
    public UsersAddress(Long usersIdx, String recipient, String address, String addressDetail, String phoneNumber) {
        if (usersIdx == null) throw new IllegalArgumentException("사용자 식별자는 필수입니다.");

        this.usersIdx = usersIdx;
        this.addressCode = UUID.randomUUID().toString();
        this.recipient = recipient;
        this.address = address;
        this.addressDetail = addressDetail;
        this.phoneNumber = phoneNumber;
        this.del = UserDel.ACTIVE;
    }

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

    public void deleteAddress() {
        this.del = UserDel.DELETED;
    }
}