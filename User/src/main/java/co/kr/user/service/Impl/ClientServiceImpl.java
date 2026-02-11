package co.kr.user.service.Impl;

import co.kr.user.dao.*;
import co.kr.user.model.dto.client.BalanceReq;
import co.kr.user.model.dto.client.ClientAddressDTO;
import co.kr.user.model.dto.client.ClientRoleDTO;
import co.kr.user.model.entity.*;
import co.kr.user.model.vo.UserDel;
import co.kr.user.service.ClientService;
import co.kr.user.service.UserQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClientServiceImpl implements ClientService {
    private final SellerRepository sellerRepository;
    private final UserAddressRepository userAddressRepository;
    private final UserCardRepository userCardRepository;

    private final UserQueryService userQueryService;

    @Override
    public ClientRoleDTO getRole(Long userIdx) {
        Users users = userQueryService.findActiveUser(userIdx);
        ClientRoleDTO dto = new ClientRoleDTO();
        dto.setRole(users.getRole());
        return dto;
    }

    @Override
    @Transactional
    public String balance(Long userIdx, BalanceReq balanceReq) {
        UsersInformation usersInformation = userQueryService.findActiveUserInfo(userIdx);

        switch (balanceReq.getStatus()) {
            case CHARGE, REFUND -> usersInformation.chargeBalance(balanceReq.getBalance());
            case PAYMENT -> usersInformation.pay(balanceReq.getBalance());
            default -> throw new IllegalArgumentException("유효하지 않은 상태입니다.");
        }

        return "잔액 처리가 완료되었습니다.";
    }

    @Override
    public ClientAddressDTO defaultAddress(Long userIdx) {
        UsersInformation usersInformation = userQueryService.findActiveUserInfo(userIdx);
        Long defaultAddressIdx = usersInformation.getDefaultAddress();

        if (defaultAddressIdx == null) {
            throw new IllegalArgumentException("기본 배송지가 설정되어 있지 않습니다.");
        }

        UsersAddress usersAddress = userAddressRepository.findFirstByUsersIdxAndAddressIdxAndDelOrderByAddressIdxDesc(userIdx, defaultAddressIdx, UserDel.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("기본 배송지를 찾을 수 없습니다."));

        ClientAddressDTO dto = new ClientAddressDTO();
        dto.setRecipient(usersAddress.getRecipient());
        dto.setAddress(usersAddress.getAddress());
        dto.setAddressDetail(usersAddress.getAddressDetail());
        dto.setPhoneNumber(usersAddress.getPhoneNumber());
        return dto;
    }

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

    @Override
    public Long defaultCard(Long userIdx) {
        UsersInformation usersInformation = userQueryService.findActiveUserInfo(userIdx);
        Long defaultCardIdx = usersInformation.getDefaultCard();

        if (defaultCardIdx == null) {
            throw new IllegalArgumentException("기본 카드가 설정되어 있지 않습니다.");
        }

        UserCard userCard = userCardRepository.findFirstByUsersIdxAndCardIdxAndDelOrderByCardIdxDesc(userIdx, defaultCardIdx, UserDel.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("기본 카드를 찾을 수 없습니다."));

        if (YearMonth.of(userCard.getExpYear(), userCard.getExpMonth()).isBefore(YearMonth.now())) {
            throw new IllegalStateException("만료된 카드입니다. 카드를 다시 등록해 주세요.");
        }

        return userCard.getCardIdx();
    }

    @Override
    public Long searchCard(Long userIdx, String cardCode) {
        UserCard userCard = userCardRepository.findFirstByUsersIdxAndCardCodeAndDelOrderByCardIdxDesc(userIdx, cardCode, UserDel.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("카드를 찾을 수 없습니다."));

        if (YearMonth.of(userCard.getExpYear(), userCard.getExpMonth()).isBefore(YearMonth.now())) {
            throw new IllegalStateException("만료된 카드입니다. 카드를 다시 등록해 주세요.");
        }

        return userCard.getCardIdx();
    }
}