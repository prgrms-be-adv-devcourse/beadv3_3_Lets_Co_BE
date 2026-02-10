package co.kr.user.model.entity;

import co.kr.user.util.CryptoConverter;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;


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

    @Convert(converter = CryptoConverter.class)
    @Column(name = "Recipient", nullable = false, length = 512)
    private String recipient;

    @Convert(converter = CryptoConverter.class)
    @Column(name = "Address", nullable = false, length = 2048)
    private String address;

    @Convert(converter = CryptoConverter.class)
    @Column(name = "Address_Detail", nullable = false, length = 2048)
    private String addressDetail;

    @Convert(converter = CryptoConverter.class)
    @Column(name = "Phone_Number", nullable = false, length = 512)
    private String phoneNumber;

    @Column(name = "Del", nullable = false, columnDefinition = "TINYINT")
    private int del;

    @Builder
    public UsersAddress(Long usersIdx, String addressCode, String recipient, String address, String addressDetail, String phoneNumber) {
        this.usersIdx = usersIdx;
        this.addressCode = addressCode;
        this.recipient = recipient;
        this.address = address;
        this.addressDetail = addressDetail;
        this.phoneNumber = phoneNumber;
    }

    public void updateAddress( String recipient, String address, String addressDetail, String phoneNumber) {
        this.recipient = recipient;
        this.address = address;
        this.addressDetail = addressDetail;
        this.phoneNumber = phoneNumber;
    }

    public void deleteAddress() {
        this.del = 1;
    }
}