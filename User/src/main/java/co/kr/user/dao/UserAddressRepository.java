package co.kr.user.dao;

import co.kr.user.model.entity.UsersAddress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserAddressRepository extends JpaRepository<UsersAddress, Long> {
    Optional<UsersAddress> findFirstByUsersIdxAndAddressIdxAndDelOrderByAddressIdxDesc(Long usersIdx, Long addressIdx,int del);

    Optional<UsersAddress> findFirstByUsersIdxAndAddressCodeAndDelOrderByAddressIdxDesc(Long usersIdx, String addressCode, int del);

    List<UsersAddress> findAllByUsersIdxAndDel(Long usersIdx, int del);

    Optional<UsersAddress> findByAddressCodeAndUsersIdxAndDel(String addressCode, Long usersIdx, int del);
}