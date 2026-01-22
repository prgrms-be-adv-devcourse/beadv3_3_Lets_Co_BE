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

/**
 * 로그인 및 로그아웃 관련 비즈니스 로직을 처리하는 서비스 클래스입니다.
 * 사용자 인증, 로그인 실패 시 보안 정책(잠금 등), JWT 토큰(Access/Refresh) 발급 및 관리를 담당합니다.
 */
@Service
@RequiredArgsConstructor
public class LoginServiceImpl implements LoginService{
    private final UserRepository userRepository;
    private final UsersLoginRepository usersLoginRepository;

    private final BCryptUtil bCryptUtil; // 비밀번호 검증 유틸리티
    private final JWTUtil jwtUtil; // JWT 생성 및 검증 유틸리티

    /**
     * 로그인 처리 메서드입니다.
     * 사용자 인증을 수행하고, 성공 시 JWT 토큰(Access, Refresh)을 발급합니다.
     * 5회 이상 실패 시 계정을 잠그는 보안 로직이 포함되어 있습니다.
     *
     * @param loginReq 로그인 요청 정보 (아이디, 비밀번호)
     * @return LoginDTO (생성된 AccessToken, RefreshToken)
     */
    @Override
    @Transactional
    public LoginDTO login(LoginReq loginReq) {
        // 아이디로 사용자 조회 (존재하지 않으면 예외 발생)
        Users users = userRepository.findByID(loginReq.getID())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이디입니다."));

        // 계정 상태 검증 (탈퇴: 1, 미인증: 2)
        if (users.getDel() == 1) {
            throw new IllegalStateException("탈퇴한 회원입니다.");
        }
        else if (users.getDel() == 2) {
            throw new IllegalStateException("인증을 먼저 시도해 주세요.");
        }

        // 로그인 실패 횟수 확인 (5회 이상이면 잠금 시도)
        if (users.getFailedLoginAttempts() >= 5) {
            users.lockFor15Minutes(); // 15분간 잠금 처리
            throw new IllegalStateException("보안을 위해 계정이 일시 정지되었습니다. 15분 후에 다시 시도해주세요.");
        }

        // 계정 잠금 시간 확인 (현재 시간이 잠금 해제 시간 이전인지 체크)
        if (users.getLockedUntil() != null) {
            if (users.getLockedUntil().isAfter(users.getUpdatedAt())) {
                throw new IllegalStateException("계정이 " + users.getLockedUntil() + "까지 정지되었습니다.");
            }
        }

        // 비밀번호 일치 여부 확인
        if (!bCryptUtil.check(loginReq.getPW(), users.getPW())) {
            users.loginFail(); // 실패 횟수 증가
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 로그인 성공 처리 (DTO 생성)
        LoginDTO loginDTO = new LoginDTO();

        // Access Token 생성 (유효기간 짧음, 인증용)
        String accessToken = jwtUtil.createAccessToken(users.getUsersIdx(), users.getCreatedAt(), users.getUpdatedAt());
        // Refresh Token 생성 (유효기간 김, 갱신용)
        String refreshToken = jwtUtil.createRefreshToken(users.getUsersIdx());

        loginDTO.setAccessToken(accessToken);
        loginDTO.setRefreshToken(refreshToken);

        // Refresh Token DB 저장 (로그아웃 처리 및 토큰 탈취 대응을 위함)
        UsersLogin usersLogin = UsersLogin.builder()
                .usersIdx(users.getUsersIdx())
                .token(refreshToken)
                .lastUsedAt(null)
                .build();

        usersLoginRepository.save(usersLogin);

        // 로그인 성공 상태 업데이트 (실패 횟수 초기화 등)
        users.loginSuccess();

        return loginDTO;
    }

    /**
     * 로그아웃 처리 메서드입니다.
     * DB에 저장된 Refresh Token을 찾아 만료(Logout) 상태로 변경하여 더 이상 토큰 갱신이 불가능하게 만듭니다.
     *
     * @param refreshToken 클라이언트 쿠키에 저장되어 있던 Refresh Token
     * @return 로그아웃 결과 메시지
     */
    @Override
    @Transactional
    public String logout(String refreshToken) {
        return usersLoginRepository.findFirstByTokenOrderByLoginIdxDesc(refreshToken)
                .map(loginRecord -> {
                    // 해당 토큰 레코드를 찾아 로그아웃 상태(Revoked)로 변경
                    loginRecord.logout();
                    return "로그아웃 되었습니다.";
                })
                .orElseThrow(() -> new IllegalArgumentException("로그아웃에 실패했습니다. 유효하지 않은 토큰입니다."));
    }
}