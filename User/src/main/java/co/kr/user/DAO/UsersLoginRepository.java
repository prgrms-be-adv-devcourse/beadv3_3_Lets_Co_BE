package co.kr.user.DAO;

import co.kr.user.model.entity.UsersLogin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 로그인 이력 및 리프레시 토큰(UsersLogin) 엔티티의 데이터베이스 접근을 담당하는 리포지토리입니다.
 * JWT 토큰 발급 이력 저장, 토큰 갱신 시 유효성 검증, 로그아웃 처리 등을 위한 쿼리 메서드를 제공합니다.
 */
public interface UsersLoginRepository extends JpaRepository<UsersLogin, Long> {
    /**
     * 리프레시 토큰(Token) 문자열로 가장 최근의 로그인 이력을 조회하는 메서드입니다.
     * 동일한 토큰 값이 여러 개 존재할 수 없겠지만, 안전을 위해 'LoginIdx' 기준 내림차순(최신순)으로 정렬하여 첫 번째 항목을 가져옵니다.
     * 토큰 갱신(Refresh) 요청 시 해당 토큰이 DB에 존재하는지 확인하기 위해 사용됩니다.
     *
     * @param token 찾고자 하는 리프레시 토큰 문자열
     * @return 해당 토큰을 가진 최신 로그인 이력 (Optional)
     */
    Optional<UsersLogin> findFirstByTokenOrderByLoginIdxDesc(String token);

    /**
     * 특정 사용자(UsersIdx)의 가장 최근 로그인 이력을 조회하는 메서드입니다.
     * 'LoginIdx' 기준 내림차순 정렬하여 가장 마지막에 생성된 레코드를 반환합니다.
     * 관리자가 회원을 차단하거나, 중복 로그인을 제어할 때 사용될 수 있습니다.
     *
     * @param usersIdx 사용자 고유 식별자
     * @return 해당 사용자의 가장 최근 로그인 이력 객체 (단일 반환, 없으면 null)
     */
    UsersLogin findFirstByUsersIdxOrderByLoginIdxDesc(Long usersIdx);
}