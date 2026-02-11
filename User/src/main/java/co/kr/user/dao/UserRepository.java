package co.kr.user.dao;

import co.kr.user.model.entity.Users;
import co.kr.user.model.vo.UserDel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<Users, Long> {
    boolean existsByIdAndDel(String id, UserDel del);

    Optional<Users> findByUsersIdxAndDel(Long usersIdx, UserDel del);

    List<Users> findAllByDel(UserDel del);

    Optional<Users> findByIdAndDel(String id, UserDel del);
}