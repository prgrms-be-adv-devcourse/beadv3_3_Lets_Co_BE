package co.kr.user.service.Impl;

import co.kr.user.dao.UserAddressRepository;
import co.kr.user.model.dto.address.AddressListDTO;
import co.kr.user.model.dto.address.AddressRequestReq;
import co.kr.user.model.entity.UsersAddress;
import co.kr.user.model.entity.UsersInformation;
import co.kr.user.service.AddressService;
import co.kr.user.service.UserQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AddressServiceImpl implements AddressService {
    private final UserAddressRepository userAddressRepository;

    private final UserQueryService userQueryService;

    @Override
    public List<AddressListDTO> addressList(Long userIdx) {
        UsersInformation usersInformation = userQueryService.findActiveUserInfo(userIdx);
        Long defaultAddressIdx = usersInformation.getDefaultAddress();

        List<UsersAddress> usersAddressList = userAddressRepository.findAllByUsersIdxAndDel(userIdx, 0);

        if (usersAddressList.isEmpty()) {
            throw new IllegalArgumentException("주소를 추가해 주세요");
        }

        return usersAddressList.stream()
                .sorted((a, b) -> {
                    int aVal = a.getAddressIdx().equals(defaultAddressIdx) ? 1 : 0;
                    int bVal = b.getAddressIdx().equals(defaultAddressIdx) ? 1 : 0;
                    return Integer.compare(bVal, aVal);
                })
                .map(address -> {
                    AddressListDTO dto = new AddressListDTO();
                    int isDefault = address.getAddressIdx().equals(defaultAddressIdx) ? 1 : 0;
                    dto.setDefaultAddress(isDefault);
                    dto.setAddressCode(address.getAddressCode());
                    dto.setRecipient(address.getRecipient());
                    dto.setAddress(address.getAddress());
                    dto.setAddressDetail(address.getAddressDetail());
                    dto.setPhoneNumber(address.getPhoneNumber());
                    return dto;
                })
                .toList();
    }

    @Override
    @Transactional
    public String addAddress(Long userIdx, AddressRequestReq addressRequestReq) {
        userQueryService.findActiveUser(userIdx);

        UsersAddress usersAddress = UsersAddress.builder()
                .usersIdx(userIdx)
                .addressCode(UUID.randomUUID().toString())
                .recipient(addressRequestReq.getRecipient())
                .address(addressRequestReq.getAddress())
                .addressDetail(addressRequestReq.getAddressDetail())
                .phoneNumber(addressRequestReq.getPhoneNumber())
                .build();
        userAddressRepository.save(usersAddress);

        if (addressRequestReq.isDefaultAddress()) {
            UsersInformation usersInformation = userQueryService.findActiveUserInfo(userIdx);
            usersInformation.updateDefaultAddress(usersAddress.getAddressIdx());
        }

        return "주소가 성공적으로 추가되었습니다.";
    }

    @Override
    @Transactional
    public String updateAddress(Long userIdx, AddressRequestReq addressRequestReq) {
        userQueryService.findActiveUser(userIdx);
        UsersAddress usersAddress = userAddressRepository.findByAddressCodeAndUsersIdxAndDel(addressRequestReq.getAddressCode(), userIdx, 0)
                .orElseThrow(() -> new IllegalArgumentException("요청하신 주소 코드에 해당하는 정보가 없습니다."));

        usersAddress.updateAddress(
                addressRequestReq.getRecipient() != null ? addressRequestReq.getRecipient() : usersAddress.getRecipient(),
                addressRequestReq.getAddress() != null ? addressRequestReq.getAddress() : usersAddress.getAddress(),
                addressRequestReq.getAddressDetail() != null ? addressRequestReq.getAddressDetail() : usersAddress.getAddressDetail(),
                addressRequestReq.getPhoneNumber() != null ? addressRequestReq.getPhoneNumber() : usersAddress.getPhoneNumber()
        );

        if (addressRequestReq.isDefaultAddress()) {
            UsersInformation usersInformation = userQueryService.findActiveUserInfo(userIdx);
            usersInformation.updateDefaultAddress(usersAddress.getAddressIdx());
        }

        return "주소 정보가 수정되었습니다.";
    }

    @Override
    @Transactional
    public String deleteAddress(Long userIdx, String addressCode) {
        userQueryService.findActiveUser(userIdx);
        UsersAddress usersAddress = userAddressRepository.findByAddressCodeAndUsersIdxAndDel(addressCode, userIdx, 0)
                .orElseThrow(() -> new IllegalArgumentException("요청하신 주소 코드(" + addressCode + ")에 해당하는 정보가 없습니다."));

        usersAddress.deleteAddress();
        return "주소가 삭제되었습니다.";
    }
}