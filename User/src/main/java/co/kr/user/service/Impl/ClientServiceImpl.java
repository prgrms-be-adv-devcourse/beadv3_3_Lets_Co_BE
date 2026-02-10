package co.kr.user.service.Impl;

import co.kr.user.dao.*;
import co.kr.user.model.dto.client.BalanceReq;
import co.kr.user.model.dto.client.ClientAddressDTO;
import co.kr.user.model.dto.client.ClientRoleDTO;
import co.kr.user.model.entity.*;
import co.kr.user.model.vo.UsersRole;
import co.kr.user.service.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.YearMonth;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClientServiceImpl implements ClientService {
    private final UserRepository userRepository;
    private final SellerRepository sellerRepository;
    private final UserInformationRepository userInformationRepository;
    private final UserAddressRepository userAddressRepository;
    private final UserCardRepository userCardRepository;

    private final UserQueryServiceImpl userQueryServiceImpl;

    @Override
    public ClientRoleDTO getRole(Long userIdx) {
        Users users = userRepository.findByUsersIdxAndDel(userIdx, 0)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        ClientRoleDTO clientRoleDTO = new ClientRoleDTO();
        clientRoleDTO.setRole(users.getRole());
        if (users.getRole().equals(UsersRole.SELLER)) {
            Seller seller = sellerRepository.findByUsersIdxAndDel(users.getUsersIdx(), 0)
                    .orElseThrow(() -> new IllegalArgumentException("판매자 정보를 찾을 수 없습니다."));
            clientRoleDTO.setIdx(seller.getSellerIdx());
        }
        else {
            clientRoleDTO.setIdx(users.getUsersIdx());
        }

        return clientRoleDTO;
    }

    @Override
    @Transactional
    public String balance(Long userIdx, BalanceReq balanceReq) {
        Users users = userQueryServiceImpl.findActiveUser(userIdx);
        UsersInformation usersInformation = userInformationRepository.findByUsersIdxAndDel(users.getUsersIdx(), 0)
                .orElseThrow(() -> new IllegalArgumentException("사용자 상세 정보를 찾을 수 없습니다."));

        BigDecimal currentBalance = usersInformation.getBalance();
        BigDecimal requestAmount = balanceReq.getBalance();

        switch (balanceReq.getStatus()) {
            case CHARGE, REFUND:
                usersInformation.updateBalance(currentBalance.add(requestAmount));
                break;
            case PAYMENT:
                if (currentBalance.compareTo(requestAmount) < 0) {
                    throw new IllegalStateException("잔액이 부족합니다.");
                }
                usersInformation.updateBalance(currentBalance.subtract(requestAmount));
                break;
            default:
                throw new IllegalArgumentException("유효하지 않은 주문 상태입니다.");
        }

        return "잔액 처리가 완료되었습니다.";
    }

    @Override
    public ClientAddressDTO defaultAddress(Long userIdx) {
        Users users = userQueryServiceImpl.findActiveUser(userIdx);
        UsersInformation usersInformation = userInformationRepository.findByUsersIdxAndDel(users.getUsersIdx(), 0)
                .orElseThrow(() -> new IllegalArgumentException("사용자 상세 정보를 찾을 수 없습니다."));



        UsersAddress usersAddress = userAddressRepository.findFirstByUsersIdxAndAddressIdxAndDelOrderByAddressIdxDesc(users.getUsersIdx(),  usersInformation.getDefaultAddress(),0)
                .orElseThrow(() -> new IllegalArgumentException("사용자(ID: " + users.getId() + ")의 기본 배송지 정보가 존재하지 않습니다."));

        ClientAddressDTO clientAddressDTO = new ClientAddressDTO();
        clientAddressDTO.setRecipient(usersAddress.getRecipient());
        clientAddressDTO.setAddress(usersAddress.getAddress());
        clientAddressDTO.setAddressDetail(usersAddress.getAddressDetail());
        clientAddressDTO.setPhoneNumber(usersAddress.getPhoneNumber());

        return clientAddressDTO;
    }

    @Override
    public ClientAddressDTO searchAddress(Long userIdx, String addressCode) {
        Users users = userQueryServiceImpl.findActiveUser(userIdx);
        UsersAddress usersAddress = userAddressRepository.findFirstByUsersIdxAndAddressCodeAndDelOrderByAddressIdxDesc(users.getUsersIdx(), addressCode, 0)
                .orElseThrow(() -> new IllegalArgumentException("해당 주소 정보가 없습니다."));

        ClientAddressDTO clientAddressDTO = new ClientAddressDTO();
        clientAddressDTO.setRecipient(usersAddress.getRecipient());
        clientAddressDTO.setAddress(usersAddress.getAddress());
        clientAddressDTO.setAddressDetail(usersAddress.getAddressDetail());
        clientAddressDTO.setPhoneNumber(usersAddress.getPhoneNumber());

        return clientAddressDTO;
    }

    @Override
    public Long defaultCard(Long userIdx) {
        Users users = userQueryServiceImpl.findActiveUser(userIdx);
        UsersInformation usersInformation = userInformationRepository.findByUsersIdxAndDel(users.getUsersIdx(), 0)
                .orElseThrow(() -> new IllegalArgumentException("사용자 상세 정보를 찾을 수 없습니다."));

        UserCard userCard = userCardRepository.findFirstByUsersIdxAndCardIdxAndDelOrderByCardIdxDesc(users.getUsersIdx(), usersInformation.getDefaultCard(), 0)
                .orElseThrow(() -> new IllegalArgumentException("Default 카드가 없습니다."));

        YearMonth cardExpiry = YearMonth.of(userCard.getExpYear(), userCard.getExpMonth());
        YearMonth currentMonth = YearMonth.now();

        if (cardExpiry.isBefore(currentMonth)) {
            throw new IllegalStateException("만료된 카드입니다. 카드를 다시 등록해 주세요.");
        }

        return userCard.getCardIdx();
    }

    @Override
    public Long searchCard(Long userIdx, String cardCode) {
        Users users = userQueryServiceImpl.findActiveUser(userIdx);
        UserCard userCard = userCardRepository.findFirstByUsersIdxAndCardCodeAndDelOrderByCardIdxDesc(users.getUsersIdx(), cardCode, 0)
                .orElseThrow(() -> new IllegalArgumentException("해당 카드 정보가 없습니다."));

        YearMonth cardExpiry = YearMonth.of(userCard.getExpYear(), userCard.getExpMonth());
        YearMonth currentMonth = YearMonth.now();

        if (cardExpiry.isBefore(currentMonth)) {
            throw new IllegalStateException("만료된 카드입니다. 카드를 다시 등록해 주세요.");
        }

        return userCard.getCardIdx();
    }
}