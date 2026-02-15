package co.kr.user.dao;

import co.kr.user.model.entity.Seller;
import co.kr.user.model.vo.UserDel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Seller 엔티티(판매자 정보)의 데이터베이스 접근을 담당하는 리포지토리 인터페이스입니다.
 */
public interface SellerRepository extends JpaRepository<Seller, Long> {

    /**
     * 사용자 식별자와 삭제 상태(Del)를 기준으로 판매자 정보를 조회합니다.
     * 특정 상태(예: ACTIVE, PENDING)인 판매자 정보를 찾을 때 사용합니다.
     *
     * @param usersIdx 사용자 식별자 (PK)
     * @param del 삭제 상태 (ACTIVE, DELETED, PENDING)
     * @return 조건에 맞는 Seller 엔티티 (Optional)
     */
    Optional<Seller> findByUsersIdxAndDel(Long usersIdx, UserDel del);

    /**
     * 특정 사용자가 해당 상태로 판매자에 등록되어 있는지 여부를 확인합니다.
     * 중복 가입 방지 등에 사용됩니다.
     *
     * @param usersIdx 사용자 식별자 (PK)
     * @param del 삭제 상태
     * @return 존재하면 true, 아니면 false
     */
    boolean existsByUsersIdxAndDel(Long usersIdx, UserDel del);
}