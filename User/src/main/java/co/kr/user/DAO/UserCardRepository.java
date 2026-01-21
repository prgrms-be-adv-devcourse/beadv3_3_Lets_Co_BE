package co.kr.user.DAO;

import co.kr.user.model.entity.UserCard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * 사용자 결제 수단(UserCard) 엔티티의 데이터베이스 접근을 담당하는 리포지토리 인터페이스입니다.
 * 등록된 카드 목록 조회, 기본 카드 확인, 특정 카드 상세 조회 등의 기능을 처리합니다.
 */
public interface UserCardRepository extends JpaRepository<UserCard, Long> {
    /**
     * 특정 사용자의 '기본 결제 카드'를 조회하는 메서드입니다.
     * Default_Card 값이 일치하는(보통 1) 가장 최근에 등록/수정된 카드를 가져옵니다.
     *
     * @param usersIdx 사용자 고유 식별자
     * @param defaultCard 기본 카드 여부 (1: 기본, 0: 일반)
     * @param del 삭제 상태 플래그 (0: 정상)
     * @return 조건에 맞는 기본 카드 정보 (Optional)
     */
    Optional<UserCard> findFirstByUsersIdxAndDefaultCardAndDelOrderByCardIdxDesc(Long usersIdx, int defaultCard, int del);

    /**
     * 카드 코드(CardCode)를 이용하여 특정 사용자의 카드 정보를 조회하는 메서드입니다.
     * 본인의 카드가 맞는지(usersIdx 일치 여부)와 삭제되지 않았는지(del=0)를 함께 검증합니다.
     *
     * @param userIdx 사용자 고유 식별자
     * @param cardCode 조회할 카드의 고유 코드
     * @param del 삭제 상태 플래그 (0: 정상)
     * @return 조건에 맞는 카드 정보 (Optional)
     */
    Optional<UserCard> findFirstByUsersIdxAndCardCodeAndDelOrderByCardIdxDesc(Long userIdx, String cardCode, int del);

    /**
     * 삭제되지 않은 사용자의 모든 등록 카드 목록을 조회하는 메서드입니다.
     * 결제 수단 관리 페이지 등에서 사용됩니다.
     *
     * @param usersIdx 사용자 고유 식별자
     * @param del 삭제 상태 플래그 (0: 정상)
     * @return 사용자의 모든 정상 카드 리스트
     */
    List<UserCard> findAllByUsersIdxAndDel(Long usersIdx, int del);

    /**
     * 카드 코드(CardCode)만으로 카드 정보를 조회하는 메서드입니다.
     * 특정 카드의 존재 여부를 확인하거나, 소유자 검증 전 단계에서 정보를 로드할 때 사용됩니다.
     *
     * @param cardCode 조회할 카드의 고유 코드
     * @param del
     * @return 해당 코드를 가진 카드 정보 (Optional)
     */
    Optional<UserCard> findByCardCodeAndDel(String cardCode, int del);
}