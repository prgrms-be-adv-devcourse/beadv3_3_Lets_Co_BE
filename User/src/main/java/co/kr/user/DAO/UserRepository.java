package co.kr.user.DAO;

import co.kr.user.model.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<Users, Long> {
    boolean existsByID(String ID);

    Optional<Users> findByID(String ID);

    List<Users> findAllByDelOrderByCreatedAtDesc(int del);

    Optional<Users> findByIDAndDel(String ID, int del);
}