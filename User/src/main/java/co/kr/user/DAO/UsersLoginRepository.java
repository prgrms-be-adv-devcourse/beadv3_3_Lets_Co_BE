package co.kr.user.DAO;

import co.kr.user.model.entity.Users_Login;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsersLoginRepository extends JpaRepository<Users_Login, Long> {
    // 필요 시 토큰으로 조회하는 메서드 추가
    Optional<Users_Login> findByToken(String token);
}