package co.kr.user.DAO;

import co.kr.user.model.entity.Seller;
import co.kr.user.model.entity.UsersAddress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserAddressRepository extends JpaRepository<UsersAddress, Long> {
    Optional<UsersAddress> findById(Long userIdx);

    Optional<UsersAddress> findByaddressCode(String addressCode);
}
