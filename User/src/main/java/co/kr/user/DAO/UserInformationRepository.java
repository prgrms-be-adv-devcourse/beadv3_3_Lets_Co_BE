package co.kr.user.DAO;

import co.kr.user.model.entity.UsersInformation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserInformationRepository extends JpaRepository<UsersInformation, Long> {
    Optional<UsersInformation> findById(Long userIdx);

    List<UsersInformation> findAllByDel(int del);

    Optional<UsersInformation> findByUsersIdxAndDel(Long usersIdx, int del);
}