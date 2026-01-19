package co.kr.user.DAO;

import co.kr.user.model.entity.UserCard;
import co.kr.user.model.entity.UsersAddress;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * 사용자 배송지(UsersAddress) 엔티티의 데이터베이스 접근을 담당하는 리포지토리 인터페이스입니다.
 * 기본 배송지 조회, 특정 주소 검색, 전체 목록 조회 등의 기능을 제공합니다.
 */
public interface UserAddressRepository extends JpaRepository<UsersAddress, Long> {
    /**
     * 특정 사용자의 '기본 배송지'를 조회하는 메서드입니다.
     * 여러 개의 주소 중 Default_Address 값이 일치하는(보통 1) 가장 최신의 주소를 가져옵니다.
     * 삭제된 주소(del=1)는 제외합니다.
     *
     * @param usersIdx 사용자 고유 식별자
     * @param defaultAddress 기본 배송지 여부 (1: 기본, 0: 일반)
     * @param del 삭제 상태 플래그 (0: 정상)
     * @return 조건에 맞는 기본 배송지 정보 (Optional)
     */
    Optional<UsersAddress> findFirstByUsersIdxAndDefaultAddressAndDelOrderByAddressIdxDesc(Long usersIdx, int defaultAddress, int del);

    /**
     * 주소 코드(AddressCode)를 이용하여 특정 사용자의 배송지 상세 정보를 조회하는 메서드입니다.
     * 보안상 PK(AddressIdx) 대신 UUID 등으로 생성된 AddressCode를 사용할 때 호출됩니다.
     *
     * @param usersIdx 사용자 고유 식별자 (본인 확인용)
     * @param addressCode 조회할 주소의 고유 코드
     * @param del 삭제 상태 플래그 (0: 정상)
     * @return 조건에 맞는 배송지 정보 (Optional)
     */
    Optional<UsersAddress> findFirstByUsersIdxAndAddressCodeAndDelOrderByAddressIdxDesc(Long usersIdx, String addressCode, int del);

    /**
     * 삭제되지 않은 사용자의 모든 배송지 목록을 조회하는 메서드입니다.
     * 배송지 관리 화면 등에서 리스트를 보여줄 때 사용됩니다.
     *
     * @param usersIdx 사용자 고유 식별자
     * @param del 삭제 상태 플래그 (0: 정상)
     * @return 사용자의 모든 정상 배송지 리스트
     */
    List<UsersAddress> findAllByUsersIdxAndDel(Long usersIdx, int del);

    /**
     * 주소 코드(AddressCode)만으로 배송지 정보를 조회하는 메서드입니다.
     * 사용자 식별자 없이 코드 자체의 유효성을 검증하거나 정보를 찾을 때 사용될 수 있습니다.
     *
     * @param addressCode 조회할 주소의 고유 코드
     * @return 해당 코드를 가진 배송지 정보 (Optional)
     */
    Optional<UsersAddress> findByAddressCode(String addressCode);
}