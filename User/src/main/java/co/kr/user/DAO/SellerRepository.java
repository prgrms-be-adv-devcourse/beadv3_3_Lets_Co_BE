package co.kr.user.DAO;

import co.kr.user.model.entity.Seller;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 판매자(Seller) 엔티티의 데이터베이스 접근을 담당하는 리포지토리 인터페이스입니다.
 * 일반 회원이 판매자로 전환된 후, 해당 판매자의 사업자 정보나 정산 계좌 등을 조회할 때 사용됩니다.
 */
public interface SellerRepository extends JpaRepository<Seller, Long> {
    /**
     * 판매자 식별자(SellerIdx)로 판매자 정보를 조회하는 메서드입니다.
     * Seller 엔티티는 Users 엔티티와 식별자를 공유하므로, UsersIdx 값을 그대로 사용하여 조회할 수 있습니다.
     *
     * @param sellerIdx 조회할 판매자의 고유 식별자 (UsersIdx와 동일)
     * @return 조회된 판매자 정보 객체 (Optional)
     */
    Optional<Seller> findBySellerIdx(Long sellerIdx);
}