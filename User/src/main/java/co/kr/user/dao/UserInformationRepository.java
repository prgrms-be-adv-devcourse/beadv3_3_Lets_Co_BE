package co.kr.user.dao;

import co.kr.user.model.entity.UsersInformation;
import co.kr.user.model.vo.UserDel;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.List;
import java.util.Optional;

/**
 * UsersInformation 엔티티(사용자 상세 정보)의 데이터베이스 접근을 담당하는 리포지토리 인터페이스입니다.
 * 이름, 이메일, 전화번호 등 개인정보를 관리합니다.
 */
public interface UserInformationRepository extends JpaRepository<UsersInformation, Long> {

    /**
     * 여러 사용자 식별자(List)에 해당하는 상세 정보들을 한 번에 조회합니다.
     * 관리자 목록 조회 등에서 N+1 문제를 방지하기 위해 사용됩니다.
     *
     * @param usersIdxList 조회할 사용자 식별자 리스트
     * @param del 삭제 상태
     * @return 사용자 상세 정보 리스트
     */
    List<UsersInformation> findAllByUsersIdxInAndDel(List<Long> usersIdxList, UserDel del);

    /**
     * 특정 사용자의 상세 정보를 조회합니다.
     *
     * @param usersIdx 사용자 식별자 (PK)
     * @param del 삭제 상태
     * @return 조건에 맞는 UsersInformation 엔티티 (Optional)
     */
    Optional<UsersInformation> findByUsersIdxAndDel(Long usersIdx, UserDel del);

    /**
     * 이메일 주소로 사용자 상세 정보를 조회합니다.
     * 아이디 찾기, 비밀번호 찾기 등 이메일 기반 인증 시 사용됩니다.
     * (이메일은 암호화되어 저장되므로, 검색을 위해 결정적 암호화가 적용되어야 함)
     *
     * @param mail 사용자 이메일
     * @param del 삭제 상태
     * @return 조건에 맞는 UsersInformation 엔티티 (Optional)
     */
    Optional<UsersInformation> findByMailAndDel(String mail, UserDel del);

    /**
     * [동시성 제어] 비관적 락(Pessimistic Lock)을 사용하여 사용자 정보를 조회합니다.
     * * @Query 없이 메서드 이름 규칙만으로 쿼리를 생성하며,
     * 기존 findBy... 메서드와 구분하기 위해 'readBy...' 키워드를 사용했습니다.
     * (Spring Data JPA에서 find, read, get은 모두 조회 기능으로 동일하게 동작합니다.)
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<UsersInformation> readByUsersIdxAndDel(Long usersIdx, UserDel del);
}