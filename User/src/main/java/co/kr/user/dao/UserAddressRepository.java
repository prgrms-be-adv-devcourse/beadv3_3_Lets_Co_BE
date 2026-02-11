package co.kr.user.dao;

import co.kr.user.model.entity.UsersAddress;
import co.kr.user.model.vo.UserDel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserAddressRepository extends JpaRepository<UsersAddress, Long> {
    Optional<UsersAddress> findFirstByUsersIdxAndAddressIdxAndDelOrderByAddressIdxDesc(Long usersIdx, Long addressIdx, UserDel del);

    Optional<UsersAddress> findFirstByUsersIdxAndAddressCodeAndDelOrderByAddressIdxDesc(Long usersIdx, String addressCode, UserDel del);

    List<UsersAddress> findAllByUsersIdxAndDel(Long usersIdx, UserDel del);

    Optional<UsersAddress> findByAddressCodeAndUsersIdxAndDel(String addressCode, Long usersIdx, UserDel del);
}