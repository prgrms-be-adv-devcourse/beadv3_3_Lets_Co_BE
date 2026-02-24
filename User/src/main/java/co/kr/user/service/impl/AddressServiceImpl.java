package co.kr.user.service.impl;

import co.kr.user.dao.UserAddressRepository;
import co.kr.user.model.dto.address.AddressListDTO;
import co.kr.user.model.dto.address.AddressRequestReq;
import co.kr.user.model.entity.UsersAddress;
import co.kr.user.model.entity.UsersInformation;
import co.kr.user.model.vo.UserDel;
import co.kr.user.service.AddressService;
import co.kr.user.service.UserQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 사용자의 주소지(배송지) 정보를 관리하는 서비스 클래스입니다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AddressServiceImpl implements AddressService {
    private final UserAddressRepository userAddressRepository;
    private final UserQueryService userQueryService;

    /**
     * 사용자의 모든 유효한 배송지 목록을 조회합니다.
     * 기본 배송지로 설정된 주소가 목록의 가장 앞에 오도록 정렬하여 반환합니다.
     *
     * @param userIdx 조회할 사용자의 고유 식별자
     * @return 정렬된 배송지 정보 리스트
     * @throws IllegalArgumentException 주소 정보가 전혀 없을 경우 발생
     */
    @Override
    public List<AddressListDTO> addressList(Long userIdx) {
        // 사용자의 기본 배송지 정보를 파악하기 위해 상세 정보를 먼저 조회합니다.
        UsersInformation usersInformation = userQueryService.findActiveUserInfo(userIdx);
        Long defaultAddressIdx = usersInformation.getDefaultAddress();

        // 삭제되지 않은 정상 상태의 배송지들만 가져옵니다.
        List<UsersAddress> usersAddressList = userAddressRepository.findAllByUsersIdxAndDel(userIdx, UserDel.ACTIVE);

        if (usersAddressList.isEmpty()) {
            throw new IllegalArgumentException("주소를 추가해 주세요");
        }

        return usersAddressList.stream()
                .sorted((a, b) -> {
                    // 기본 배송지 식별자와 일치하는 항목에 가중치를 주어 상단에 노출시킵니다.
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

    /**
     * 새로운 주소 정보를 추가합니다.
     * 요청 옵션에 따라 해당 주소를 즉시 기본 배송지로 설정할 수 있습니다.
     *
     * @param userIdx 주소를 추가할 사용자 식별자
     * @param addressRequestReq 추가할 주소지 상세 데이터 및 기본 배송지 설정 여부
     * @return 성공 메시지
     */
    @Override
    @Transactional
    public String addAddress(Long userIdx, AddressRequestReq addressRequestReq) {
        userQueryService.findActiveUser(userIdx);

        UsersAddress usersAddress = UsersAddress.builder()
                .usersIdx(userIdx)
                .recipient(addressRequestReq.getRecipient())
                .address(addressRequestReq.getAddress())
                .addressDetail(addressRequestReq.getAddressDetail())
                .phoneNumber(addressRequestReq.getPhoneNumber())
                .build();
        userAddressRepository.save(usersAddress);

        // 기본 배송지로 설정 옵션이 켜져 있다면 회원 상세 정보의 default_address 컬럼을 갱신합니다.
        if (addressRequestReq.isDefaultAddress()) {
            UsersInformation usersInformation = userQueryService.findActiveUserInfo(userIdx);
            usersInformation.updateDefaultAddress(usersAddress.getAddressIdx());
        }

        return "주소가 성공적으로 추가되었습니다.";
    }

    /**
     * 특정 주소 코드의 정보를 수정합니다.
     *
     * @param userIdx 사용자 식별자
     * @param req 수정할 주소 필드 데이터 및 기본 배송지 설정 여부
     * @return 성공 메시지
     */
    @Override
    @Transactional
    public String updateAddress(Long userIdx, AddressRequestReq req) {
        userQueryService.findActiveUser(userIdx);

        UsersAddress usersAddress = userAddressRepository.findByAddressCodeAndUsersIdxAndDel(req.getAddressCode(), userIdx, UserDel.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("요청하신 주소 코드에 해당하는 정보가 없습니다."));

        // 엔티티 내부의 수정 로직을 통해 필드 정보를 갱신합니다.
        usersAddress.updateAddress(req.getRecipient(), req.getAddress(), req.getAddressDetail(), req.getPhoneNumber());

        if (req.isDefaultAddress()) {
            UsersInformation usersInformation = userQueryService.findActiveUserInfo(userIdx);
            usersInformation.updateDefaultAddress(usersAddress.getAddressIdx());
        }

        return "주소 정보가 수정되었습니다.";
    }

    /**
     * 특정 주소지를 논리적으로 삭제(Soft Delete)합니다.
     *
     * @param userIdx 사용자 식별자
     * @param addressCode 삭제할 주소의 고유 코드
     * @return 성공 메시지
     */
    @Override
    @Transactional
    public String deleteAddress(Long userIdx, String addressCode) {
        userQueryService.findActiveUser(userIdx);

        UsersAddress usersAddress = userAddressRepository.findByAddressCodeAndUsersIdxAndDel(addressCode, userIdx, UserDel.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("요청하신 주소 코드에 해당하는 정보가 없습니다."));

        // DB에서 실제로 삭제하지 않고 삭제 플래그만 변경합니다.
        usersAddress.deleteAddress();
        return "주소가 삭제되었습니다.";
    }
}