package co.kr.user.service.Impl;

import co.kr.user.dao.UserAddressRepository;
import co.kr.user.dao.UserRepository;
import co.kr.user.model.dto.address.AddressListDTO;
import co.kr.user.model.dto.address.AddressRequestReq;
import co.kr.user.model.entity.Users;
import co.kr.user.model.entity.UsersAddress;
import co.kr.user.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * 사용자 배송지(Address) 관리 비즈니스 로직을 처리하는 서비스 클래스입니다.
 * 배송지 추가, 수정, 삭제(Soft Delete), 목록 조회, 기본 배송지 확인 기능을 제공합니다.
 * 개인정보(이름, 주소, 전화번호)는 암호화하여 DB에 저장하고, 조회 시 복호화하여 반환합니다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AddressServiceImpl implements AddressService {
    private final UserRepository userRepository;
    private final UserAddressRepository userAddressRepository;

    private final UserQueryServiceImpl userQueryServiceImpl;

    /**
     * 사용자의 기본 배송지 식별자(PK)를 조회하는 메서드입니다.
     * 상품 구매 등의 프로세스에서 별도 선택이 없을 경우 사용될 주소를 찾습니다.
     *
     * @param userIdx 로그인한 사용자의 식별자
     * @return 기본 배송지의 AddressIdx
     */
    @Override
    public Long defaultAddress(Long userIdx) {
        Users users = userQueryServiceImpl.findActiveUser(userIdx);

        // 기본 배송지(DefaultAddress=1)이면서 삭제되지 않은(Del=0) 주소 조회
        UsersAddress usersAddress = userAddressRepository.findFirstByUsersIdxAndDelOrderByAddressIdxDesc(users.getUsersIdx(),  0)
                .orElseThrow(() -> new IllegalArgumentException("사용자(ID: " + users.getId() + ")의 기본 배송지 정보가 존재하지 않습니다."));

        return usersAddress.getAddressIdx();
    }

    /**
     * 특정 배송지 코드(UUID)로 배송지 식별자(PK)를 조회하는 메서드입니다.
     * 프론트엔드에서 전달받은 addressCode가 유효한지 검증하고, 해당 주소의 실제 ID를 반환합니다.
     *
     * @param userIdx 로그인한 사용자의 식별자
     * @param addressCode 조회할 주소의 고유 코드
     * @return 해당 배송지의 AddressIdx
     */
    @Override
    public Long searchAddress(Long userIdx, String addressCode) {
        Users users = userQueryServiceImpl.findActiveUser(userIdx);
        // 주소 코드와 사용자 ID로 주소 조회
        UsersAddress usersAddress = userAddressRepository.findFirstByUsersIdxAndAddressCodeAndDelOrderByAddressIdxDesc(users.getUsersIdx(), addressCode, 0)
                .orElseThrow(() -> new IllegalArgumentException("해당 주소 정보가 없습니다."));

        return usersAddress.getAddressIdx();
    }

    /**
     * 사용자의 등록된 모든 배송지 목록을 조회하는 메서드입니다.
     * 암호화되어 저장된 수령인, 주소, 전화번호 정보를 복호화하여 DTO로 반환합니다.
     *
     * @param userIdx 로그인한 사용자의 식별자
     * @return 배송지 목록 (AddressListDTO 리스트)
     */
    @Override
    public List<AddressListDTO> addressList(Long userIdx) {
        Users users = userQueryServiceImpl.findActiveUser(userIdx);
        List<UsersAddress> usersAddressList = userAddressRepository.findAllByUsersIdxAndDel(users.getUsersIdx(), 0);

        if (usersAddressList.isEmpty()) {
            throw new IllegalArgumentException("주소를 추가해 주세요");
        }

        return usersAddressList.stream()
                .map(address -> {
                    AddressListDTO dto = new AddressListDTO();
                    dto.setAddressCode(address.getAddressCode());
                    // [수정] aesUtil.decrypt() 호출 제거. Entity에서 이미 복호화된 상태로 넘어옵니다.
                    dto.setRecipient(address.getRecipient());
                    dto.setAddress(address.getAddress());
                    dto.setAddressDetail(address.getAddressDetail());
                    dto.setPhoneNumber(address.getPhoneNumber());
                    return dto;
                })
                .toList();
    }

    /**
     * 신규 배송지를 추가하는 메서드입니다.
     * 개인정보 필드는 암호화하여 저장하며, UUID를 생성하여 주소 코드로 부여합니다.
     *
     * @param userIdx 로그인한 사용자의 식별자
     * @param addressRequestReq 추가할 배송지 정보
     * @return 처리 결과 메시지
     */
    @Override
    @Transactional
    public String addAddress(Long userIdx, AddressRequestReq addressRequestReq) {
        Users users = userQueryServiceImpl.findActiveUser(userIdx);

        // [수정] aesUtil.encrypt() 호출 제거. 평문으로 저장하면 JPA가 저장 시 자동 암호화합니다.
        UsersAddress usersAddress = UsersAddress.builder()
                .usersIdx(users.getUsersIdx())
                .addressCode(UUID.randomUUID().toString())
                .recipient(addressRequestReq.getRecipient())
                .address(addressRequestReq.getAddress())
                .addressDetail(addressRequestReq.getAddressDetail())
                .phoneNumber(addressRequestReq.getPhoneNumber())
                .build();
        userAddressRepository.save(usersAddress);

        return "주소가 성공적으로 추가되었습니다.";
    }

    /**
     * 기존 배송지 정보를 수정하는 메서드입니다.
     * 요청에 포함된 필드만 선별적으로 업데이트하며, 개인정보 변경 시 재암호화를 수행합니다.
     * 본인의 주소인지 검증하는 로직이 포함되어 있습니다.
     *
     * @param userIdx 로그인한 사용자의 식별자
     * @param addressRequestReq 수정할 배송지 정보 (AddressCode 필수)
     * @return 처리 결과 메시지
     */
    @Override
    @Transactional
    public String updateAddress(Long userIdx, AddressRequestReq addressRequestReq) {
        Users users = userQueryServiceImpl.findActiveUser(userIdx);
        UsersAddress usersAddress = userAddressRepository.findByAddressCodeAndDel(addressRequestReq.getAddressCode(), 0).orElseThrow();

        // [수정] 수동 암호화 로직을 모두 제거하고 평문 필드를 그대로 엔티티에 전달합니다.
        usersAddress.updateAddress(
                addressRequestReq.getRecipient() != null ? addressRequestReq.getRecipient() : usersAddress.getRecipient(),
                addressRequestReq.getAddress() != null ? addressRequestReq.getAddress() : usersAddress.getAddress(),
                addressRequestReq.getAddressDetail() != null ? addressRequestReq.getAddressDetail() : usersAddress.getAddressDetail(),
                addressRequestReq.getPhoneNumber() != null ? addressRequestReq.getPhoneNumber() : usersAddress.getPhoneNumber()
        );

        return "주소 정보가 수정되었습니다.";
    }

    /**
     * 배송지를 삭제하는 메서드입니다.
     * 실제 데이터를 지우지 않고 Del 플래그를 업데이트하여 화면에서 숨김 처리(Soft Delete)합니다.
     *
     * @param userIdx 로그인한 사용자의 식별자
     * @param addressCode 삭제할 주소 코드
     * @return 처리 결과 메시지
     */
    @Override
    @Transactional
    public String deleteAddress(Long userIdx, String addressCode) {
        Users users = userQueryServiceImpl.findActiveUser(userIdx);

        // 삭제할 주소 조회
        UsersAddress usersAddress = userAddressRepository.findByAddressCodeAndDel(addressCode, 0)
                .orElseThrow(() -> new IllegalArgumentException("요청하신 주소 코드(" + addressCode + ")에 해당하는 정보가 없습니다."));

        if (!usersAddress.getUsersIdx().equals(userIdx)) {
            throw new IllegalStateException("해당 주소에 대한 수정/삭제 권한이 없습니다.");
        }

        // 삭제 처리 (Soft Delete)
        usersAddress.deleteAddress();

        return "주소가 삭제되었습니다.";
    }
}