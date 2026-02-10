package co.kr.user.dao;

import co.kr.user.model.entity.UsersVerifications;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserVerificationsRepository extends JpaRepository<UsersVerifications, Long> {
    Optional<UsersVerifications> findTopByCodeAndDelOrderByCreatedAtDesc(String Code, int del);

    Optional<UsersVerifications> findTopByUsersIdxAndDelOrderByCreatedAtDesc(Long userIdx, int del);
}