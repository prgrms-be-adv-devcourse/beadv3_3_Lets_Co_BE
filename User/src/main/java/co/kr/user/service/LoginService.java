package co.kr.user.service;

import co.kr.user.DAO.UserRepository;
import co.kr.user.DAO.UsersLoginRepository;
import co.kr.user.model.DTO.login.LoginDTO;
import co.kr.user.model.DTO.login.LoginReq;
import co.kr.user.model.entity.Users;
import co.kr.user.model.entity.UsersLogin;
import co.kr.user.util.BCryptUtil;
import co.kr.user.util.JWTUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LoginService implements LoginServiceImpl{
    private final UserRepository userRepository;
    private final UsersLoginRepository usersLoginRepository;

    private final BCryptUtil bCryptUtil;
    private final JWTUtil jwtUtil;

    @Override
    @Transactional
    public LoginDTO login(LoginReq loginReq) {
        Users users = userRepository.findByID(loginReq.getID())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이디입니다."));

        if (users.getDel() == 1) {
            throw new IllegalStateException("탈퇴한 회원입니다.");
        }
        else if (users.getDel() == 2) {
            throw new IllegalStateException("인증을 먼저 시도해 주세요.");
        }

        if (users.getFailedLoginAttempts() >= 5) {
            users.lockFor15Minutes();
            throw new IllegalStateException("보안을 위해 계정이 일시 정지되었습니다. 15분 후에 다시 시도해주세요.");
        }

        if (users.getLockedUntil() != null) {
            if (users.getLockedUntil().isAfter(users.getUpdatedAt())) {
                throw new IllegalStateException("계정이 " + users.getLockedUntil() + "까지 정지되었습니다.");
            }
        }

        if (!bCryptUtil.check(loginReq.getPW(), users.getPW())) {
            users.loginFail();
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        LoginDTO loginDTO = new LoginDTO();
        String accessToken = jwtUtil.createAccessToken(users.getUsersIdx(), users.getCreatedAt(), users.getUpdatedAt());
        String refreshToken = jwtUtil.createRefreshToken(users.getUsersIdx());

        loginDTO.setAccessToken(accessToken);
        loginDTO.setRefreshToken(refreshToken);

        UsersLogin usersLogin = UsersLogin.builder()
                .usersIdx(users.getUsersIdx())
                .token(refreshToken)
                .lastUsedAt(null)
                .build();

        usersLoginRepository.save(usersLogin);

        users.loginSuccess();

        return loginDTO;
    }

    @Override
    @Transactional
    public String logout(String refreshToken) {
        return usersLoginRepository.findFirstByTokenOrderByLoginIdxDesc(refreshToken)
                .map(loginRecord -> {
                    loginRecord.logout();
                    return "로그아웃 되었습니다.";
                })
                .orElseThrow(() -> new IllegalArgumentException("로그아웃에 실패했습니다. 유효하지 않은 토큰입니다."));
    }
}