package co.kr.user.DAO;

import co.kr.user.model.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * [회원 기본 정보 리포지토리]
 * 'Users' 테이블에 접근하여 데이터를 생성(Create), 조회(Read), 수정(Update), 삭제(Delete)하는 인터페이스입니다.
 * MyBatis와 달리 쿼리를 직접 짜지 않고, JPA가 메서드 이름을 분석하여 자동으로 쿼리를 만들어줍니다.
 *
 * * extends JpaRepository<Users, Long>
 * - Users: 이 리포지토리가 다룰 엔티티 클래스
 * - Long: 해당 엔티티의 PK(Primary Key) 타입 (Users의 usersIdx가 Long 타입임)
 */
public interface UserRepository extends JpaRepository<Users, Long> {

    /**
     * [아이디 중복 확인 쿼리 메서드]
     * Spring Data JPA의 'Query Method' 기능을 사용했습니다.
     * 메서드 이름만으로 "SELECT COUNT(*) > 0 FROM Users WHERE ID = ?" 와 같은 쿼리가 자동 생성됩니다.
     *
     * @param ID 중복인지 확인할 사용자 아이디(이메일)
     * @return 존재하면 true, 없으면 false
     */
    boolean existsByID(String ID); // 변수명(ID)이 엔티티 필드명과 정확히 일치해야 함

    Optional<Users> findByID(String ID);


}