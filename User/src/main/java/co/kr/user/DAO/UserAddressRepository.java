package co.kr.user.DAO;

import co.kr.user.model.entity.UsersAddress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserAddressRepository extends JpaRepository<UsersAddress, Long> {
    List<UsersAddress> findAllByUsersIdxAndDel(Long usersIdx, int del);

    Optional<UsersAddress> findByAddressCode(String addressCode);
}