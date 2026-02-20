package co.kr.user.dao;

import co.kr.user.model.entity.Users;
import co.kr.user.model.vo.UserDel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Users 엔티티(사용자 계정 기본 정보)의 데이터베이스 접근을 담당하는 리포지토리 인터페이스입니다.
 * 아이디, 비밀번호, 권한 등 인증과 관련된 핵심 정보를 관리합니다.
 */
public interface UserRepository extends JpaRepository<Users, Long> {

    /**
     * 특정 아이디가 존재하는지 확인합니다.
     * 회원가입 시 아이디 중복 체크에 사용됩니다.
     *
     * @param id 확인할 사용자 아이디
     * @param del 삭제 상태
     * @return 존재하면 true, 아니면 false
     */
    boolean existsByIdAndDel(String id, UserDel del);

    /**
     * 사용자 식별자(PK)로 계정 정보를 조회합니다.
     *
     * @param usersIdx 사용자 식별자
     * @param del 삭제 상태
     * @return 조건에 맞는 Users 엔티티 (Optional)
     */
    Optional<Users> findByUsersIdxAndDel(Long usersIdx, UserDel del);

    /**
     * 특정 상태(예: ACTIVE)인 모든 사용자 계정을 조회합니다.
     * 관리자 기능 등에서 전체 목록이 필요할 때 사용됩니다.
     *
     * @param del 삭제 상태
     * @return 모든 Users 엔티티 리스트
     */
    List<Users> findAllByDel(UserDel del);

    /**
     * [성능 최적화] 페이징 처리를 적용하여 특정 상태의 사용자 계정을 조회합니다.
     * 기존 findAllByDel과 달리 LIMIT/OFFSET 쿼리를 사용하여 메모리 부하를 방지합니다.
     *
     * @param del 삭제 상태
     * @param pageable 페이징 및 정렬 정보
     * @return 페이지 처리된 Users 엔티티 목록
     */
    Page<Users> findAllByDel(UserDel del, Pageable pageable);

    /**
     * 아이디(String)로 사용자 계정 정보를 조회합니다.
     * 로그인 시 아이디 기반으로 사용자를 찾을 때 주로 사용됩니다.
     * (아이디는 암호화되어 저장되므로, 검색을 위해 결정적 암호화가 적용되어야 함)
     *
     * @param id 사용자 아이디
     * @param del 삭제 상태
     * @return 조건에 맞는 Users 엔티티 (Optional)
     */
    Optional<Users> findByIdAndDel(String id, UserDel del);

    boolean existsById(String id);
}