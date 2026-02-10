package co.kr.user.dao;

import co.kr.user.model.entity.UsersInformation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserInformationRepository extends JpaRepository<UsersInformation, Long> {
    List<UsersInformation> findAllByUsersIdxInAndDel(List<Long> usersIdxList, int del);

    Optional<UsersInformation> findByUsersIdxAndDel(Long usersIdx, int del);

    Optional<UsersInformation> findByMailAndDel(String mail, int del);
}