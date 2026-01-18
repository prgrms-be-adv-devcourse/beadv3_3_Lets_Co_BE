package co.kr.user.DAO;

import co.kr.user.model.entity.UsersVerifications;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserVerificationsRepository extends JpaRepository<UsersVerifications, Long> {
    Optional<UsersVerifications> findTopByCodeOrderByCreatedAtDesc(String Code);

    Optional<UsersVerifications> findTopByUsersIdxOrderByCreatedAtDesc(Long userIdx);
}