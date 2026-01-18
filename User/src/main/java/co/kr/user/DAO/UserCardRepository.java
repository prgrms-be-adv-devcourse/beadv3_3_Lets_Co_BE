package co.kr.user.DAO;

import co.kr.user.model.entity.UserCard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserCardRepository extends JpaRepository<UserCard, Long> {
    List<UserCard> findAllByUsersIdxAndDel(Long usersIdx, int del);

    Optional<UserCard> findByCardCode(String cardCode);
}