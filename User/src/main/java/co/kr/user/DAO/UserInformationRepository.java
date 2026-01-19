package co.kr.user.DAO;

import co.kr.user.model.entity.UsersInformation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * 사용자 상세 정보(UsersInformation) 엔티티의 데이터베이스 접근을 담당하는 리포지토리입니다.
 * Users 엔티티와 분리된 부가 정보(이름, 연락처, 생년월일 등)의 CRUD 작업을 처리합니다.
 */
public interface UserInformationRepository extends JpaRepository<UsersInformation, Long> {
    /**
     * 사용자 식별자(PK)를 기준으로 상세 정보를 조회하는 메서드입니다.
     * JpaRepository의 기본 메서드인 findById와 동일하지만, 명시적으로 선언되어 있습니다.
     *
     * @param userIdx 사용자 고유 식별자 (UsersInformation의 PK)
     * @return 조회된 상세 정보 객체 (Optional)
     */
    Optional<UsersInformation> findById(Long userIdx);

    /**
     * 삭제 상태(Del)를 기준으로 모든 사용자 상세 정보를 조회하는 메서드입니다.
     * 탈퇴하지 않은 회원들의 상세 정보 목록 등을 추출할 때 사용됩니다.
     *
     * @param del 삭제 상태 플래그 (0: 정상, 1: 삭제됨)
     * @return 조건에 맞는 사용자 상세 정보 리스트
     */
    List<UsersInformation> findAllByDel(int del);

    /**
     * 사용자 식별자(userIdx)와 삭제 상태(del)를 조건으로 상세 정보를 조회하는 메서드입니다.
     * 특정 회원의 상세 정보를 조회하되, 탈퇴 여부까지 함께 검증할 때 유용합니다.
     *
     * @param usersIdx 사용자 고유 식별자
     * @param del 삭제 상태 플래그
     * @return 조건에 맞는 상세 정보 객체 (Optional)
     */
    Optional<UsersInformation> findByUsersIdxAndDel(Long usersIdx, int del);
}