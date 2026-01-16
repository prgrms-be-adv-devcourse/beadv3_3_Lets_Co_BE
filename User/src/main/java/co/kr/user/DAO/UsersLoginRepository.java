package co.kr.user.DAO;

import co.kr.user.model.entity.UsersLogin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsersLoginRepository extends JpaRepository<UsersLogin, Long> {
    // 필요 시 토큰으로 조회하는 메서드 추가
    Optional<UsersLogin> findByToken(String token);
}