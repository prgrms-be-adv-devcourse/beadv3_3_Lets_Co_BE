package co.kr.user.repository;

import co.kr.user.model.entity.UserInformation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * [UserInformation 데이터 접근 계층]
 * DB의 'Users_Information' 테이블에 접근
 */
public interface UserInformationRepository extends JpaRepository<UserInformation, Long> {

    /**
     * [쿼리 메서드 정의]
     * User 엔티티의 ID(usersIdx)를 기준으로 상세 정보를 조회
     * SQL 예시: SELECT * FROM Users_Information WHERE Users_IDX = ?
     */
    Optional<UserInformation> findByUserUsersIdx(Long usersIdx);
}



