package co.kr.user.dao;

import co.kr.user.model.entity.UserCard;
import co.kr.user.model.vo.UserDel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserCardRepository extends JpaRepository<UserCard, Long> {
    Optional<UserCard> findFirstByUsersIdxAndCardIdxAndDelOrderByCardIdxDesc(Long usersIdx, Long cardIdx, UserDel del);

    Optional<UserCard> findFirstByUsersIdxAndCardCodeAndDelOrderByCardIdxDesc(Long userIdx, String cardCode, UserDel del);

    List<UserCard> findAllByUsersIdxAndDel(Long usersIdx, UserDel del);

    Optional<UserCard> findByCardCodeAndUsersIdxAndDel(String cardCode, Long usersIdx, UserDel del);
}