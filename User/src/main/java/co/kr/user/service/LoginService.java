package co.kr.user.service;

import co.kr.user.DAO.UserInformationRepository;
import co.kr.user.DAO.UserRepository;
import co.kr.user.model.DTO.login.LoginDTO;
import co.kr.user.model.DTO.login.LoginReq;
import co.kr.user.model.entity.Users;
import co.kr.user.model.entity.Users_Information;
import co.kr.user.util.BCryptUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginService implements LoginServiceImpl{

    private final UserRepository userRepository;
    private final UserInformationRepository userInformationRepository;

    private final BCryptUtil bCryptUtil;

    @Override
    public LoginDTO login(LoginReq loginReq) {
        // 1. 전체 정보 조회 (비밀번호 비교를 위해)
        Users users = userRepository.findByID(loginReq.getID())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이디입니다."));

        // 2. 탈퇴 여부 확인 (Del 컬럼 활용)
        // (Users 엔티티에 getDel() 메서드가 있다고 가정)
        if (users.getDel() == 1) {
            throw new IllegalStateException("탈퇴한 회원입니다.");
        }
        else if (users.getDel() == 2) {
            throw new IllegalStateException("인증을 먼저 시도해 주세요.");
        }

        // 3. 비밀번호 검증 (BCryptUtil 사용)
        // 사용자가 입력한 PW와 DB의 암호화된 PW 비교
        if (!bCryptUtil.checkPassword(loginReq.getPW(), users.getPW())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }


        Users_Information usersInformation = userInformationRepository.findById(users.getUsersIdx())
                .orElseThrow(() -> new IllegalStateException("회원 상세 정보를 찾을 수 없습니다."));




        return null;
    }
}
