package co.kr.user.DAO;

import co.kr.user.model.entity.UsersLogin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsersLoginRepository extends JpaRepository<UsersLogin, Long> {
    Optional<UsersLogin> findFirstByTokenOrderByLoginIdxDesc(String token);

    UsersLogin findFirstByUsersIdxOrderByLoginIdxDesc(Long usersIdx);
}