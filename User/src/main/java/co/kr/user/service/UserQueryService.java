package co.kr.user.service;

import co.kr.user.model.entity.UserCard;
import co.kr.user.model.entity.Users;
import co.kr.user.model.entity.UsersAddress;
import co.kr.user.model.entity.UsersInformation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

/**
 * 사용자 정보의 단순 조회(Read-Only)를 전담하는 서비스 인터페이스입니다.
 * 데이터 변경 없이 엔티티를 찾거나 존재 여부를 확인하는 로직을 분리하여 재사용성을 높였습니다.
 * 주로 다른 서비스(Service) 구현체 내부에서 호출되어 사용됩니다.
 */
public interface UserQueryService {

    /**
     * 대기 상태(PENDING, 예: 이메일 인증 전)인 사용자를 조회합니다.
     * * @param userIdx 사용자 식별자 (PK)
     * @return 대기 상태인 Users 엔티티
     * @throws IllegalArgumentException 사용자가 없거나 대기 상태가 아닐 경우
     */
    Users findWaitUser(Long userIdx);

    /**
     * 활성 상태(ACTIVE, 정상 회원)인 사용자를 조회합니다.
     * 탈퇴했거나 대기 중인 회원은 조회되지 않습니다.
     * * @param userIdx 사용자 식별자 (PK)
     * @return 활성 상태인 Users 엔티티
     * @throws IllegalArgumentException 사용자가 없거나 활성 상태가 아닐 경우
     */
    Users findActiveUser(Long userIdx);

    /**
     * 아이디(String)를 사용하여 활성 상태인 사용자를 조회합니다.
     * 로그인 등 아이디 기반 로직에서 사용됩니다.
     * * @param id 사용자 아이디
     * @return 활성 상태인 Users 엔티티
     */
    Users findActiveUserById(String id);

    /**
     * 특정 아이디를 가진 활성 사용자가 존재하는지 확인합니다.
     * 회원가입 시 중복 아이디 체크 등에 사용됩니다.
     * * @param id 확인할 사용자 아이디
     * @return 존재하면 true, 아니면 false
     */
    boolean existsActiveId(String id);

    /**
     * 모든 활성 상태의 사용자 목록을 조회합니다.
     * (주의: 데이터가 많을 경우 성능 이슈가 발생할 수 있으므로 관리자 기능 등 제한된 곳에서 사용해야 함)
     * * @return 활성 상태인 모든 Users 엔티티 리스트
     */
    Page<Users> findActiveUsersWithPaging(Pageable pageable);

    /**
     * 대기 상태인 사용자의 부가 정보(UsersInformation)를 조회합니다.
     * * @param userIdx 사용자 식별자 (PK)
     * @return 대기 상태인 UsersInformation 엔티티
     */
    UsersInformation findWaitUserInfo(Long userIdx);

    /**
     * 활성 상태인 사용자의 부가 정보(UsersInformation)를 조회합니다.
     * 이름, 전화번호, 생년월일 등의 개인정보가 포함됩니다.
     * * @param userIdx 사용자 식별자 (PK)
     * @return 활성 상태인 UsersInformation 엔티티
     */
    UsersInformation findActiveUserInfo(Long userIdx);

    /**
     * [동시성 제어용] 비관적 락(Pessimistic Lock)을 걸고 사용자 상세 정보를 조회합니다.
     * 잔액 변경, 결제 등 데이터 정합성이 중요한 로직에서 사용해야 합니다.
     * 이 메서드로 조회된 엔티티는 트랜잭션이 끝날 때까지 다른 요청에서 수정할 수 없습니다.
     * @param userIdx 사용자 식별자 (PK)
     * @return 락이 걸린 상태의 UsersInformation 엔티티
     */
    UsersInformation findActiveUserInfoForUpdate(Long userIdx);

    /**
     * 여러 사용자 식별자(PK List)에 해당하는 활성 사용자 정보들을 한 번에 조회합니다.
     * 관리자 페이지 등에서 목록을 표시할 때 N+1 문제를 방지하기 위해 유용합니다.
     * * @param userIdxList 조회할 사용자 식별자 리스트
     * @return 사용자 식별자를 키(Key)로 하는 UsersInformation 맵(Map)
     */
    Map<Long, UsersInformation> findActiveUserInfos(List<Long> userIdxList);

    /**
     * 특정 사용자의 활성 상태인 배송지 목록을 조회합니다.
     * * @param userIdx 사용자 식별자 (PK)
     * @return UsersAddress 엔티티 리스트
     */
    List<UsersAddress> findActiveAddresses(Long userIdx);

    /**
     * 특정 사용자의 활성 상태인 카드 목록을 조회합니다.
     * * @param userIdx 사용자 식별자 (PK)
     * @return UserCard 엔티티 리스트
     */
    List<UserCard> findActiveCards(Long userIdx);
}