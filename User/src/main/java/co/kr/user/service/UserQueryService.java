package co.kr.user.service;

import co.kr.user.model.entity.Users;

public interface UserQueryService {
    Users findActiveUser(Long userIdx);

    Users findActiveUserById(String id);
}
