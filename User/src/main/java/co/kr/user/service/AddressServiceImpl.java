package co.kr.user.service;

import co.kr.user.model.DTO.address.AddressListDTO;
import co.kr.user.model.DTO.address.AddressRequestReq;

import java.util.List;

public interface AddressServiceImpl {

    List<AddressListDTO> addressList(Long userIdx);

    // 추가
    String addAddress(Long userIdx, AddressRequestReq addressRequestDTO);

    // 수정
    String updateAddress(Long userIdx, AddressRequestReq addressRequestDTO);

    // 삭제 (Soft Delete)
    String deleteAddress(Long userIdx, String addressCode);
}
