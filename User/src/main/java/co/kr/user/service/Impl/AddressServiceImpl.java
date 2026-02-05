package co.kr.user.service.Impl;

import co.kr.user.dao.UserAddressRepository;
import co.kr.user.dao.UserRepository;
import co.kr.user.model.dto.address.AddressListDTO;
import co.kr.user.model.dto.address.AddressRequestReq;
import co.kr.user.model.entity.Users;
import co.kr.user.model.entity.UsersAddress;
import co.kr.user.service.AddressService;
import co.kr.user.util.AESUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * 사용자 배송지(Address) 관리 비즈니스 로직을 처리하는 서비스 클래스입니다.
 * 배송지 추가, 수정, 삭제(Soft Delete), 목록 조회, 기본 배송지 확인 기능을 제공합니다.
 * 개인정보(이름, 주소, 전화번호)는 암호화하여 DB에 저장하고, 조회 시 복호화하여 반환합니다.
 */
@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {
    private final UserRepository userRepository;
    private final UserAddressRepository userAddressRepository;

    private final AESUtil aesUtil; // 개인정보 양방향 암호화 유틸리티

    /**
     * 사용자의 기본 배송지 식별자(PK)를 조회하는 메서드입니다.
     * 상품 구매 등의 프로세스에서 별도 선택이 없을 경우 사용될 주소를 찾습니다.
     *
     * @param userIdx 로그인한 사용자의 식별자
     * @return 기본 배송지의 AddressIdx
     */
    @Override
    public Long defaultAddress(Long userIdx) {
        // 사용자 검증
        Users users = userRepository.findById(userIdx)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이디입니다."));

        // 계정 상태 확인 (탈퇴/미인증)
        if (users.getDel() == 1) {
            throw new IllegalStateException("탈퇴한 회원입니다.");
        }
        else if (users.getDel() == 2) {
            throw new IllegalStateException("인증을 먼저 시도해 주세요.");
        }

        // 기본 배송지(DefaultAddress=1)이면서 삭제되지 않은(Del=0) 주소 조회
        UsersAddress usersAddress = userAddressRepository.findFirstByUsersIdxAndDelOrderByAddressIdxDesc(users.getUsersIdx(),  0)
                .orElseThrow(null); // 없을 경우 null 예외 발생 (적절한 예외 처리 필요)

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
        Users users = userRepository.findById(userIdx)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이디입니다."));

        if (users.getDel() == 1) {
            throw new IllegalStateException("탈퇴한 회원입니다.");
        }
        else if (users.getDel() == 2) {
            throw new IllegalStateException("인증을 먼저 시도해 주세요.");
        }

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
        Users users = userRepository.findById(userIdx)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이디입니다."));

        if (users.getDel() == 1) {
            throw new IllegalStateException("탈퇴한 회원입니다.");
        }
        else if (users.getDel() == 2) {
            throw new IllegalStateException("인증을 먼저 시도해 주세요.");
        }

        // 삭제되지 않은 모든 주소 조회
        List<UsersAddress> usersAddressList = userAddressRepository.findAllByUsersIdxAndDel(users.getUsersIdx(), 0);

        if (usersAddressList.isEmpty()) {
            throw new IllegalArgumentException("주소를 추가해 주세요");
        }

        // Entity -> DTO 변환 (복호화 포함)
        return usersAddressList.stream()
                .map(address -> {
                    AddressListDTO dto = new AddressListDTO();
                    dto.setAddressCode(address.getAddressCode());
                    dto.setRecipient(aesUtil.decrypt(address.getRecipient()));
                    dto.setAddress(aesUtil.decrypt(address.getAddress()));
                    dto.setAddressDetail(aesUtil.decrypt(address.getAddressDetail()));
                    dto.setPhoneNumber(aesUtil.decrypt(address.getPhoneNumber()));
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
        Users users = userRepository.findById(userIdx)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이디입니다."));

        if (users.getDel() == 1) {
            throw new IllegalStateException("탈퇴한 회원입니다.");
        }
        else if (users.getDel() == 2) {
            throw new IllegalStateException("인증을 먼저 시도해 주세요.");
        }

        // 주소 엔티티 생성 및 암호화
        UsersAddress usersAddress = UsersAddress.builder()
                .usersIdx(users.getUsersIdx())
                .addressCode(UUID.randomUUID().toString())
                .recipient(aesUtil.encrypt(addressRequestReq.getRecipient()))
                .address(aesUtil.encrypt(addressRequestReq.getAddress()))
                .addressDetail(aesUtil.encrypt(addressRequestReq.getAddressDetail()))
                .phoneNumber(aesUtil.encrypt(addressRequestReq.getPhoneNumber()))
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
        Users users = userRepository.findById(userIdx)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이디입니다."));

        if (users.getDel() == 1) {
            throw new IllegalStateException("탈퇴한 회원입니다.");
        }
        else if (users.getDel() == 2) {
            throw new IllegalStateException("인증을 먼저 시도해 주세요.");
        }

        // 수정할 주소 조회
        UsersAddress usersAddress = userAddressRepository.findByAddressCodeAndDel(addressRequestReq.getAddressCode(), 0)
                .orElseThrow(() -> new IllegalArgumentException("해당 주소 정보를 찾을 수 없습니다."));

        // 소유권 검증
        if (!usersAddress.getUsersIdx().equals(userIdx)) {
            throw new IllegalStateException("본인의 주소만 수정할 수 있습니다.");
        }

        // 수정 데이터 준비 (입력값이 없으면 기존 값 유지, 있으면 암호화)
        AddressRequestReq dto = new AddressRequestReq();
        dto.setAddressCode(usersAddress.getAddressCode());

        // 기본 배송지 설정 여부
        if (addressRequestReq.getDefaultAddress() == 1) {
            dto.setDefaultAddress(1);
        } else {
            dto.setDefaultAddress(0);
        }

        // 수령인
        if (addressRequestReq.getRecipient() == null || addressRequestReq.getRecipient().isEmpty()) {
            dto.setRecipient(usersAddress.getRecipient()); // 기존 암호화된 값 유지
        } else {
            dto.setRecipient(aesUtil.encrypt(addressRequestReq.getRecipient())); // 새 값 암호화
        }

        // 주소
        if (addressRequestReq.getAddress() == null || addressRequestReq.getAddress().isEmpty()) {
            dto.setAddress(usersAddress.getAddress());
        }
        else {
            dto.setAddress(aesUtil.encrypt(addressRequestReq.getAddress()));
        }

        // 상세 주소
        if (addressRequestReq.getAddressDetail() == null || addressRequestReq.getAddressDetail().isEmpty()) {
            dto.setAddressDetail(usersAddress.getAddressDetail());
        }
        else {
            dto.setAddressDetail(aesUtil.encrypt(addressRequestReq.getAddressDetail()));
        }

        // 전화번호
        if (addressRequestReq.getPhoneNumber() == null || addressRequestReq.getPhoneNumber().isEmpty()) {
            dto.setPhoneNumber(usersAddress.getPhoneNumber());
        }
        else {
            dto.setPhoneNumber(aesUtil.encrypt(addressRequestReq.getPhoneNumber()));
        }

        // 엔티티 업데이트 수행
        usersAddress.updateAddress(
                dto.getRecipient(),
                dto.getAddress(),
                dto.getAddressDetail(),
                dto.getPhoneNumber()
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
        Users users = userRepository.findById(userIdx)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이디입니다."));

        if (users.getDel() == 1) {
            throw new IllegalStateException("탈퇴한 회원입니다.");
        }
        else if (users.getDel() == 2) {
            throw new IllegalStateException("인증을 먼저 시도해 주세요.");
        }

        // 삭제할 주소 조회
        UsersAddress usersAddress = userAddressRepository.findByAddressCodeAndDel(addressCode, 0)
                .orElseThrow(() -> new IllegalArgumentException("해당 주소 정보를 찾을 수 없습니다."));

        // 소유권 검증
        if (!usersAddress.getUsersIdx().equals(userIdx)) {
            throw new IllegalStateException("본인의 주소만 수정할 수 있습니다.");
        }

        // 삭제 처리 (Soft Delete)
        usersAddress.deleteAddress();

        return "주소가 삭제되었습니다.";
    }
}