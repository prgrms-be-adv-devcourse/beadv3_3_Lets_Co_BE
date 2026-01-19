package co.kr.user.service;

import co.kr.user.model.DTO.address.AddressListDTO;
import co.kr.user.model.DTO.address.AddressRequestReq;

import java.util.List;

/**
 * 사용자 배송지(Address) 관리 비즈니스 로직을 정의하는 인터페이스입니다.
 * 배송지 목록 조회, 추가, 수정, 삭제, 기본 배송지 설정 및 검색 기능을 명세합니다.
 * 구현체: AddressService
 */
public interface AddressServiceImpl {

    /**
     * 기본 배송지 조회 메서드 정의입니다.
     * 사용자가 설정한 기본 배송지의 식별자(ID)를 조회합니다.
     *
     * @param userIdx 사용자 고유 식별자
     * @return 기본 배송지 ID (AddressIdx)
     */
    Long defaultAddress(Long userIdx);

    /**
     * 배송지 검색 메서드 정의입니다.
     * 주소 코드(AddressCode)를 통해 특정 배송지의 식별자(ID)를 조회합니다.
     *
     * @param userIdx 사용자 고유 식별자
     * @param addressCode 검색할 주소 코드 (UUID)
     * @return 해당 배송지 ID (AddressIdx)
     */
    Long searchAddress(Long userIdx, String addressCode);

    /**
     * 배송지 목록 조회 메서드 정의입니다.
     * 사용자가 등록한 모든 배송지 정보를 조회합니다.
     *
     * @param userIdx 사용자 고유 식별자
     * @return 배송지 목록 (AddressListDTO 리스트)
     */
    List<AddressListDTO> addressList(Long userIdx);

    /**
     * 배송지 추가 메서드 정의입니다.
     * 새로운 배송지를 등록합니다. (개인정보 암호화 포함)
     *
     * @param userIdx 사용자 고유 식별자
     * @param addressRequestDTO 등록할 배송지 정보
     * @return 배송지 추가 결과 메시지
     */
    String addAddress(Long userIdx, AddressRequestReq addressRequestDTO);

    /**
     * 배송지 수정 메서드 정의입니다.
     * 기존 배송지 정보를 수정합니다.
     *
     * @param userIdx 사용자 고유 식별자
     * @param addressRequestDTO 수정할 배송지 정보
     * @return 배송지 수정 결과 메시지
     */
    String updateAddress(Long userIdx, AddressRequestReq addressRequestDTO);

    /**
     * 배송지 삭제 메서드 정의입니다.
     * 등록된 배송지를 삭제(Soft Delete)합니다.
     *
     * @param userIdx 사용자 고유 식별자
     * @param addressCode 삭제할 배송지 코드
     * @return 배송지 삭제 결과 메시지
     */
    String deleteAddress(Long userIdx, String addressCode);
}