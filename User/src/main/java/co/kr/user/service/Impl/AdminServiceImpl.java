package co.kr.user.service.Impl;

import co.kr.user.dao.UserAddressRepository;
import co.kr.user.dao.UserCardRepository;
import co.kr.user.dao.UserInformationRepository;
import co.kr.user.model.dto.address.AddressListDTO;
import co.kr.user.model.dto.admin.AdminItemsPerPageReq;
import co.kr.user.model.dto.admin.AdminUserDetailDTO;
import co.kr.user.model.dto.admin.AdminUserListDTO;
import co.kr.user.model.dto.card.CardListDTO;
import co.kr.user.model.entity.*;
import co.kr.user.model.vo.DBSorting;
import co.kr.user.model.vo.UserDel;
import co.kr.user.model.vo.UsersRole;
import co.kr.user.service.AdminService;
import co.kr.user.service.UserQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminServiceImpl implements AdminService {
    private final UserInformationRepository userInformationRepository;
    private final UserAddressRepository userAddressRepository;
    private final UserCardRepository userCardRepository;

    private final RedisTemplate<String, Object> redisTemplate;

    private final UserQueryService userQueryService;

    private void checkAdminRole(Long adminIdx) {
        Users admin = userQueryService.findActiveUser(adminIdx); //
        if (admin.getRole() != UsersRole.ADMIN) {
            throw new IllegalStateException("권한이 없습니다.");
        }
    }

    @Override
    public List<AdminUserListDTO> userList(Long userIdx, int page, AdminItemsPerPageReq adminItemsPerPageReq) {
        checkAdminRole(userIdx);

        List<Users> usersList = userQueryService.findAllActiveUsers();
        List<Long> userIdxList = usersList.stream()
                .map(Users::getUsersIdx)
                .collect(Collectors.toList());

        Map<Long, UsersInformation> userInfoMap = userInformationRepository.findAllByUsersIdxInAndDel(userIdxList, UserDel.ACTIVE)
                .stream()
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
            case ID -> comparator = Comparator.comparing(AdminUserListDTO::getId, Comparator.nullsLast(Comparator.naturalOrder()));
            case NAME -> comparator = Comparator.comparing(AdminUserListDTO::getName, Comparator.nullsLast(Comparator.naturalOrder()));
            case ROLE -> comparator = Comparator.comparing(AdminUserListDTO::getRole);
            case MEMBERSHIP -> comparator = Comparator.comparing(AdminUserListDTO::getMembership);
            default -> comparator = Comparator.comparing(AdminUserListDTO::getCreatedAt, Comparator.reverseOrder());
        }

        if (adminItemsPerPageReq.getSorting() == DBSorting.DESC) {
            comparator = comparator.reversed();
        }

        dtoList.sort(comparator);
        page = Math.max(page, 1) - 1;

        int pageSize = adminItemsPerPageReq.getItemsPerPage();
        int totalSize = dtoList.size();
        int start = Math.min(page * pageSize, totalSize);
        int end = Math.min(start + pageSize, totalSize);

        if (start >= totalSize) return Collections.emptyList();
        return dtoList.subList(start, end);
    }

    @Override
    public AdminUserDetailDTO userDetail(Long userIdx, String id) {
        checkAdminRole(userIdx);

        Users targetUser = userQueryService.findActiveUserById(id); //
        UsersInformation targetInfo = userQueryService.findActiveUserInfo(targetUser.getUsersIdx()); //

        List<AddressListDTO> addressList = userAddressRepository.findAllByUsersIdxAndDel(targetUser.getUsersIdx(), UserDel.ACTIVE).stream()
                .map(addr -> {
                    AddressListDTO dto = new AddressListDTO();
                    dto.setDefaultAddress(addr.getAddressIdx().equals(targetInfo.getDefaultAddress()) ? 1 : 0);
                    dto.setAddressCode(addr.getAddressCode());
                    dto.setRecipient(addr.getRecipient());
                    dto.setAddress(addr.getAddress());
                    dto.setAddressDetail(addr.getAddressDetail());
                    dto.setPhoneNumber(addr.getPhoneNumber());
                    return dto;
                })
                .sorted((a, b) -> Integer.compare(b.getDefaultAddress(), a.getDefaultAddress()))
                .collect(Collectors.toList());

        List<CardListDTO> cardList = userCardRepository.findAllByUsersIdxAndDel(targetUser.getUsersIdx(), UserDel.ACTIVE).stream()
                .map(card -> {
                    CardListDTO dto = new CardListDTO();
                    dto.setDefaultCard(card.getCardIdx().equals(targetInfo.getDefaultCard()) ? 1 : 0);
                    dto.setCardCode(card.getCardCode());
                    dto.setCardBrand(card.getCardBrand());
                    dto.setCardName(card.getCardName());
                    dto.setCardToken(card.getCardToken());
                    dto.setExpMonth(card.getExpMonth());
                    dto.setExpYear(card.getExpYear());
                    return dto;
                })
                .sorted((a, b) -> Integer.compare(b.getDefaultCard(), a.getDefaultCard()))
                .collect(Collectors.toList());

        AdminUserDetailDTO dto = new AdminUserDetailDTO();
        dto.setId(targetUser.getId());
        dto.setLockedUntil(targetUser.getLockedUntil());
        dto.setRole(targetUser.getRole());
        dto.setMembership(targetUser.getMembership());
        dto.setAgreeTermsAt(targetUser.getAgreeTermsAt());
        dto.setAgreePrivacyAt(targetUser.getAgreePrivacyAt());
        dto.setCreatedAt(targetUser.getCreatedAt());
        dto.setUpdatedAt(targetUser.getUpdatedAt());
        dto.setName(targetInfo.getName());
        dto.setPhoneNumber(targetInfo.getPhoneNumber());
        dto.setBirth(targetInfo.getBirth());
        dto.setMail(targetInfo.getMail());
        dto.setGender(targetInfo.getGender());
        dto.setBalance(targetInfo.getBalance());
        dto.setAgreeMarketingAt(targetInfo.getAgreeMarketingAt());
        dto.setAddressListDTO(addressList);
        dto.setCardListDTO(cardList);

        return dto;
    }

    @Override
    @Transactional
    public String userRole(Long userIdx, String id, UsersRole changeRole) {
        checkAdminRole(userIdx);
        Users targetUser = userQueryService.findActiveUserById(id); //
        targetUser.assignRole(changeRole);
        return targetUser.getId() + "의 권한이 " + changeRole + "으로 변경되었습니다.";
    }

    @Override
    @Transactional
    public String userBlock(Long userIdx, String id, LocalDateTime lockedUntil) {
        checkAdminRole(userIdx);
        if (lockedUntil.isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("정지 해제 일시는 현재 시간보다 이후여야 합니다.");
        }

        Users targetUser = userQueryService.findActiveUserById(id); //
        targetUser.suspendUser(lockedUntil);

        String rtKey = "RT:" + targetUser.getUsersIdx();
        String refreshToken = (String) redisTemplate.opsForValue().get(rtKey);
        if (refreshToken != null) {
            redisTemplate.delete(rtKey);
            redisTemplate.opsForValue().set("BL:" + refreshToken, "BLOCK_BY_ADMIN");
        }

        return targetUser.getId() + "의 계정이 " + lockedUntil + "까지 정지되었습니다.";
    }

    @Override
    @Transactional
    public String userDelete(Long userIdx, String id) {
        checkAdminRole(userIdx);
        Users targetUser = userQueryService.findActiveUserById(id); //
        UsersInformation targetInfo = userQueryService.findActiveUserInfo(targetUser.getUsersIdx()); //

        targetUser.deleteUsers(targetUser.getId() + "_DEL_" + LocalDateTime.now());
        targetInfo.deleteInformation(targetInfo.getMail() + "_DEL_" + LocalDateTime.now());

        String rtKey = "RT:" + targetUser.getUsersIdx();
        String refreshToken = (String) redisTemplate.opsForValue().get(rtKey);
        if (refreshToken != null) {
            redisTemplate.delete(rtKey);
            redisTemplate.opsForValue().set("BL:" + refreshToken, "DELETED_BY_ADMIN");
        }

        return targetUser.getId() + "의 계정이 삭제되었습니다.";
    }
}