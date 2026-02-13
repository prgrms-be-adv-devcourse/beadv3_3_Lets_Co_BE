package co.kr.user.service.Impl;

import co.kr.user.dao.UserRepository;
import co.kr.user.model.entity.Users;
import co.kr.user.service.UserQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserQueryServiceImpl implements UserQueryService {
    private final UserRepository userRepository;

    public Users findWaitUser(Long userIdx) {
        Users user = userRepository.findByUsersIdxAndDel(userIdx, 2)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        return user;
    }

    public Users findActiveUser(Long userIdx) {
        Users user = userRepository.findByUsersIdxAndDel(userIdx, 0)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        user.checkAccountStatus();
        return user;
    }

    public Users findActiveUserById(String id) {
        Users user = userRepository.findByIdAndDel(id, 0)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이디입니다."));

        user.checkAccountStatus();
        return user;
    }
}