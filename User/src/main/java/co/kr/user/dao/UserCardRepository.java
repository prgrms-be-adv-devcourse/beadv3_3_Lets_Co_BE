package co.kr.user.dao;

import co.kr.user.model.entity.UserCard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserCardRepository extends JpaRepository<UserCard, Long> {
    Optional<UserCard> findFirstByUsersIdxAndCardIdxAndDelOrderByCardIdxDesc(Long usersIdx, Long cardIdx, int del);

    Optional<UserCard> findFirstByUsersIdxAndCardCodeAndDelOrderByCardIdxDesc(Long userIdx, String cardCode, int del);

    List<UserCard> findAllByUsersIdxAndDel(Long usersIdx, int del);

    Optional<UserCard> findByCardCodeAndUsersIdxAndDel(String cardCode, Long usersIdx, int del);
}