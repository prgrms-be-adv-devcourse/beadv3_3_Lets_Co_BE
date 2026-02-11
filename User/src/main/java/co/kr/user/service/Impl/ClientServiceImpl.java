package co.kr.user.service.Impl;

import co.kr.user.dao.*;
import co.kr.user.model.dto.client.BalanceReq;
import co.kr.user.model.dto.client.ClientAddressDTO;
import co.kr.user.model.dto.client.ClientRoleDTO;
import co.kr.user.model.entity.*;
import co.kr.user.model.vo.UsersRole;
import co.kr.user.service.ClientService;
import co.kr.user.service.UserQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.YearMonth;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClientServiceImpl implements ClientService {private final UserRepository userRepository;
    private final SellerRepository sellerRepository;
    private final UserAddressRepository userAddressRepository;
    private final UserCardRepository userCardRepository;

    private final UserQueryService userQueryService;

    @Override
    public ClientRoleDTO getRole(Long userIdx) {
        Users users = userQueryService.findActiveUser(userIdx);

        ClientRoleDTO clientRoleDTO = new ClientRoleDTO();
        clientRoleDTO.setRole(users.getRole());
        if (users.getRole().equals(UsersRole.SELLER)) {
            Seller seller = sellerRepository.findByUsersIdxAndDel(userIdx, 0)
                    .orElseThrow(() -> new IllegalArgumentException("판매자 정보를 찾을 수 없습니다."));
            clientRoleDTO.setIdx(seller.getSellerIdx());
        } else {
            clientRoleDTO.setIdx(userIdx);
        }
        return clientRoleDTO;
    }

    @Override
    @Transactional
    public String balance(Long userIdx, BalanceReq balanceReq) {
        UsersInformation usersInformation = userQueryService.findActiveUserInfo(userIdx);

        BigDecimal currentBalance = usersInformation.getBalance();
        BigDecimal requestAmount = balanceReq.getBalance();

        switch (balanceReq.getStatus()) {
            case CHARGE, REFUND -> usersInformation.updateBalance(currentBalance.add(requestAmount));
            case PAYMENT -> {
                if (currentBalance.compareTo(requestAmount) < 0) {
                    throw new IllegalStateException("잔액이 부족합니다.");
                }
                usersInformation.updateBalance(currentBalance.subtract(requestAmount));
            }
            default -> throw new IllegalArgumentException("유효하지 않은 주문 상태입니다.");
        }
        return "잔액 처리가 완료되었습니다.";
    }

    @Override
    public ClientAddressDTO defaultAddress(Long userIdx) {
        UsersInformation info = userQueryService.findActiveUserInfo(userIdx);

        UsersAddress usersAddress = userAddressRepository.findFirstByUsersIdxAndAddressIdxAndDelOrderByAddressIdxDesc(userIdx, info.getDefaultAddress(), 0)
                .orElseThrow(() -> new IllegalArgumentException("기본 배송지 정보가 존재하지 않습니다."));

        ClientAddressDTO dto = new ClientAddressDTO();
        dto.setRecipient(usersAddress.getRecipient());
        dto.setAddress(usersAddress.getAddress());
        dto.setAddressDetail(usersAddress.getAddressDetail());
        dto.setPhoneNumber(usersAddress.getPhoneNumber());
        return dto;
    }

    @Override
    public ClientAddressDTO searchAddress(Long userIdx, String addressCode) {
        userQueryService.findActiveUser(userIdx);
        UsersAddress usersAddress = userAddressRepository.findFirstByUsersIdxAndAddressCodeAndDelOrderByAddressIdxDesc(userIdx, addressCode, 0)
                .orElseThrow(() -> new IllegalArgumentException("해당 주소 정보가 없습니다."));

        ClientAddressDTO dto = new ClientAddressDTO();
        dto.setRecipient(usersAddress.getRecipient());
        dto.setAddress(usersAddress.getAddress());
        dto.setAddressDetail(usersAddress.getAddressDetail());
        dto.setPhoneNumber(usersAddress.getPhoneNumber());
        return dto;
    }

    @Override
    public Long defaultCard(Long userIdx) {
        UsersInformation info = userQueryService.findActiveUserInfo(userIdx);

        UserCard userCard = userCardRepository.findFirstByUsersIdxAndCardIdxAndDelOrderByCardIdxDesc(userIdx, info.getDefaultCard(), 0)
                .orElseThrow(() -> new IllegalArgumentException("Default 카드가 없습니다."));

        if (YearMonth.of(userCard.getExpYear(), userCard.getExpMonth()).isBefore(YearMonth.now())) {
            throw new IllegalStateException("만료된 카드입니다. 카드를 다시 등록해 주세요.");
        }
        return userCard.getCardIdx();
    }

    @Override
    public Long searchCard(Long userIdx, String cardCode) {
        userQueryService.findActiveUser(userIdx);
        UserCard userCard = userCardRepository.findFirstByUsersIdxAndCardCodeAndDelOrderByCardIdxDesc(userIdx, cardCode, 0)
                .orElseThrow(() -> new IllegalArgumentException("해당 카드 정보가 없습니다."));

        if (YearMonth.of(userCard.getExpYear(), userCard.getExpMonth()).isBefore(YearMonth.now())) {
            throw new IllegalStateException("만료된 카드입니다. 카드를 다시 등록해 주세요.");
        }
        return userCard.getCardIdx();
    }
}