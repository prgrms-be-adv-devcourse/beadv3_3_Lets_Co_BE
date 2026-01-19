package co.kr.user.service;

import co.kr.user.DAO.UserAddressRepository;
import co.kr.user.DAO.UserRepository;
import co.kr.user.model.DTO.address.AddressListDTO;
import co.kr.user.model.DTO.address.AddressRequestReq;
import co.kr.user.model.entity.UserCard;
import co.kr.user.model.entity.Users;
import co.kr.user.model.entity.UsersAddress;
import co.kr.user.util.AESUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AddressService implements AddressServiceImpl{
    private final UserRepository userRepository;
    private final UserAddressRepository userAddressRepository;

    private final AESUtil aesUtil;

    @Override
    public Long defaultAddress(Long userIdx) {
        Users users = userRepository.findById(userIdx)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이디입니다."));


        if (users.getDel() == 1) {
            throw new IllegalStateException("탈퇴한 회원입니다.");
        }
        else if (users.getDel() == 2) {
            throw new IllegalStateException("인증을 먼저 시도해 주세요.");
        }

        UsersAddress usersAddress = userAddressRepository.findFirstByUsersIdxAndDefaultAddressAndDelOrderByAddressIdxDesc(users.getUsersIdx(), 1, 0)
                .orElseThrow(() -> new IllegalArgumentException("Default 주소가 없습니다."));

        return usersAddress.getAddressIdx();
    }

    @Override
    public Long searchAddress(Long userIdx, String addressCode) {
        Users users = userRepository.findById(userIdx)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이디입니다."));

        if (users.getDel() == 1) {
            throw new IllegalStateException("탈퇴한 회원입니다.");
        }
        else if (users.getDel() == 2) {
            throw new IllegalStateException("인증을 먼저 시도해 주세요.");
        }

        UsersAddress usersAddress = userAddressRepository.findFirstByUsersIdxAndAddressCodeAndDelOrderByAddressIdxDesc(users.getUsersIdx(), addressCode, 0)
                .orElseThrow(() -> new IllegalArgumentException("해당 카드 정보가 없습니다."));

        return usersAddress.getAddressIdx();
    }

    @Override
    public List<AddressListDTO> addressList(Long userIdx) {
        Users users = userRepository.findById(userIdx)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이디입니다."));

        if (users.getDel() == 1) {
            throw new IllegalStateException("탈퇴한 회원입니다.");
        }
        else if (users.getDel() == 2) {
            throw new IllegalStateException("인증을 먼저 시도해 주세요.");
        }

        List<UsersAddress> usersAddressList = userAddressRepository.findAllByUsersIdxAndDel(users.getUsersIdx(), 0);

        if (usersAddressList.isEmpty()) {
            throw new IllegalArgumentException("주소를 추가해 주세요");
        }

        return usersAddressList.stream()
                .map(address -> {
                    AddressListDTO dto = new AddressListDTO();
                    dto.setAddressCode(address.getAddressCode());
                    dto.setDefaultAddress(address.getDefaultAddress());
                    dto.setRecipient(aesUtil.decrypt(address.getRecipient()));
                    dto.setAddress(aesUtil.decrypt(address.getAddress()));
                    dto.setAddressDetail(aesUtil.decrypt(address.getAddressDetail()));
                    dto.setPhoneNumber(aesUtil.decrypt(address.getPhoneNumber()));
                    return dto;
                })
                .toList();
    }

    @Override
    @Transactional
    public String addAddress(Long userIdx, AddressRequestReq addressRequestReq) {
        Users users = userRepository.findById(userIdx)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이디입니다."));

        if (users.getDel() == 1) {
            throw new IllegalStateException("탈퇴한 회원입니다.");
        }
        else if (users.getDel() == 2) {
            throw new IllegalStateException("인증을 먼저 시도해 주세요.");
        }

        UsersAddress usersAddress = UsersAddress.builder()
                        .usersIdx(users.getUsersIdx())
                        .addressCode(UUID.randomUUID().toString())
                        .defaultAddress(addressRequestReq.getDefaultAddress())
                        .recipient(aesUtil.encrypt(addressRequestReq.getRecipient()))
                        .address(aesUtil.encrypt(addressRequestReq.getAddress()))
                        .addressDetail(aesUtil.encrypt(addressRequestReq.getAddressDetail()))
                        .phoneNumber(aesUtil.encrypt(addressRequestReq.getPhoneNumber()))
                        .build();
        userAddressRepository.save(usersAddress);

        return "주소가 성공적으로 추가되었습니다.";
    }

    @Override
    @Transactional
    public String updateAddress(Long userIdx, AddressRequestReq addressRequestReq) {
        Users users = userRepository.findById(userIdx)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이디입니다."));

        if (users.getDel() == 1) {
            throw new IllegalStateException("탈퇴한 회원입니다.");
        }
        else if (users.getDel() == 2) {
            throw new IllegalStateException("인증을 먼저 시도해 주세요.");
        }

        UsersAddress usersAddress = userAddressRepository.findByAddressCode(addressRequestReq.getAddressCode())
                .orElseThrow(() -> new IllegalArgumentException("해당 주소 정보를 찾을 수 없습니다."));

        if (!usersAddress.getUsersIdx().equals(userIdx)) {
            throw new IllegalStateException("본인의 주소만 수정할 수 있습니다.");
        }

        AddressRequestReq dto = new AddressRequestReq();
        dto.setAddressCode(usersAddress.getAddressCode());
        if (addressRequestReq.getDefaultAddress() == 1) {
            dto.setDefaultAddress(1);
        } else {
            dto.setDefaultAddress(0);
        }
        if (addressRequestReq.getRecipient() == null || addressRequestReq.getRecipient().isEmpty()) {
            dto.setRecipient(aesUtil.encrypt(usersAddress.getRecipient()));
        }
        else {
            dto.setRecipient(aesUtil.encrypt(addressRequestReq.getRecipient()));
        }
        if (addressRequestReq.getAddress() == null || addressRequestReq.getAddress().isEmpty()) {
            dto.setAddress(aesUtil.encrypt(usersAddress.getAddress()));
        }
        else {
            dto.setAddress(aesUtil.encrypt(addressRequestReq.getAddress()));
        }
        if (addressRequestReq.getAddressDetail() == null || addressRequestReq.getAddressDetail().isEmpty()) {
            dto.setAddressDetail(aesUtil.encrypt(usersAddress.getAddressDetail()));
        }
        else {
            dto.setAddressDetail(aesUtil.encrypt(addressRequestReq.getAddressDetail()));
        }
        if (addressRequestReq.getPhoneNumber() == null || addressRequestReq.getPhoneNumber().isEmpty()) {
            dto.setPhoneNumber(aesUtil.encrypt(usersAddress.getPhoneNumber()));
        }
        else {
            dto.setPhoneNumber(aesUtil.encrypt(addressRequestReq.getPhoneNumber()));
        }


        usersAddress.updateAddress(
                dto.getDefaultAddress(),
                dto.getRecipient(),
                dto.getAddress(),
                dto.getAddressDetail(),
                dto.getPhoneNumber()
        );

        return "주소 정보가 수정되었습니다.";
    }

    @Override
    @Transactional
    public String deleteAddress(Long userIdx, String addressCode) {
        Users users = userRepository.findById(userIdx)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이디입니다."));

        if (users.getDel() == 1) {
            throw new IllegalStateException("탈퇴한 회원입니다.");
        }
        else if (users.getDel() == 2) {
            throw new IllegalStateException("인증을 먼저 시도해 주세요.");
        }

        UsersAddress usersAddress = userAddressRepository.findByAddressCode(addressCode)
                .orElseThrow(() -> new IllegalArgumentException("해당 주소 정보를 찾을 수 없습니다."));

        if (!usersAddress.getUsersIdx().equals(userIdx)) {
            throw new IllegalStateException("본인의 주소만 수정할 수 있습니다.");
        }

        usersAddress.del();

        return "주소가 삭제되었습니다.";
    }
}