package co.kr.user.dao;

import co.kr.user.model.entity.Seller;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SellerRepository extends JpaRepository<Seller, Long> {
    Optional<Seller> findByUsersIdxAndDel(Long usersIdx, int del);

    boolean existsByUsersIdxAndDel(Long usersIdx, int del);
}