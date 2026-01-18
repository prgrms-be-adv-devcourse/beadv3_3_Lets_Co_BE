package co.kr.user.service;

import co.kr.user.DAO.UserRepository;
import co.kr.user.DAO.UsersLoginRepository;
import co.kr.user.model.DTO.auth.TokenDto;
import co.kr.user.model.entity.Users;
import co.kr.user.model.entity.UsersLogin;
import co.kr.user.model.vo.UsersRole;
import co.kr.user.util.JWTUtil;
import co.kr.user.util.TokenUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService implements AuthServiceImpl {
    private final UserRepository userRepository;
    private final UsersLoginRepository usersLoginRepository;

    private final JWTUtil jwtUtil;

    @Override
    public UsersRole getRole(Long userIdx) {
        Users users = userRepository.findById(userIdx)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        return users.getRole();
    }

    @Override
    @Transactional
    public TokenDto refreshToken(String refreshToken) {
        UsersLogin usersLogin = usersLoginRepository.findFirstByTokenOrderByLoginIdxDesc(refreshToken)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 Refresh Token입니다."));

        Users users = userRepository.findById(usersLogin.getUsersIdx())
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보를 찾을 수 없습니다."));

        if (users.getLockedUntil() != null) {
            if (users.getLockedUntil().isAfter(users.getUpdatedAt())) {
                throw new IllegalStateException("계정이 " + users.getLockedUntil() + "까지 정지되었습니다.");
            }
        }

        String newAccessToken = jwtUtil.createAccessToken(users.getUsersIdx(), users.getCreatedAt(), users.getUpdatedAt());

        String newRefreshToken = null;

        if (TokenUtil.isTokenExpiringSoon(refreshToken)) {

            usersLogin.maturity();

            newRefreshToken = jwtUtil.createRefreshToken(users.getUsersIdx());

            usersLogin.updateToken(newRefreshToken);
        }

        TokenDto tokenDto = new TokenDto();
        tokenDto.setAccessToken(newAccessToken);
        tokenDto.setRefreshToken(newRefreshToken);

        return tokenDto;
    }
}