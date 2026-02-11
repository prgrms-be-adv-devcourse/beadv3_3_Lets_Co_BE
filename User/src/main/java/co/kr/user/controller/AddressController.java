package co.kr.user.controller;

import co.kr.user.model.dto.address.AddressDelReq;
import co.kr.user.model.dto.address.AddressListDTO;
import co.kr.user.model.dto.address.AddressRequestReq;
import co.kr.user.service.AddressService;
import co.kr.user.util.BaseResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/users/address")
public class AddressController {
    private final AddressService addressService;

    @PostMapping("/list")
    public ResponseEntity<BaseResponse<List<AddressListDTO>>> addressList(@RequestHeader("X-USERS-IDX") Long userIdx) {
        return ResponseEntity.ok(new BaseResponse<>("SUCCESS", addressService.addressList(userIdx)));
    }

    @PostMapping("/add")
    public ResponseEntity<BaseResponse<String>> addAddress(@RequestHeader("X-USERS-IDX") Long userIdx,
                                                           @RequestBody AddressRequestReq addressRequestReq) {
        return ResponseEntity.ok(new BaseResponse<>("SUCCESS", addressService.addAddress(userIdx, addressRequestReq)));
    }

    @PutMapping("/update")
    public ResponseEntity<BaseResponse<String>> updateAddress(@RequestHeader("X-USERS-IDX") Long userIdx,
                                                              @RequestBody AddressRequestReq addressRequestReq) {
        return ResponseEntity.ok(new BaseResponse<>("SUCCESS", addressService.updateAddress(userIdx, addressRequestReq)));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<BaseResponse<String>> deleteAddress(@RequestHeader("X-USERS-IDX") Long userIdx,
                                                              @RequestBody @Valid AddressDelReq addressDelReq) {
        return ResponseEntity.ok(new BaseResponse<>("SUCCESS", addressService.deleteAddress(userIdx, addressDelReq.getAddressCode())));
    }
}