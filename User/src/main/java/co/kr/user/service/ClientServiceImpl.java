package co.kr.user.service;

import co.kr.user.dao.UserRepository;
import co.kr.user.dao.UsersLoginRepository;
import co.kr.user.model.dto.auth.TokenDto;
import co.kr.user.model.entity.Users;
import co.kr.user.model.entity.UsersLogin;
import co.kr.user.model.vo.UsersRole;
import co.kr.user.util.JWTUtil;
import co.kr.user.util.TokenUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 인증(Authentication) 및 권한(Authorization) 관련 공통 로직을 처리하는 서비스 클래스입니다.
 * 사용자 권한 조회와 JWT 토큰 재발급(Refresh) 기능을 제공합니다.
 */
@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {
    private final UserRepository userRepository;

    /**
     * 사용자의 권한(Role)을 조회하는 메서드입니다.
     * 다른 서비스나 컨트롤러에서 권한 체크가 필요할 때 호출됩니다.
     *
     * @param userIdx 사용자 고유 식별자
     * @return UsersRole (ADMIN, SELLER, USERS 등)
     */
    @Override
    public UsersRole getRole(Long userIdx) {
        Users users = userRepository.findById(userIdx)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        return users.getRole();
    }
}