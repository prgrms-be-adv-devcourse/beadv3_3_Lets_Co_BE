package co.kr.user.DAO;

import co.kr.user.model.entity.UserCard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserCardRepository extends JpaRepository<UserCard, Long> {
    Optional<UserCard> findFirstByUsersIdxAndDefaultCardAndDelOrderByCardIdxDesc(Long usersIdx, int defaultCard, int del);

    Optional<UserCard> findFirstByUsersIdxAndCardCodeAndDelOrderByCardIdxDesc(Long userIdx, String cardCode, int del);

    List<UserCard> findAllByUsersIdxAndDel(Long usersIdx, int del);

    Optional<UserCard> findByCardCode(String cardCode);
}