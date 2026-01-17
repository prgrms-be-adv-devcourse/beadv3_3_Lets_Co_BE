package co.kr.user.service;

import co.kr.user.DAO.UserAddressRepository;
import co.kr.user.DAO.UserRepository;
import co.kr.user.model.DTO.address.AddressListDTO;
import co.kr.user.model.DTO.address.AddressRequestReq;
import co.kr.user.model.entity.Users;
import co.kr.user.model.entity.UsersAddress;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AddressService implements AddressServiceImpl{

    private final UserRepository userRepository;
    private final UserAddressRepository userAddressRepository;

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

        UsersAddress usersAddress = userAddressRepository.findById(users.getUsersIdx())
                .orElseThrow(() -> new IllegalArgumentException("주소를 추가해 주세요"));

        // 1. Create the DTO
        AddressListDTO addressListDTO = new AddressListDTO();

        // 2. Map fields from Entity (UsersAddress) to DTO (AddressListDTO)
        addressListDTO.setAddressCode(usersAddress.getAddressCode());
        addressListDTO.setDefaultAddress(usersAddress.getDefaultAddress());
        addressListDTO.setRecipient(usersAddress.getRecipient());
        addressListDTO.setAddress(usersAddress.getAddress());
        addressListDTO.setAddressDetail(usersAddress.getAddressDetail());
        addressListDTO.setPhoneNumber(usersAddress.getPhoneNumber());

        return List.of(addressListDTO);
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

        // Entity 생성 (Builder 사용)
        UsersAddress usersAddress = UsersAddress.builder()
                        .usersIdx(users.getUsersIdx())
                        .addressCode(UUID.randomUUID().toString())
                        .defaultAddress(addressRequestReq.getDefaultAddress())
                        .recipient(addressRequestReq.getRecipient())
                        .address(addressRequestReq.getAddress())
                        .addressDetail(addressRequestReq.getAddressDetail())
                        .phoneNumber(addressRequestReq.getPhoneNumber())
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

        // UUID로 주소 찾기
        UsersAddress usersAddress = userAddressRepository.findByaddressCode(addressRequestReq.getAddressCode())
                .orElseThrow(() -> new IllegalArgumentException("해당 주소 정보를 찾을 수 없습니다."));

        // 권한 체크 (내 주소가 맞는지)
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
        if (addressRequestReq.getRecipient().equals(usersAddress.getRecipient())) {
            dto.setRecipient(usersAddress.getRecipient());
        }
        if (addressRequestReq.getAddress().equals(usersAddress.getAddress())) {
            dto.setAddress(usersAddress.getAddress());
        }
        if (addressRequestReq.getAddressDetail().equals(usersAddress.getAddressDetail())) {
            dto.setAddressDetail(usersAddress.getAddressDetail());
        }
        if (addressRequestReq.getPhoneNumber().equals(usersAddress.getPhoneNumber())) {
            dto.setPhoneNumber(usersAddress.getPhoneNumber());
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

        // UUID로 주소 찾기
        UsersAddress usersAddress = userAddressRepository.findByaddressCode(addressCode)
                .orElseThrow(() -> new IllegalArgumentException("해당 주소 정보를 찾을 수 없습니다."));

        // 권한 체크 (내 주소가 맞는지)
        if (!usersAddress.getUsersIdx().equals(userIdx)) {
            throw new IllegalStateException("본인의 주소만 수정할 수 있습니다.");
        }

        usersAddress.del();

        return "주소가 삭제되었습니다.";
    }
}
