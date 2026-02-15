package co.kr.user.service.Impl;

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
 * AddressService 인터페이스의 구현체입니다.
 * 사용자 배송지(주소) 관리와 관련된 비즈니스 로직을 처리합니다.
 */
@Service // 스프링 서비스 빈으로 등록
@RequiredArgsConstructor // final 필드에 대한 생성자를 자동으로 생성
@Transactional(readOnly = true) // 기본적으로 읽기 전용 트랜잭션 적용 (조회 성능 최적화)
public class AddressServiceImpl implements AddressService {
    // DB 접근을 위한 Repository 및 조회 전용 서비스 주입
    private final UserAddressRepository userAddressRepository;
    private final UserQueryService userQueryService;

    /**
     * 사용자의 등록된 배송지 목록을 조회합니다.
     * 기본 배송지를 목록의 최상단에 위치시킵니다.
     * @param userIdx 사용자 식별자 (PK)
     * @return 정렬된 배송지 목록 DTO 리스트
     */
    @Override
    public List<AddressListDTO> addressList(Long userIdx) {
        // 사용자의 기본 배송지 인덱스를 확인하기 위해 사용자 상세 정보를 조회
        UsersInformation usersInformation = userQueryService.findActiveUserInfo(userIdx);
        Long defaultAddressIdx = usersInformation.getDefaultAddress();

        // 삭제되지 않은(UserDel.ACTIVE) 사용자의 모든 배송지 조회
        List<UsersAddress> usersAddressList = userAddressRepository.findAllByUsersIdxAndDel(userIdx, UserDel.ACTIVE);

        // 배송지가 하나도 없으면 예외 발생 (최소 1개는 있어야 함을 암시하거나, UI 처리를 위해 메시지 전달)
        if (usersAddressList.isEmpty()) {
            throw new IllegalArgumentException("주소를 추가해 주세요");
        }

        // 스트림을 사용하여 DTO로 변환 및 정렬 수행
        return usersAddressList.stream()
                .sorted((a, b) -> {
                    // 기본 배송지인 경우 우선순위를 높게(1) 설정하여 내림차순 정렬 시 맨 위로 오게 함
                    int aVal = a.getAddressIdx().equals(defaultAddressIdx) ? 1 : 0;
                    int bVal = b.getAddressIdx().equals(defaultAddressIdx) ? 1 : 0;
                    return Integer.compare(bVal, aVal); // 내림차순 정렬
                })
                .map(address -> {
                    AddressListDTO dto = new AddressListDTO();
                    // 현재 주소가 기본 배송지인지 여부를 1(True) 또는 0(False)으로 설정
                    int isDefault = address.getAddressIdx().equals(defaultAddressIdx) ? 1 : 0;
                    dto.setDefaultAddress(isDefault);
                    dto.setAddressCode(address.getAddressCode());
                    dto.setRecipient(address.getRecipient());
                    dto.setAddress(address.getAddress());
                    dto.setAddressDetail(address.getAddressDetail());
                    dto.setPhoneNumber(address.getPhoneNumber());
                    return dto;
                })
                .toList(); // 불변 리스트로 반환
    }

    /**
     * 새로운 배송지를 추가합니다.
     * @param userIdx 사용자 식별자 (PK)
     * @param addressRequestReq 추가할 주소 정보
     * @return 성공 메시지
     */
    @Override
    @Transactional // 데이터 변경이 발생하므로 쓰기 트랜잭션 적용
    public String addAddress(Long userIdx, AddressRequestReq addressRequestReq) {
        // 사용자 존재 여부 확인 (활성 사용자만 가능)
        userQueryService.findActiveUser(userIdx);

        // 새로운 주소 엔티티 생성 (빌더 패턴 사용)
        UsersAddress usersAddress = UsersAddress.builder()
                .usersIdx(userIdx)
                .recipient(addressRequestReq.getRecipient())
                .address(addressRequestReq.getAddress())
                .addressDetail(addressRequestReq.getAddressDetail())
                .phoneNumber(addressRequestReq.getPhoneNumber())
                .build();
        // DB에 저장
        userAddressRepository.save(usersAddress);

        // 요청에 '기본 배송지로 설정' 옵션이 있는 경우
        if (addressRequestReq.isDefaultAddress()) {
            // 사용자 상세 정보를 가져와서 기본 배송지 ID를 방금 생성한 주소의 ID로 업데이트
            UsersInformation usersInformation = userQueryService.findActiveUserInfo(userIdx);
            usersInformation.updateDefaultAddress(usersAddress.getAddressIdx());
        }

        return "주소가 성공적으로 추가되었습니다.";
    }

    /**
     * 기존 배송지 정보를 수정합니다.
     * @param userIdx 사용자 식별자 (PK)
     * @param req 수정할 주소 정보 (AddressCode 필수)
     * @return 성공 메시지
     */
    @Override
    @Transactional // 쓰기 트랜잭션 적용
    public String updateAddress(Long userIdx, AddressRequestReq req) {
        // 사용자 활성 상태 확인
        userQueryService.findActiveUser(userIdx);

        // 주소 코드와 사용자 ID로 수정할 주소 엔티티 조회
        UsersAddress usersAddress = userAddressRepository.findByAddressCodeAndUsersIdxAndDel(req.getAddressCode(), userIdx, UserDel.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("요청하신 주소 코드에 해당하는 정보가 없습니다."));

        // 엔티티의 수정 메서드를 호출하여 정보 업데이트 (Dirty Checking에 의해 자동 저장)
        usersAddress.updateAddress(req.getRecipient(), req.getAddress(), req.getAddressDetail(), req.getPhoneNumber());

        // 기본 배송지로 설정 요청이 있는 경우 업데이트
        if (req.isDefaultAddress()) {
            UsersInformation usersInformation = userQueryService.findActiveUserInfo(userIdx);
            usersInformation.updateDefaultAddress(usersAddress.getAddressIdx());
        }

        return "주소 정보가 수정되었습니다.";
    }

    /**
     * 배송지를 삭제합니다. (실제 삭제가 아닌 상태 변경: Soft Delete)
     * @param userIdx 사용자 식별자 (PK)
     * @param addressCode 삭제할 주소 코드
     * @return 성공 메시지
     */
    // [참고] 기본 배송지를 삭제할 경우, 별도의 대체 로직 없이 null 처리합니다.
    // 클라이언트 측에서 삭제 전 경고를 주거나, 추후 주문 시 주소 입력을 유도해야 합니다.
    @Override
    @Transactional // 쓰기 트랜잭션 적용
    public String deleteAddress(Long userIdx, String addressCode) {
        // 사용자 활성 상태 확인
        userQueryService.findActiveUser(userIdx);

        // 삭제할 주소 엔티티 조회
        UsersAddress usersAddress = userAddressRepository.findByAddressCodeAndUsersIdxAndDel(addressCode, userIdx, UserDel.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("요청하신 주소 코드에 해당하는 정보가 없습니다."));

        // 상태를 DELETED로 변경하여 논리적 삭제 처리
        usersAddress.deleteAddress();
        return "주소가 삭제되었습니다.";
    }
}