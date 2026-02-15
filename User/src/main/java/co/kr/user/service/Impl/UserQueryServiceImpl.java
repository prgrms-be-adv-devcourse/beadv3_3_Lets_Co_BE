package co.kr.user.service.Impl;

import co.kr.user.dao.UserInformationRepository;
import co.kr.user.dao.UserRepository;
import co.kr.user.dao.UserAddressRepository;
import co.kr.user.dao.UserCardRepository;
import co.kr.user.model.entity.Users;
import co.kr.user.model.entity.UsersInformation;
import co.kr.user.model.entity.UsersAddress;
import co.kr.user.model.entity.UserCard;
import co.kr.user.model.vo.UserDel;
import co.kr.user.service.UserQueryService;
import co.kr.user.util.AESUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * UserQueryService 인터페이스의 구현체입니다.
 * 사용자 정보 조회를 전담하며, 반복적인 조회 로직을 중앙화하여 관리합니다.
 * 주로 다른 서비스 레이어에서 호출하여 사용합니다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 모든 메서드에 대해 기본적으로 읽기 전용 트랜잭션 적용
public class UserQueryServiceImpl implements UserQueryService {
    private final UserRepository userRepository;
    private final UserInformationRepository userInformationRepository;
    private final UserAddressRepository userAddressRepository;
    private final UserCardRepository userCardRepository;

    /**
     * 대기 상태(PENDING)의 사용자를 식별자로 조회합니다.
     * 가입 승인 대기 중인 사용자를 찾을 때 사용합니다.
     */
    @Override
    public Users findWaitUser(Long userIdx) {
        return userRepository.findByUsersIdxAndDel(userIdx, UserDel.PENDING)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않거나 대기 상태가 아닌 사용자입니다."));
    }

    /**
     * 활성 상태(ACTIVE)의 사용자를 식별자로 조회합니다.
     * 일반적인 비즈니스 로직에서 유효한 회원을 찾을 때 사용합니다.
     */
    @Override
    public Users findActiveUser(Long userIdx) {
        Users user = userRepository.findByUsersIdxAndDel(userIdx, UserDel.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        // 계정의 잠금 상태 등 추가적인 상태 체크 수행
        user.checkAccountStatus();
        return user;
    }

    /**
     * 활성 상태(ACTIVE)의 사용자를 아이디(String)로 조회합니다.
     * 로그인 등에 사용됩니다.
     */
    @Override
    public Users findActiveUserById(String id) {
        Users user = userRepository.findByIdAndDel(id, UserDel.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이디입니다."));
        user.checkAccountStatus();
        return user;
    }

    /**
     * 활성 상태인 특정 아이디가 존재하는지 확인합니다.
     * 회원가입 시 아이디 중복 체크용입니다.
     */
    @Override
    public boolean existsActiveId(String id) {
        return userRepository.existsByIdAndDel(id, UserDel.ACTIVE);
    }

    /**
     * 모든 활성 사용자를 조회합니다.
     * (주의: 데이터 양이 많을 경우 성능 이슈가 발생할 수 있습니다.)
     */
    @Override
    public Page<Users> findActiveUsersWithPaging(Pageable pageable) {
        return userRepository.findAllByDel(UserDel.ACTIVE, pageable);
    }

    /**
     * 대기 상태인 사용자의 상세 정보(UsersInformation)를 조회합니다.
     */
    @Override
    public UsersInformation findWaitUserInfo(Long userIdx) {
        return userInformationRepository.findByUsersIdxAndDel(userIdx, UserDel.PENDING)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않거나 대기 상태가 아닌 사용자입니다."));
    }

    /**
     * 활성 상태인 사용자의 상세 정보(UsersInformation)를 조회합니다.
     * 내부적으로 findActiveUser를 먼저 호출하여 기본 사용자 존재 여부를 검증합니다.
     */
    @Override
    public UsersInformation findActiveUserInfo(Long userIdx) {
        this.findActiveUser(userIdx); // 기본 사용자 검증
        return userInformationRepository.findByUsersIdxAndDel(userIdx, UserDel.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("사용자 상세 정보를 찾을 수 없습니다."));
    }

    /**
     * [동시성 제어용] 비관적 락(Pessimistic Lock)을 걸고 사용자 정보를 조회합니다.
     * 잔액 변경 등 중요한 업데이트 시에 사용해야 합니다.
     */
    @Override
    public UsersInformation findActiveUserInfoForUpdate(Long userIdx) {
        this.findActiveUser(userIdx);

        // 2. 락이 걸린 Repository 메서드 호출 (readBy... 사용)
        return userInformationRepository.readByUsersIdxAndDel(userIdx, UserDel.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("사용자 상세 정보를 찾을 수 없습니다."));

    }


    /**
     * 여러 사용자 식별자 목록에 해당하는 상세 정보들을 한 번에 조회하여 Map으로 반환합니다.
     * N+1 문제를 방지하기 위해 사용됩니다.
     * Key: UserIdx, Value: UsersInformation
     */
    @Override
    public Map<Long, UsersInformation> findActiveUserInfos(List<Long> userIdxList) {
        return userInformationRepository.findAllByUsersIdxInAndDel(userIdxList, UserDel.ACTIVE)
                .stream()
                .collect(Collectors.toMap(UsersInformation::getUsersIdx, Function.identity()));
    }

    /**
     * 사용자의 활성 배송지 목록을 조회합니다.
     */
    @Override
    public List<UsersAddress> findActiveAddresses(Long userIdx) {
        return userAddressRepository.findAllByUsersIdxAndDel(userIdx, UserDel.ACTIVE);
    }

    /**
     * 사용자의 활성 카드 목록을 조회합니다.
     */
    @Override
    public List<UserCard> findActiveCards(Long userIdx) {
        return userCardRepository.findAllByUsersIdxAndDel(userIdx, UserDel.ACTIVE);
    }
}