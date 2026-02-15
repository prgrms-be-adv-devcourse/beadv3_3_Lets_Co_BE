package co.kr.user.service;

import co.kr.user.model.entity.UserCard;
import co.kr.user.model.entity.Users;
import co.kr.user.model.entity.UsersAddress;
import co.kr.user.model.entity.UsersInformation;

import java.util.List;
import java.util.Map;

public interface UserQueryService {
    Users findWaitUser(Long userIdx);

    Users findActiveUser(Long userIdx);

    Users findActiveUserById(String id);

    boolean existsActiveId(String id);

    List<Users> findAllActiveUsers();

    UsersInformation findWaitUserInfo(Long userIdx);

    UsersInformation findActiveUserInfo(Long userIdx);

    Map<Long, UsersInformation> findActiveUserInfos(List<Long> userIdxList);

    List<UsersAddress> findActiveAddresses(Long userIdx);

    List<UserCard> findActiveCards(Long userIdx);
}