package co.kr.user.service.impl;

import co.kr.user.dao.*;
import co.kr.user.model.dto.client.BalanceReq;
import co.kr.user.model.dto.client.ClientAddressDTO;
import co.kr.user.model.dto.client.ClientRoleDTO;
import co.kr.user.model.dto.client.SellerBankDTO;
import co.kr.user.model.entity.*;
import co.kr.user.model.vo.PublicDel;
import co.kr.user.model.vo.UserDel;
import co.kr.user.service.ClientService;
import co.kr.user.service.S3Service;
import co.kr.user.service.UserQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;

/**
 * ClientService 인터페이스의 구현체입니다.
 * 클라이언트(사용자)의 권한 조회, 잔액 관리, 기본 배송지/카드 조회 등 편의 기능을 처리합니다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClientServiceImpl implements ClientService {
    // 필요한 Repository들 주입
    private final SellerRepository sellerRepository;
    private final UserAddressRepository userAddressRepository;
    private final UserCardRepository userCardRepository;
    private final FileRepository fileRepository;

    private final S3Service s3Service;
    private final UserQueryService userQueryService;

    /**
     * S3 내부에 저장된 기본 인덱스 이미지 경로입니다.
     * 판매자가 이미지를 등록하지 않았거나 삭제된 경우 이 경로를 사용합니다.
     */
    private static final String DEFAULT_IMAGE = "seller/profile/default_index.png";

    /**
     * 사용자의 현재 역할(Role)을 조회합니다.
     * @param userIdx 사용자 식별자
     * @return 역할 정보 DTO
     */
    @Override
    public ClientRoleDTO getRole(Long userIdx) {
        Users users = userQueryService.findActiveUser(userIdx);
        UsersInformation usersInformation = userQueryService.findActiveUserInfo(userIdx);
        Seller seller = sellerRepository.findByUsersIdxAndDel(userIdx, UserDel.ACTIVE).orElse(null);
        ClientRoleDTO dto = new ClientRoleDTO();
        dto.setRole(users.getRole());
        dto.setUsersIdx(users.getUsersIdx());
        dto.setUserName(usersInformation.getName());
        if (seller != null) {
            dto.setSellerIdx(seller.getSellerIdx());
            dto.setSellerName(seller.getSellerName());
        }
        return dto;
    }

    /**
     * 사용자의 잔액을 변경(충전/결제/환불)합니다.
     * [중요] 동시성 이슈(갱신 분실)를 방지하기 위해 비관적 락을 사용합니다.
     * @param userIdx 사용자 식별자
     * @param balanceReq 변경할 금액과 상태(유형) 정보
     * @return 처리 완료 메시지
     */
    @Override
    @Transactional // 잔액 변경은 중요한 쓰기 작업이므로 트랜잭션 필수
    public String balance(Long userIdx, BalanceReq balanceReq) {
        UsersInformation usersInformation = userQueryService.findActiveUserInfoForUpdate(userIdx);

        // 요청된 상태(Status)에 따라 분기 처리
        switch (balanceReq.getStatus()) {
            case CHARGE, REFUND ->
                // 충전이나 환불인 경우 잔액을 증가시킴
                    usersInformation.chargeBalance(balanceReq.getBalance());
            case PAYMENT ->
                // 결제인 경우 잔액을 차감함
                    usersInformation.pay(balanceReq.getBalance());
            default -> throw new IllegalArgumentException("유효하지 않은 상태입니다.");
        }

        return "잔액 처리가 완료되었습니다.";
    }

    /**
     * 사용자의 기본 배송지 정보를 조회합니다.
     * @param userIdx 사용자 식별자
     * @return 기본 배송지 DTO
     */
    @Override
    public ClientAddressDTO defaultAddress(Long userIdx) {
        UsersInformation usersInformation = userQueryService.findActiveUserInfo(userIdx);
        Long defaultAddressIdx = usersInformation.getDefaultAddress();

        // 기본 배송지가 설정되어 있지 않은 경우
        if (defaultAddressIdx == null) {
            throw new IllegalArgumentException("기본 배송지가 설정되어 있지 않습니다.");
        }

        // 설정된 ID로 주소 엔티티 조회
        UsersAddress usersAddress = userAddressRepository.findFirstByUsersIdxAndAddressIdxAndDelOrderByAddressIdxDesc(userIdx, defaultAddressIdx, UserDel.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("기본 배송지를 찾을 수 없습니다."));

        ClientAddressDTO dto = new ClientAddressDTO();
        dto.setRecipient(usersAddress.getRecipient());
        dto.setAddress(usersAddress.getAddress());
        dto.setAddressDetail(usersAddress.getAddressDetail());
        dto.setPhoneNumber(usersAddress.getPhoneNumber());
        return dto;
    }

    /**
     * 특정 주소 코드로 배송지 정보를 조회합니다.
     * 주문 시 배송지 변경 등의 기능에 사용될 수 있습니다.
     * @param userIdx 사용자 식별자
     * @param addressCode 조회할 주소 코드
     * @return 해당 배송지 DTO
     */
    @Override
    public ClientAddressDTO searchAddress(Long userIdx, String addressCode) {
        UsersAddress usersAddress = userAddressRepository.findFirstByUsersIdxAndAddressCodeAndDelOrderByAddressIdxDesc(userIdx, addressCode, UserDel.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("배송지를 찾을 수 없습니다."));

        ClientAddressDTO dto = new ClientAddressDTO();
        dto.setRecipient(usersAddress.getRecipient());
        dto.setAddress(usersAddress.getAddress());
        dto.setAddressDetail(usersAddress.getAddressDetail());
        dto.setPhoneNumber(usersAddress.getPhoneNumber());
        return dto;
    }

    /**
     * 사용자의 기본 카드 식별자(PK)를 반환합니다.
     * 만료 여부도 함께 검사합니다.
     * @param userIdx 사용자 식별자
     * @return 기본 카드 ID
     */
    @Override
    public Long defaultCard(Long userIdx) {
        UsersInformation usersInformation = userQueryService.findActiveUserInfo(userIdx);
        Long defaultCardIdx = usersInformation.getDefaultCard();

        if (defaultCardIdx == null) {
            throw new IllegalArgumentException("기본 카드가 설정되어 있지 않습니다.");
        }

        UserCard userCard = userCardRepository.findFirstByUsersIdxAndCardIdxAndDelOrderByCardIdxDesc(userIdx, defaultCardIdx, UserDel.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("기본 카드를 찾을 수 없습니다."));

        // 카드의 유효기간(연/월)이 현재 시점보다 이전인지 확인
        if (YearMonth.of(userCard.getExpYear(), userCard.getExpMonth()).isBefore(YearMonth.now())) {
            throw new IllegalStateException("만료된 카드입니다. 카드를 다시 등록해 주세요.");
        }

        return userCard.getCardIdx();
    }

    /**
     * 특정 카드 코드로 카드 식별자(PK)를 반환합니다.
     * 결제 시 특정 카드를 선택했을 때 사용됩니다.
     * @param userIdx 사용자 식별자
     * @param cardCode 조회할 카드 코드
     * @return 해당 카드 ID
     */
    @Override
    public Long searchCard(Long userIdx, String cardCode) {
        UserCard userCard = userCardRepository.findFirstByUsersIdxAndCardCodeAndDelOrderByCardIdxDesc(userIdx, cardCode, UserDel.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("카드를 찾을 수 없습니다."));

        // 만료 여부 확인
        if (YearMonth.of(userCard.getExpYear(), userCard.getExpMonth()).isBefore(YearMonth.now())) {
            throw new IllegalStateException("만료된 카드입니다. 카드를 다시 등록해 주세요.");
        }

        return userCard.getCardIdx();
    }

    /**
     * 판매자 프로필 이미지를 조회하는 비즈니스 로직입니다.
     * 1. File 테이블에서 Ref_Table이 'Seller'이고 Ref_Index가 sellerIdx인 데이터를 찾습니다.
     * 2. 상태값(Del)이 PublicDel.ACTIVE인 최신 데이터를 기준으로 합니다.
     * 3. 데이터가 존재하면 해당 filePath를, 없으면 기본 경로를 선택합니다.
     * 4. 선택된 경로를 S3Service에 전달하여 최종 URL을 생성합니다.
     */
    @Override
    public String getSellerProfileImage(Long sellerIdx) {
        // DB에서 활성화된 파일 정보를 조회하고, 없을 경우 기본 이미지 경로를 결정하는 비즈니스 정책 수행
        String finalImagePath = fileRepository.findByRefTableAndRefIndexAndDel(
                        "Seller",
                        sellerIdx,
                        PublicDel.ACTIVE
                )
                .map(File::getFilePath)
                .orElse(DEFAULT_IMAGE);

        // 결정된 경로를 바탕으로 S3에 URL 발급 요청 (Low-level 통신 위임)
        return s3Service.getPresignedUrl(finalImagePath);
    }

    @Override
    public SellerBankDTO getSellerBankInfo(Long sellerIdx) {
        Seller seller = sellerRepository.findById(sellerIdx)
                .orElseThrow(() -> new IllegalArgumentException("판매자를 찾을 수 없습니다."));
        SellerBankDTO dto = new SellerBankDTO();
        dto.setBankBrand(seller.getBankBrand());
        dto.setBankName(seller.getBankName());
        dto.setBankToken(seller.getBankToken());
        return dto;
    }
}