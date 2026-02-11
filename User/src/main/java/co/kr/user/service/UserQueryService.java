package co.kr.user.service;

import co.kr.user.model.entity.Users;
import co.kr.user.model.entity.UsersInformation;

import java.util.List;

public interface UserQueryService {
    Users findWaitUser(Long userIdx);

    Users findActiveUser(Long userIdx);

    Users findActiveUserById(String id);

    boolean existsActiveId(String id);

    List<Users> findAllActiveUsers();

    UsersInformation findWaitUserInfo(Long userIdx);

    UsersInformation findActiveUserInfo(Long userIdx);
}