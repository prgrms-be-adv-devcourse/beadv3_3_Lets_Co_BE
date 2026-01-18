package co.kr.user.service;

import co.kr.user.model.DTO.address.AddressListDTO;
import co.kr.user.model.DTO.address.AddressRequestReq;

import java.util.List;

public interface AddressServiceImpl {
    List<AddressListDTO> addressList(Long userIdx);

    String addAddress(Long userIdx, AddressRequestReq addressRequestDTO);

    String updateAddress(Long userIdx, AddressRequestReq addressRequestDTO);

    String deleteAddress(Long userIdx, String addressCode);
}