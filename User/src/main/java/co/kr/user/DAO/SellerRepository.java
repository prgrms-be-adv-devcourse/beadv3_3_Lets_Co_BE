package co.kr.user.DAO;

import co.kr.user.model.entity.Seller;
import co.kr.user.model.entity.UsersInformation;
import co.kr.user.model.entity.UsersVerifications;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SellerRepository  extends JpaRepository<Seller, Long> {
    Optional<Seller> findById(Long userIdx);

}
