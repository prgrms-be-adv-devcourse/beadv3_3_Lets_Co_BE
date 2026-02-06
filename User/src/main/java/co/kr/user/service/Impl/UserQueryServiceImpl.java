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

    /**
     * 식별자로 활성화된 사용자를 조회하고 상태를 검증합니다.
     */
    public Users findActiveUser(Long userIdx) {
        Users user = userRepository.findById(userIdx)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        user.checkAccountStatus(); // 1단계에서 만든 검증 메서드 호출
        return user;
    }

    /**
     * 아이디(이메일)로 활성화된 사용자를 조회하고 상태를 검증합니다.
     */
    public Users findActiveUserById(String id) {
        Users user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이디입니다."));

        user.checkAccountStatus();
        return user;
    }
}