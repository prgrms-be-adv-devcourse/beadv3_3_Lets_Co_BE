package co.kr.user.model.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * [회원 상세 정보 엔티티]
 * DB 테이블: Users_Information
 * 역할: 로그인에 필요한 핵심 정보 외의 부가적인 개인정보를 관리
 */
@Entity
@Table(name = "Users_Information")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA 스펙상 기본 생성자 필요 (외부에서 무분별한 생성 방지)
public class UserInformation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Users_IDX") // DB 컬럼명 매핑 (PK이자 FK인 식별 관계 구조로 보임)
    private Long id;

    /*
     * [연관 관계 매핑]
     * User 엔티티와 1:1 관계
     * FetchType.LAZY: 이 객체를 조회할 때 User 정보를 당장 쓰지 않는다면 쿼리를 날리지 않음 (지연 로딩)
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "Users_IDX") // 외래키 컬럼 지정
    private User user;

    @Column(name = "Name")
    private String name;

    @Column(name = "Phone_Number")
    private String phoneNumber;

    @Column(name = "Birth")
    private String birth;

    // Entity는 Setter를 지양하고, 값 변경이 필요하면 의미 있는 비즈니스 메서드를 추가
}


