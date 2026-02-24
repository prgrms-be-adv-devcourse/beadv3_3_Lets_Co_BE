package co.kr.user.service;

import co.kr.user.model.dto.address.AddressListDTO;
import co.kr.user.model.dto.address.AddressRequestReq;

import java.util.List;

/**
 * 사용자 주소(배송지) 관리를 위한 비즈니스 로직 인터페이스입니다.
 * 주소 목록 조회, 추가, 수정, 삭제 기능을 정의합니다.
 */
public interface AddressService {

    /**
     * 특정 사용자의 등록된 주소 목록을 조회합니다.
     * @param userIdx 사용자 식별자 (PK)
     * @return 주소 목록 DTO 리스트
     */
    List<AddressListDTO> addressList(Long userIdx);

    /**
     * 사용자의 새로운 주소를 추가합니다.
     * @param userIdx 사용자 식별자 (PK)
     * @param addressRequestDTO 추가할 주소 정보가 담긴 요청 객체
     * @return 처리 결과 메시지 ("주소가 성공적으로 추가되었습니다.")
     */
    String addAddress(Long userIdx, AddressRequestReq addressRequestDTO);

    /**
     * 기존에 등록된 주소 정보를 수정합니다.
     * @param userIdx 사용자 식별자 (PK)
     * @param addressRequestDTO 수정할 주소 정보가 담긴 요청 객체 (AddressCode 포함)
     * @return 처리 결과 메시지 ("주소 정보가 수정되었습니다.")
     */
    String updateAddress(Long userIdx, AddressRequestReq addressRequestDTO);

    /**
     * 등록된 주소를 삭제(논리적 삭제)합니다.
     * @param userIdx 사용자 식별자 (PK)
     * @param addressCode 삭제할 주소의 고유 코드
     * @return 처리 결과 메시지 ("주소가 삭제되었습니다.")
     */
    String deleteAddress(Long userIdx, String addressCode);
}