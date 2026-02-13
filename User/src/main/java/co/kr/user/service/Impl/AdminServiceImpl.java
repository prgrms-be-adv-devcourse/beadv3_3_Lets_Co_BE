package co.kr.user.service.Impl;

import co.kr.user.dao.UserAddressRepository;
import co.kr.user.dao.UserCardRepository;
import co.kr.user.dao.UserInformationRepository;
import co.kr.user.dao.UserRepository;
import co.kr.user.model.dto.address.AddressListDTO;
import co.kr.user.model.dto.admin.AdminItemsPerPageReq;
import co.kr.user.model.dto.admin.AdminUserDetailDTO;
import co.kr.user.model.dto.admin.AdminUserListDTO;
import co.kr.user.model.dto.card.CardListDTO;
import co.kr.user.model.entity.UserCard;
import co.kr.user.model.entity.Users;
import co.kr.user.model.entity.UsersAddress;
import co.kr.user.model.entity.UsersInformation;
import co.kr.user.model.vo.DBSorting;
import co.kr.user.model.vo.UsersRole;
import co.kr.user.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminServiceImpl implements AdminService {
    private final UserRepository userRepository;
    private final UserInformationRepository userInformationRepository;
    private final UserAddressRepository userAddressRepository;
    private final UserCardRepository userCardRepository;

    private final RedisTemplate<String, Object> redisTemplate;

    private final UserQueryServiceImpl userQueryServiceImpl;

    @Override
    public List<AdminUserListDTO> userList(Long userIdx, int page, AdminItemsPerPageReq adminItemsPerPageReq) {
        Users admin = userQueryServiceImpl.findActiveUser(userIdx);
        if (admin.getRole() != UsersRole.ADMIN) {
            throw new IllegalStateException("권한이 없습니다.");
        }

        List<Users> usersList = userRepository.findAllByDel(0);
        List<Long> userIdxList = usersList.stream()
                .map(Users::getUsersIdx)
                .collect(Collectors.toList());

        List<UsersInformation> usersInformationList = userInformationRepository.findAllByUsersIdxInAndDel(userIdxList, 0);

        Map<Long, UsersInformation> userInfoMap = usersInformationList.stream()
                .collect(Collectors.toMap(UsersInformation::getUsersIdx, Function.identity()));

        List<AdminUserListDTO> dtoList = usersList.stream()
                .map(user -> {
                    AdminUserListDTO dto = new AdminUserListDTO();
                    dto.setRole(user.getRole());
                    dto.setId(user.getId());
                    dto.setMembership(user.getMembership());
                    dto.setLockedUntil(user.getLockedUntil());
                    dto.setCreatedAt(user.getCreatedAt());
                    dto.setUpdatedAt(user.getUpdatedAt());

                    UsersInformation info = userInfoMap.get(user.getUsersIdx());
                    if (info != null) {
                        dto.setName(info.getName());
                    }
                    return dto;
                })
                .collect(Collectors.toList());

        Comparator<AdminUserListDTO> comparator;
        switch (adminItemsPerPageReq.getColum()) {
            case ID:
                comparator = Comparator.comparing(AdminUserListDTO::getId, Comparator.nullsLast(Comparator.naturalOrder()));
                break;
            case NAME:
                comparator = Comparator.comparing(AdminUserListDTO::getName, Comparator.nullsLast(Comparator.naturalOrder()));
                break;
            case ROLE:
                comparator = Comparator.comparing(AdminUserListDTO::getRole);
                break;
            case MEMBERSHIP:
                comparator = Comparator.comparing(AdminUserListDTO::getMembership);
                break;
            default:
                comparator = Comparator.comparing(AdminUserListDTO::getCreatedAt, Comparator.reverseOrder());
        }

        if (adminItemsPerPageReq.getSorting() == DBSorting.DESC) {
            comparator = comparator.reversed();
        }

        dtoList.sort(comparator);

        page = Math.max(page, 1) - 1; // 페이지 번호는 0부터 시작하도록 조정

        int pageSize = adminItemsPerPageReq.getItemsPerPage();
        int totalSize = dtoList.size();
        int start = Math.min(page * pageSize, totalSize);
        int end = Math.min(start + pageSize, totalSize);

        if (start >= totalSize) {
            return Collections.emptyList();
        }

        return dtoList.subList(start, end);
    }

    @Override
    public AdminUserDetailDTO userDetail(Long userIdx, String id) {
        Users admin = userQueryServiceImpl.findActiveUser(userIdx);
        if (admin.getRole() != UsersRole.ADMIN) {
            throw new IllegalStateException("권한이 없습니다.");
        }

        Users usersDetail = userRepository.findByIdAndDel(id, 0)
                .orElseThrow(() -> new IllegalArgumentException("조회 대상 사용자(ID: " + id + ")를 찾을 수 없거나 탈퇴한 회원입니다."));
        UsersInformation usersInformationDetail = userInformationRepository.findByUsersIdxAndDel(usersDetail.getUsersIdx(), 0)
                .orElseThrow(() -> new IllegalArgumentException("유저 상세 정보를 찾을 수 없습니다."));

        List<UsersAddress> addressEntities = userAddressRepository.findAllByUsersIdxAndDel(usersDetail.getUsersIdx(), 0);
        Long defaultAddrIdx = usersInformationDetail.getDefaultAddress(); // 유저 정보에 저장된 기본 주소 IDX

        List<AddressListDTO> addressList = addressEntities.stream()
                .map(addr -> {
                    AddressListDTO addressListDTO = new AddressListDTO();
                    boolean isDefault = addr.getAddressIdx().equals(defaultAddrIdx);
                    addressListDTO.setDefaultAddress(isDefault ? 1 : 0);

                    addressListDTO.setAddressCode(addr.getAddressCode());
                    addressListDTO.setRecipient(addr.getRecipient());
                    addressListDTO.setAddress(addr.getAddress());
                    addressListDTO.setAddressDetail(addr.getAddressDetail());
                    addressListDTO.setPhoneNumber(addr.getPhoneNumber());
                    return addressListDTO;
                })
                .sorted((a, b) -> {
                    // 기본 주소(1)인 항목을 가장 위로 정렬
                    return Integer.compare(b.getDefaultAddress(), a.getDefaultAddress());
                })
                .collect(Collectors.toList());

        List<UserCard> cardEntities = userCardRepository.findAllByUsersIdxAndDel(usersDetail.getUsersIdx(), 0);
        Long defaultCardIdx = usersInformationDetail.getDefaultCard();

        List<CardListDTO> cardList = cardEntities.stream()
                .map(card -> {
                    CardListDTO cardListDTO = new CardListDTO();
                    boolean isDefault = card.getCardIdx().equals(defaultCardIdx);
                    cardListDTO.setDefaultCard(isDefault ? 1 : 0);

                    cardListDTO.setCardCode(card.getCardCode());
                    cardListDTO.setCardBrand(card.getCardBrand());
                    cardListDTO.setCardName(card.getCardName());
                    cardListDTO.setCardToken(card.getCardToken());
                    cardListDTO.setExpMonth(card.getExpMonth());
                    cardListDTO.setExpYear(card.getExpYear());
                    return cardListDTO;
                })
                .sorted((a, b) -> Integer.compare(b.getDefaultCard(), a.getDefaultCard()))
                .collect(Collectors.toList());

        AdminUserDetailDTO dto = new AdminUserDetailDTO();
        dto.setId(usersDetail.getId());
        dto.setLockedUntil(usersDetail.getLockedUntil());
        dto.setRole(usersDetail.getRole());
        dto.setMembership(usersDetail.getMembership());
        dto.setAgreeTermsAt(usersDetail.getAgreeTermsAt());
        dto.setAgreePrivacyAt(usersDetail.getAgreePrivacyAt());
        dto.setCreatedAt(usersDetail.getCreatedAt());
        dto.setUpdatedAt(usersDetail.getUpdatedAt());

        dto.setName(usersInformationDetail.getName());
        dto.setPhoneNumber(usersInformationDetail.getPhoneNumber());
        dto.setBirth(usersInformationDetail.getBirth());
        dto.setMail(usersInformationDetail.getMail());
        dto.setGender(usersInformationDetail.getGender());
        dto.setBalance(usersInformationDetail.getBalance());
        dto.setAgreeMarketingAt(usersInformationDetail.getAgreeMarketingAt());

        dto.setAddressListDTO(addressList);
        dto.setCardListDTO(cardList);

        return dto;
    }

    @Override
    @Transactional
    public String userRole(Long userIdx, String id, UsersRole changeRole) {
        Users admin = userQueryServiceImpl.findActiveUser(userIdx);
        if (admin.getRole() != UsersRole.ADMIN) {
            throw new IllegalStateException("권한이 없습니다.");
        }

        // 대상 회원 조회 및 권한 변경
        Users userData = userRepository.findByIdAndDel(id, 0)
                .orElseThrow(() -> new IllegalArgumentException(("존재하지 않는 아이디입니다.")));

        userData.assignRole(changeRole);

        return userData.getId() + "의 권한이 " + changeRole + "으로 변경되었습니다.";
    }

    @Override
    @Transactional
    public String userBlock(Long userIdx, String id, LocalDateTime lockedUntil) {
        Users admin = userQueryServiceImpl.findActiveUser(userIdx);
        if (admin.getRole() != UsersRole.ADMIN) {
            throw new IllegalStateException("권한이 없습니다.");
        }

        if (lockedUntil.isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("정지 해제 일시는 현재 시간보다 이후여야 합니다.");
        }

        Users userData = userRepository.findByIdAndDel(id, 0)
                .orElseThrow(() -> new IllegalArgumentException(("존재하지 않는 아이디입니다.")));

        userData.suspendUser(lockedUntil);

        String rtKey = "RT:" + userData.getUsersIdx();
        String refreshToken = (String) redisTemplate.opsForValue().get(rtKey);

        if (refreshToken != null) {
            redisTemplate.delete(rtKey);
            redisTemplate.opsForValue().set("BL:" + refreshToken, "BLOCK_BY_ADMIN");
            log.info("유저 {}의 활성 토큰을 폐기하고 블랙리스트에 등록했습니다.", id);
        } else {
            log.info("유저 {}는 현재 활성화된 세션(RT)이 없습니다. 계정만 정지 처리되었습니다.", id);
        }

        return userData.getId() + "의 계정이 " + lockedUntil + "까지 정지되었습니다.";
    }

    @Override
    @Transactional
    public String userDelete(Long userIdx, String id) {
        Users users = userQueryServiceImpl.findActiveUser(userIdx);

        UsersRole role = users.getRole();
        if (role != UsersRole.ADMIN) {
            throw new IllegalStateException("권한이 없습니다.");
        }

        Users userData = userRepository.findByIdAndDel(id, 0)
                .orElseThrow(() -> new IllegalArgumentException(("존재하지 않는 아이디입니다.")));
        UsersInformation usersInformation = userInformationRepository.findByUsersIdxAndDel(userData.getUsersIdx(), 0)
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보를 찾을 수 없습니다."));

        userData.deleteUsers(users.getId() + "_DEL_" + LocalDateTime.now());
        usersInformation.deleteInformation(usersInformation.getMail() + "_DEL_" + LocalDateTime.now());

        String rtKey = "RT:" + userData.getUsersIdx();
        String refreshToken = (String) redisTemplate.opsForValue().get(rtKey);

        if (refreshToken != null) {
            redisTemplate.delete(rtKey);

            redisTemplate.opsForValue().set("BL:" + refreshToken, "DELETED_BY_ADMIN");

            log.info("관리자(Idx: {})에 의해 유저 {}의 계정이 삭제되었으며, 활성 토큰이 폐기되었습니다.", userIdx, id);
        } else {
            log.info("관리자(Idx: {})에 의해 유저 {}의 계정이 삭제되었습니다. (활성 세션 없음)", userIdx, id);
        }

        return userData.getId() + "의 계정이 삭제되었습니다.";
    }
}