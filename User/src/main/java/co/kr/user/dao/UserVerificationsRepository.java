package co.kr.user.dao;

import co.kr.user.model.entity.UsersVerifications;
import co.kr.user.model.vo.PublicDel;
import co.kr.user.model.vo.UserDel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * UsersVerifications 엔티티(이메일 인증 등 본인 확인 정보)의 데이터베이스 접근을 담당하는 리포지토리입니다.
 */
public interface UserVerificationsRepository extends JpaRepository<UsersVerifications, Long> {

    /**
     * 인증 코드와 삭제 상태로 인증 정보를 조회합니다.
     * 최신순으로 정렬하여 가장 최근에 발급된 인증 코드를 먼저 가져옵니다.
     *
     * @param code 인증 코드
     * @param del 삭제 상태
     * @return 조건에 맞는 최신 UsersVerifications 엔티티 (Optional)
     */
    Optional<UsersVerifications> findTopByCodeAndDelOrderByCreatedAtDesc(String code, PublicDel del);

    /**
     * 사용자 식별자와 삭제 상태로 인증 정보를 조회합니다.
     * 해당 사용자의 가장 최신 인증 요청 내역을 가져옵니다.
     *
     * @param usersIdx 사용자 식별자
     * @param del 삭제 상태
     * @return 조건에 맞는 최신 UsersVerifications 엔티티 (Optional)
     */
    Optional<UsersVerifications> findTopByUsersIdxAndDelOrderByCreatedAtDesc(Long usersIdx, PublicDel del);
}