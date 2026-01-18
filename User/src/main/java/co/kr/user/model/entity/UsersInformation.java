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
@Table(name = "Users_Information")
public class UsersInformation {
    @Id
    @Column(name = "Users_IDX")
    private Long usersIdx;

    @Column(name = "Pre_PW", nullable = true)
    private String prePW;

    @Column(name = "Name", nullable = false)
    private String name;

    @Column(name = "Phone_Number", nullable = false)
    private String phoneNumber;

    @Column(name = "Birth", nullable = false)
    private String birth;

    @Column(name = "Del", nullable = false)
    @ColumnDefault("0")
    private int del = 0;

    @Builder
    public UsersInformation(Long usersIdx, String name, String phoneNumber, String birth) {
        this.usersIdx = usersIdx;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.birth = birth;
    }

    public void amend(String name, String phoneNumber, String birth) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.birth = birth;
    }

    public void lastPassword(String PW) {
        this.prePW = PW;
    }
}