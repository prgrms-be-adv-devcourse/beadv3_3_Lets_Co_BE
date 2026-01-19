package co.kr.user.DAO;

import co.kr.user.model.entity.Users;
import co.kr.user.model.entity.Users_Information;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * [상세 개인정보 리포지토리]
 * 'Users_Information' 테이블에 접근하여 CRUD(생성, 조회, 수정, 삭제) 작업을 수행하는 인터페이스입니다.
 * * * Users(기본 계정) vs Users_Information(상세 정보) 분리 이유:
 * - 자주 조회되는 로그인 정보(ID, PW)와 상대적으로 덜 조회되는 개인정보(주소, 생일 등)를 분리하여
 * DB 성능을 최적화하고, 보안 레벨을 다르게 관리하기 위함입니다.
 * * * extends JpaRepository<Users_Information, Long>
 * - Users_Information: 관리할 엔티티 객체
 * - Long: 해당 엔티티의 PK(Primary Key) 데이터 타입 (usersIdx)
 */
public interface UserInformationRepository extends JpaRepository<Users_Information, Long> {

    // [기본 CRUD 메서드 자동 제공]
    // 이 인터페이스 내부가 비어있어도, Spring Data JPA가 실행 시점에 자동으로 구현체를 만들어 주입합니다.
    // 따라서 다음과 같은 메서드들을 즉시 사용할 수 있습니다:
    // - save(entity): 저장 및 수정 (Insert / Update)
    // - findById(id): PK로 단건 조회 (Select)
    // - findAll(): 전체 조회 (Select All)
    // - delete(entity): 삭제 (Delete)

    Optional<Users_Information> findById(Long id);

}