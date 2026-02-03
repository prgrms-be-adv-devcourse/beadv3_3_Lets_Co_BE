package co.kr.user.service;

import co.kr.user.model.dto.auth.TokenDto;
import co.kr.user.model.dto.balance.BalanceReq;
import co.kr.user.model.vo.UsersRole;

/**
 * 인증(Authentication) 및 권한(Authorization) 관련 공통 비즈니스 로직을 정의하는 인터페이스입니다.
 * 사용자 권한 조회와 리프레시 토큰을 이용한 토큰 재발급 기능을 명세합니다.
 * 구현체: AuthServiceImpl
 */
public interface ClientService {

    /**
     * 사용자 권한 조회 메서드 정의입니다.
     * 특정 사용자의 현재 권한(Role)을 확인합니다. (예: USER, SELLER, ADMIN)
     *
     * @param userIdx 사용자 고유 식별자
     * @return 사용자 권한 (UsersRole Enum)
     */
    UsersRole getRole(Long userIdx);

    String Balance(Long userIdx, BalanceReq balanceReq);
}