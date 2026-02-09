package co.kr.user.service.Impl;

import co.kr.user.dao.UserInformationRepository;
import co.kr.user.dao.UserRepository;
import co.kr.user.model.dto.balance.BalanceReq;
import co.kr.user.model.entity.Users;
import co.kr.user.model.vo.UsersRole;
import co.kr.user.service.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 인증(Authentication) 및 권한(Authorization) 관련 공통 로직을 처리하는 서비스 클래스입니다.
 * 사용자 권한 조회와 JWT 토큰 재발급(Refresh) 기능을 제공합니다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClientServiceImpl implements ClientService {
    private final UserRepository userRepository;
    private final UserInformationRepository userInformationRepository;

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

    @Override
    @Transactional
    public String Balance(Long userIdx, BalanceReq balanceReq) {
//        Users users = userRepository.findById(userIdx)
//                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
//        if (users.getDel() == 1) {
//            throw new IllegalStateException("탈퇴한 회원입니다.");
//        }
//        else if (users.getDel() == 2) {
//            throw new IllegalStateException("인증을 먼저 시도해 주세요.");
//        }
//
//        UsersInformation usersInformation = UserInformat(userIdx);

        return "";
    }
}