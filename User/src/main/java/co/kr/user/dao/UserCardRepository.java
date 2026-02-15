package co.kr.user.dao;

import co.kr.user.model.entity.UserCard;
import co.kr.user.model.vo.UserDel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * UserCard 엔티티(사용자 카드 정보)의 데이터베이스 접근을 담당하는 리포지토리 인터페이스입니다.
 */
public interface UserCardRepository extends JpaRepository<UserCard, Long> {

    /**
     * 사용자 식별자와 카드 식별자(PK)로 특정 카드를 조회합니다.
     * 기본 카드를 찾거나 특정 카드의 유효성을 검증할 때 사용합니다.
     *
     * @param usersIdx 사용자 식별자
     * @param cardIdx 카드 식별자 (PK)
     * @param del 삭제 상태
     * @return 조건에 맞는 UserCard 엔티티 (Optional)
     */
    Optional<UserCard> findFirstByUsersIdxAndCardIdxAndDelOrderByCardIdxDesc(Long usersIdx, Long cardIdx, UserDel del);

    /**
     * 사용자 식별자와 카드 코드(UUID)로 특정 카드를 조회합니다.
     * 외부 클라이언트가 카드 코드로 접근할 때 사용합니다.
     *
     * @param userIdx 사용자 식별자
     * @param cardCode 카드 고유 코드
     * @param del 삭제 상태
     * @return 조건에 맞는 UserCard 엔티티 (Optional)
     */
    Optional<UserCard> findFirstByUsersIdxAndCardCodeAndDelOrderByCardIdxDesc(Long userIdx, String cardCode, UserDel del);

    /**
     * 특정 사용자의 삭제되지 않은 모든 카드 목록을 조회합니다.
     *
     * @param usersIdx 사용자 식별자
     * @param del 삭제 상태 (주로 ACTIVE)
     * @return 해당 사용자의 카드 리스트
     */
    List<UserCard> findAllByUsersIdxAndDel(Long usersIdx, UserDel del);

    /**
     * 카드 코드와 사용자 식별자로 특정 카드를 조회합니다.
     * 카드 정보 수정/삭제 시 권한 확인 용도로도 사용됩니다.
     *
     * @param cardCode 카드 고유 코드
     * @param usersIdx 사용자 식별자
     * @param del 삭제 상태
     * @return 조건에 맞는 UserCard 엔티티 (Optional)
     */
    Optional<UserCard> findByCardCodeAndUsersIdxAndDel(String cardCode, Long usersIdx, UserDel del);
}