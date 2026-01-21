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
 * 사용자 상세 정보(이름, 전화번호, 생년월일 등)를 관리하는 엔티티 클래스입니다.
 * Users 엔티티와 분리하여 민감한 개인정보나 부가 정보를 별도로 저장하는 역할을 합니다.
 */
@Entity // JPA가 관리하는 엔티티 객체임을 명시합니다.
@Getter // 모든 필드에 대한 Getter 메서드를 자동으로 생성합니다 (Lombok).
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 파라미터가 없는 기본 생성자를 생성하며, 접근 권한을 PROTECTED로 설정하여 무분별한 객체 생성을 막습니다.
@EntityListeners(AuditingEntityListener.class) // JPA Auditing 기능을 사용하여 엔티티의 생성/수정 시간을 자동으로 관리할 수 있게 리스너를 등록합니다.
@DynamicInsert // 쿼리 발생 시 null인 필드는 제외하고, 값이 있는 필드만으로 INSERT SQL을 생성합니다 (Default 값 적용 용이).
@Table(name = "Users_Information") // 데이터베이스의 "Users_Information" 테이블과 매핑됩니다.
public class UsersInformation {
    /**
     * 사용자의 고유 식별자(PK)입니다.
     * Users 테이블의 PK와 동일한 값을 사용하여 1:1 관계를 형성하거나 참조합니다.
     */
    @Id
    @Column(name = "Users_IDX")
    private Long usersIdx;

    /**
     * 이전 비밀번호를 저장하는 필드입니다.
     * 비밀번호 변경 시 기존 비밀번호를 기록하여 재사용 방지 등의 보안 정책에 활용될 수 있습니다.
     * (NULL 허용)
     */
    @Column(name = "Pre_PW", nullable = true)
    private String prePW;

    /**
     * 사용자의 이름입니다.
     * AES-256 등의 알고리즘으로 암호화되어 저장될 수 있습니다.
     * (NULL 비허용)
     */
    @Column(name = "Name", nullable = false)
    private String name;

    /**
     * 사용자의 휴대전화 번호입니다.
     * 개인정보 보호를 위해 암호화되어 저장될 수 있습니다.
     * (NULL 비허용)
     */
    @Column(name = "Phone_Number", nullable = false)
    private String phoneNumber;

    /**
     * 사용자의 생년월일입니다.
     * (NULL 비허용)
     */
    @Column(name = "Birth", nullable = false)
    private String birth;

    /**
     * 회원 탈퇴 여부 또는 삭제 상태를 나타내는 플래그입니다.
     * 0: 정상 (활성 상태)
     * 1: 삭제됨 (탈퇴 상태)
     * 기본값은 0으로 설정됩니다.
     */
    @Column(name = "Del", nullable = false)
    @ColumnDefault("0")
    private int del = 0;

    /**
     * 엔티티 생성을 위한 빌더 패턴 생성자입니다.
     * 필수적인 정보(식별자, 이름, 전화번호, 생년월일)를 입력받아 객체를 초기화합니다.
     *
     * @param usersIdx 사용자 고유 식별자
     * @param name 사용자 이름
     * @param phoneNumber 휴대전화 번호
     * @param birth 생년월일
     */
    @Builder
    public UsersInformation(Long usersIdx, String name, String phoneNumber, String birth) {
        this.usersIdx = usersIdx;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.birth = birth;
    }

    /**
     * 사용자 정보를 수정하는 비즈니스 메서드입니다.
     * Setter를 직접 사용하는 대신, 명확한 의도를 가진 메서드를 통해 상태를 변경합니다.
     *
     * @param name 변경할 이름
     * @param phoneNumber 변경할 전화번호
     * @param birth 변경할 생년월일
     */
    public void amend(String name, String phoneNumber, String birth) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.birth = birth;
    }

    /**
     * 현재 사용 중인 비밀번호를 이전 비밀번호(Pre_PW) 필드로 백업하는 메서드입니다.
     * 비밀번호 변경 로직 수행 전에 호출되어 이전 이력을 남깁니다.
     *
     * @param PW 현재 비밀번호 (변경 전)
     */
    public void lastPassword(String PW) {
        this.prePW = PW;
    }
}