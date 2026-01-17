package co.kr.user.service;

import co.kr.user.DAO.UserRepository;
import co.kr.user.DAO.UsersLoginRepository;
import co.kr.user.model.DTO.auth.TokenDto;
import co.kr.user.model.entity.Users;
import co.kr.user.model.entity.UsersLogin;
import co.kr.user.model.vo.UsersRole;
import co.kr.user.util.JWTUtil;
import co.kr.user.util.TokenUtil; // 제공해주신 TokenUtil 사용
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
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
    public String accessToken(String AccessToken) {
        // 기존 코드 유지 (사용하지 않는다면 비워둠)
        return "";
    }

    @Override
    @Transactional
    public TokenDto refreshToken(String refreshToken) {
        // 1. DB에서 리프레시 토큰 존재 여부 확인 (유효성 검증 포함)
        UsersLogin usersLogin = usersLoginRepository.findByToken(refreshToken)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 Refresh Token입니다."));

        // 2. 사용자 정보 조회 (Access Token 재발급 시 필요한 정보 가져오기 위함)
        Users users = userRepository.findById(usersLogin.getUsersIdx())
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보를 찾을 수 없습니다."));

        // 3. Access Token은 무조건 재발급
        String newAccessToken = jwtUtil.createAccessToken(users.getUsersIdx(), users.getCreatedAt(), users.getUpdatedAt());

        // 4. Refresh Token 갱신 여부 판단 (6일 이하 남았는지 체크)
        String newRefreshToken = null;
        if (TokenUtil.isTokenExpiringSoon(refreshToken)) {
            log.info("Refresh Token 만료가 임박하여 재발급합니다.");

            usersLogin.maturity();

            newRefreshToken = jwtUtil.createRefreshToken(users.getUsersIdx());

            // DB 업데이트 (Dirty Checking)
            // UsersLogin 엔티티에 updateToken 같은 메서드가 없다면 setter 사용,
            // 혹은 builder로 새로 만들어서 save 해야 함 (여기서는 setter 가정)
            usersLogin.updateToken(newRefreshToken);
            // 만약 setToken이 없고 불변 객체라면, Repository.save()로 덮어쓰기 로직 필요
        }

        // 5. 결과 반환
        TokenDto tokenDto = new TokenDto();
        tokenDto.setAccessToken(newAccessToken);
        tokenDto.setRefreshToken(newRefreshToken); // 갱신 안 됐으면 null

        return tokenDto;
    }
}