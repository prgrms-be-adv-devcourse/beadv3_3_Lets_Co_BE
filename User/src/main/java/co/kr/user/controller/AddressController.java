package co.kr.user.controller;

import co.kr.user.model.dto.address.AddressDelReq;
import co.kr.user.model.dto.address.AddressListDTO;
import co.kr.user.model.dto.address.AddressRequestReq;
import co.kr.user.service.AddressService;
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
    public ResponseEntity<List<AddressListDTO>> addressList(@RequestHeader("X-USERS-IDX") Long userIdx) {
        return ResponseEntity.ok(addressService.addressList(userIdx));
    }

    @PostMapping("/add")
    public ResponseEntity<String> addAddress(@RequestHeader("X-USERS-IDX") Long userIdx,
                                             @RequestBody AddressRequestReq addressRequestReq) {
        return ResponseEntity.ok(addressService.addAddress(userIdx, addressRequestReq));
    }

    @PutMapping("/update")
    public ResponseEntity<String> updateAddress(@RequestHeader("X-USERS-IDX") Long userIdx,
                                                @RequestBody AddressRequestReq addressRequestReq) {
        return ResponseEntity.ok(addressService.updateAddress(userIdx, addressRequestReq));
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteAddress(@RequestHeader("X-USERS-IDX") Long userIdx,
                                                @RequestBody @Valid AddressDelReq addressDelReq) {
        return ResponseEntity.ok(addressService.deleteAddress(userIdx, addressDelReq.getAddressCode()));
    }
}