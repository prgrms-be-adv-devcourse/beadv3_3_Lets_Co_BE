package co.kr.user.model.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
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

    @Column(name = "Default_Address", nullable = false)
    @ColumnDefault("0")
    private int defaultAddress = 0;

    @Column(name = "Recipient", nullable = false)
    private String recipient;

    @Column(name = "Address", nullable = false)
    private String address;

    @Column(name = "Address_Detail", nullable = false)
    private String addressDetail;

    @Column(name = "Phone_Number", nullable = false)
    private String phoneNumber;

    @Column(name = "Del", nullable = false)
    @ColumnDefault("0")
    private int del = 0;

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

    public void updateAddress(int defaultAddress, String recipient, String address, String addressDetail, String phoneNumber) {
        this.defaultAddress = defaultAddress;
        this.recipient = recipient;
        this.address = address;
        this.addressDetail = addressDetail;
        this.phoneNumber = phoneNumber;
    }

    public void defaultAddress() {
        this.defaultAddress = 1;
    }

    public void del() {
        this.del = 1;
    }
}