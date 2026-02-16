package co.kr.user.dao;

import co.kr.user.model.entity.UsersAddress;
import co.kr.user.model.vo.UserDel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * UsersAddress 엔티티(사용자 배송지)의 데이터베이스 접근을 담당하는 리포지토리 인터페이스입니다.
 */
public interface UserAddressRepository extends JpaRepository<UsersAddress, Long> {

    /**
     * 사용자 식별자와 주소 식별자(PK)로 특정 배송지를 조회합니다.
     * 주로 기본 배송지를 찾거나 특정 배송지의 존재 여부를 확인할 때 사용합니다.
     *
     * @param usersIdx 사용자 식별자
     * @param addressIdx 주소 식별자 (PK)
     * @param del 삭제 상태
     * @return 조건에 맞는 UsersAddress 엔티티 (Optional)
     */
    Optional<UsersAddress> findFirstByUsersIdxAndAddressIdxAndDelOrderByAddressIdxDesc(Long usersIdx, Long addressIdx, UserDel del);

    /**
     * 사용자 식별자와 주소 코드(UUID)로 특정 배송지를 조회합니다.
     * 외부(클라이언트)에 노출된 코드를 이용해 배송지를 찾을 때 사용합니다.
     *
     * @param usersIdx 사용자 식별자
     * @param addressCode 주소 고유 코드
     * @param del 삭제 상태
     * @return 조건에 맞는 UsersAddress 엔티티 (Optional)
     */
    Optional<UsersAddress> findFirstByUsersIdxAndAddressCodeAndDelOrderByAddressIdxDesc(Long usersIdx, String addressCode, UserDel del);

    /**
     * 특정 사용자의 삭제되지 않은 모든 배송지 목록을 조회합니다.
     *
     * @param usersIdx 사용자 식별자
     * @param del 삭제 상태 (주로 ACTIVE)
     * @return 해당 사용자의 배송지 리스트
     */
    List<UsersAddress> findAllByUsersIdxAndDel(Long usersIdx, UserDel del);

    /**
     * 주소 코드와 사용자 식별자로 특정 배송지를 조회합니다.
     * 배송지 수정이나 삭제 시, 해당 배송지가 본인의 것이 맞는지 확인할 때 유용합니다.
     *
     * @param addressCode 주소 고유 코드
     * @param usersIdx 사용자 식별자
     * @param del 삭제 상태
     * @return 조건에 맞는 UsersAddress 엔티티 (Optional)
     */
    Optional<UsersAddress> findByAddressCodeAndUsersIdxAndDel(String addressCode, Long usersIdx, UserDel del);
}