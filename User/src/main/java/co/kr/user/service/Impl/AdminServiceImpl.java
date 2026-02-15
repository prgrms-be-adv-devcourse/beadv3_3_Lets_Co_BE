package co.kr.user.service.Impl;

import co.kr.user.model.dto.address.AddressListDTO;
import co.kr.user.model.dto.admin.AdminItemsPerPageReq;
import co.kr.user.model.dto.admin.AdminUserDetailDTO;
import co.kr.user.model.dto.admin.AdminUserListDTO;
import co.kr.user.model.dto.card.CardListDTO;
import co.kr.user.model.entity.*;
import co.kr.user.model.vo.DBSorting;
import co.kr.user.model.vo.UsersRole;
import co.kr.user.service.AdminService;
import co.kr.user.service.UserQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * AdminService 인터페이스의 구현체입니다.
 * 관리자 전용 기능(회원 관리, 정지, 탈퇴 등)을 처리합니다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminServiceImpl implements AdminService {
    // Redis 작업을 위한 템플릿 (토큰 블랙리스트 처리 등)
    private final RedisTemplate<String, Object> redisTemplate;
    // 회원 정보 조회를 위한 공통 서비스
    private final UserQueryService userQueryService;

    @Value("${custom.security.redis.rt-prefix}")
    private String rtPrefix; // Refresh Token Key 접두사

    @Value("${custom.security.redis.bl-prefix}")
    private String blPrefix; // Blacklist Key 접두사

    /**
     * 요청자가 관리자(ADMIN) 권한을 가지고 있는지 검증하는 내부 메서드입니다.
     * @param adminIdx 요청자의 사용자 식별자
     */
    private void checkAdminRole(Long adminIdx) {
        Users admin = userQueryService.findActiveUser(adminIdx);
        if (admin.getRole() != UsersRole.ADMIN) {
            throw new IllegalStateException("권한이 없습니다.");
        }
    }

    /**
     * 전체 회원 목록을 조회하고, 페이징 및 정렬을 수행합니다.
     * [성능 개선] 기존의 전체 조회 후 메모리 페이징 방식을 제거하고, DB 페이징(Pageable)을 적용했습니다.
     *
     * @param userIdx 관리자 식별자
     * @param page 현재 페이지 번호
     * @param adminItemsPerPageReq 페이지당 항목 수 및 정렬 옵션
     * @return 관리자용 회원 목록 DTO 리스트
     */
    @Override
    public List<AdminUserListDTO> userList(Long userIdx, int page, AdminItemsPerPageReq adminItemsPerPageReq) {
        // 1. 관리자 권한 확인
        checkAdminRole(userIdx);

        // 2. 정렬 조건(Sort) 생성
        Sort sort = createSort(adminItemsPerPageReq);

        // 3. Pageable 객체 생성 (페이지는 0부터 시작하므로 page - 1 처리)
        // 사용자가 1페이지를 요청하면 0번 페이지를 조회해야 함
        int pageNumber = Math.max(page - 1, 0);
        Pageable pageable = PageRequest.of(pageNumber, adminItemsPerPageReq.getItemsPerPage(), sort);

        // 4. DB 페이징 조회 (필요한 데이터만 조회됨)
        Page<Users> userPage = userQueryService.findActiveUsersWithPaging(pageable);
        List<Users> usersList = userPage.getContent();

        // 조회된 데이터가 없으면 빈 리스트 반환
        if (usersList.isEmpty()) {
            return Collections.emptyList();
        }

        // 5. 조회된 사용자들의 상세 정보를 한 번에 조회 (N+1 문제 방지)
        List<Long> userIdxList = usersList.stream()
                .map(Users::getUsersIdx)
                .toList();

        Map<Long, UsersInformation> userInfoMap = userQueryService.findActiveUserInfos(userIdxList);

        // 6. 엔티티를 DTO로 변환
        return usersList.stream()
                .map(user -> {
                    AdminUserListDTO dto = new AdminUserListDTO();
                    dto.setRole(user.getRole());
                    dto.setId(user.getId());
                    dto.setMembership(user.getMembership());
                    dto.setLockedUntil(user.getLockedUntil());
                    dto.setCreatedAt(user.getCreatedAt());
                    dto.setUpdatedAt(user.getUpdatedAt());

                    // 상세 정보 Map에서 해당 사용자의 이름 가져오기
                    UsersInformation info = userInfoMap.get(user.getUsersIdx());
                    if (info != null) {
                        dto.setName(info.getName());
                    }
                    return dto;
                })
                .collect(Collectors.toList());
    }

    /**
     * AdminItemsPerPageReq의 정렬 조건을 Spring Data Sort로 변환하는 헬퍼 메서드
     * 주의: NAME 정렬 등 Join이 필요한 필드는 기본적으로 가입일 정렬 등으로 대체될 수 있습니다.
     */
    private Sort createSort(AdminItemsPerPageReq req) {
        String property;
        switch (req.getColum()) {
            case ID -> property = "id";
            case ROLE -> property = "role";
            case MEMBERSHIP -> property = "membership";
            // NAME은 Users 테이블에 없으므로, 현재 구조에서는 가입일 정렬로 대체하거나
            // 추후 QueryDSL 등으로 Join 정렬을 구현해야 합니다. 여기서는 안전하게 가입일순으로 처리합니다.
            case NAME -> property = "createdAt";
            default -> property = "createdAt";
        }

        Sort.Direction direction = (req.getSorting() == DBSorting.ASC)
                ? Sort.Direction.ASC : Sort.Direction.DESC;

        return Sort.by(direction, property);
    }

    /**
     * 특정 회원의 상세 정보를 조회합니다. (주소, 카드 정보 포함)
     * @param userIdx 관리자 식별자
     * @param id 대상 회원의 아이디
     * @return 회원 상세 정보 DTO
     */
    @Override
    public AdminUserDetailDTO userDetail(Long userIdx, String id) {
        checkAdminRole(userIdx);

        // 대상 회원 및 상세 정보 조회
        Users targetUser = userQueryService.findActiveUserById(id);
        UsersInformation targetInfo = userQueryService.findActiveUserInfo(targetUser.getUsersIdx());

        // 대상 회원의 주소 목록 조회 및 DTO 변환 (기본 배송지 우선 정렬)
        List<AddressListDTO> addressList = userQueryService.findActiveAddresses(targetUser.getUsersIdx()).stream()
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
                .toList();

        // 대상 회원의 카드 목록 조회 및 DTO 변환 (기본 카드 우선 정렬)
        List<CardListDTO> cardList = userQueryService.findActiveCards(targetUser.getUsersIdx()).stream()
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
                .toList();

        // 최종 DTO 조립
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

    /**
     * 회원의 권한을 변경합니다.
     * @param userIdx 관리자 식별자
     * @param id 대상 회원 아이디
     * @param changeRole 변경할 권한
     * @return 결과 메시지
     */
    @Override
    @Transactional
    public String userRole(Long userIdx, String id, UsersRole changeRole) {
        checkAdminRole(userIdx);
        Users targetUser = userQueryService.findActiveUserById(id);
        targetUser.assignRole(changeRole); // 변경 감지(Dirty Checking)로 업데이트
        return targetUser.getId() + "의 권한이 " + changeRole + "으로 변경되었습니다.";
    }

    /**
     * 회원을 일정 기간 동안 정지(잠금)시킵니다.
     * 정지 시 해당 회원의 로그인 토큰을 무효화합니다.
     * @param userIdx 관리자 식별자
     * @param id 대상 회원 아이디
     * @param lockedUntil 정지 해제 일시
     * @return 결과 메시지
     */
    @Override
    @Transactional
    public String userBlock(Long userIdx, String id, LocalDateTime lockedUntil) {
        checkAdminRole(userIdx);
        // 과거 날짜로 정지할 수 없음
        if (lockedUntil.isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("정지 해제 일시는 현재 시간보다 이후여야 합니다.");
        }

        Users targetUser = userQueryService.findActiveUserById(id);
        targetUser.suspendUser(lockedUntil); // 잠금 일시 설정

        // 강제 로그아웃 처리: Redis에서 Refresh Token을 삭제하고 블랙리스트에 등록
        String rtKey = rtPrefix + targetUser.getUsersIdx();
        String refreshToken = (String) redisTemplate.opsForValue().get(rtKey);
        if (refreshToken != null) {
            redisTemplate.delete(rtKey);
            redisTemplate.opsForValue().set(blPrefix + refreshToken, "BLOCK_BY_ADMIN");
        }

        return targetUser.getId() + "의 계정이 " + lockedUntil + "까지 정지되었습니다.";
    }

    /**
     * 회원을 강제로 탈퇴시킵니다.
     * 탈퇴 시 개인정보를 식별 불가능하게 변경하고 토큰을 무효화합니다.
     * @param userIdx 관리자 식별자
     * @param id 대상 회원 아이디
     * @return 결과 메시지
     */
    @Override
    @Transactional
    public String userDelete(Long userIdx, String id) {
        checkAdminRole(userIdx);
        Users targetUser = userQueryService.findActiveUserById(id);
        UsersInformation targetInfo = userQueryService.findActiveUserInfo(targetUser.getUsersIdx());

        // 사용자 정보에 탈퇴 표식 추가 (ID/이메일에 삭제 시간 등을 붙여 중복 가입 가능하게 하거나 이력 보존)
        targetUser.deleteUsers(targetUser.getId() + "_DEL_" + LocalDateTime.now());
        targetInfo.deleteInformation(targetInfo.getMail() + "_DEL_" + LocalDateTime.now());

        // 강제 로그아웃 처리: Redis 토큰 삭제 및 블랙리스트 등록
        String rtKey = rtPrefix + targetUser.getUsersIdx();
        String refreshToken = (String) redisTemplate.opsForValue().get(rtKey);
        if (refreshToken != null) {
            redisTemplate.delete(rtKey);
            redisTemplate.opsForValue().set(blPrefix + refreshToken, "DELETED_BY_ADMIN");
        }

        return targetUser.getId() + "의 계정이 삭제되었습니다.";
    }
}