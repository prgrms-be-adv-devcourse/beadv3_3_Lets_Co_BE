package co.kr.user.dao;

import co.kr.user.model.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<Users, Long> {
    boolean existsByIdAndDel(String id, int del);

    Optional<Users> findByUsersIdxAndDel(Long usersIdx, int del);

    List<Users> findAllByDel(int del);

    Optional<Users> findByIdAndDel(String id, int del);
}