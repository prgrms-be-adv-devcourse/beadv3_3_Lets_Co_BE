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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserQueryServiceImpl implements UserQueryService {
    private final UserRepository userRepository;
    private final UserInformationRepository userInformationRepository;
    private final UserAddressRepository userAddressRepository;
    private final UserCardRepository userCardRepository;

    @Override
    public Users findWaitUser(Long userIdx) {
        return userRepository.findByUsersIdxAndDel(userIdx, UserDel.PENDING)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않거나 대기 상태가 아닌 사용자입니다."));
    }

    @Override
    public Users findActiveUser(Long userIdx) {
        Users user = userRepository.findByUsersIdxAndDel(userIdx, UserDel.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        user.checkAccountStatus();
        return user;
    }

    @Override
    public Users findActiveUserById(String id) {
        Users user = userRepository.findByIdAndDel(id, UserDel.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이디입니다."));
        user.checkAccountStatus();
        return user;
    }

    @Override
    public boolean existsActiveId(String id) {
        return userRepository.existsByIdAndDel(id, UserDel.ACTIVE);
    }

    @Override
    public List<Users> findAllActiveUsers() {
        return userRepository.findAllByDel(UserDel.ACTIVE);
    }

    @Override
    public UsersInformation findWaitUserInfo(Long userIdx) {
        return userInformationRepository.findByUsersIdxAndDel(userIdx, UserDel.PENDING)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않거나 대기 상태가 아닌 사용자입니다."));
    }

    @Override
    public UsersInformation findActiveUserInfo(Long userIdx) {
        this.findActiveUser(userIdx);
        return userInformationRepository.findByUsersIdxAndDel(userIdx, UserDel.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("사용자 상세 정보를 찾을 수 없습니다."));
    }

    @Override
    public Map<Long, UsersInformation> findActiveUserInfos(List<Long> userIdxList) {
        return userInformationRepository.findAllByUsersIdxInAndDel(userIdxList, UserDel.ACTIVE)
                .stream()
                .collect(Collectors.toMap(UsersInformation::getUsersIdx, Function.identity()));
    }

    @Override
    public List<UsersAddress> findActiveAddresses(Long userIdx) {
        return userAddressRepository.findAllByUsersIdxAndDel(userIdx, UserDel.ACTIVE);
    }

    @Override
    public List<UserCard> findActiveCards(Long userIdx) {
        return userCardRepository.findAllByUsersIdxAndDel(userIdx, UserDel.ACTIVE);
    }
}