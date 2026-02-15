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

/**
 * 사용자 배송지(주소) 관리를 위한 REST 컨트롤러입니다.
 * 주소 목록 조회, 추가, 수정, 삭제 API를 제공합니다.
 * 요청 헤더의 "X-USERS-IDX"를 통해 인증된 사용자 ID를 받아 처리합니다.
 */
@Validated // 메서드 파라미터 유효성 검증(@Valid 등)을 활성화
@RestController
@RequiredArgsConstructor
@RequestMapping("/users/address") // 기본 경로 설정
public class AddressController {
    private final AddressService addressService;

    /**
     * 사용자의 등록된 배송지 목록을 조회합니다.
     * @param userIdx 요청 헤더에서 추출한 사용자 식별자
     * @return 배송지 목록 DTO 리스트를 담은 응답 객체
     */
    @PostMapping("/list")
    public ResponseEntity<BaseResponse<List<AddressListDTO>>> addressList(@RequestHeader("X-USERS-IDX") Long userIdx) {
        return ResponseEntity.ok(new BaseResponse<>("SUCCESS", addressService.addressList(userIdx)));
    }

    /**
     * 새로운 배송지를 추가합니다.
     * @param userIdx 요청 헤더에서 추출한 사용자 식별자
     * @param addressRequestReq 추가할 주소 정보 (수령인, 주소, 연락처 등)
     * @return 처리 성공 메시지를 담은 응답 객체
     */
    @PostMapping("/add")
    public ResponseEntity<BaseResponse<String>> addAddress(@RequestHeader("X-USERS-IDX") Long userIdx,
                                                           @RequestBody AddressRequestReq addressRequestReq) {
        return ResponseEntity.ok(new BaseResponse<>("SUCCESS", addressService.addAddress(userIdx, addressRequestReq)));
    }

    /**
     * 등록된 배송지 정보를 수정합니다.
     * @param userIdx 요청 헤더에서 추출한 사용자 식별자
     * @param addressRequestReq 수정할 주소 정보 (주소 코드 포함)
     * @return 처리 성공 메시지를 담은 응답 객체
     */
    @PutMapping("/update")
    public ResponseEntity<BaseResponse<String>> updateAddress(@RequestHeader("X-USERS-IDX") Long userIdx,
                                                              @RequestBody AddressRequestReq addressRequestReq) {
        return ResponseEntity.ok(new BaseResponse<>("SUCCESS", addressService.updateAddress(userIdx, addressRequestReq)));
    }

    /**
     * 등록된 배송지를 삭제합니다.
     * @param userIdx 요청 헤더에서 추출한 사용자 식별자
     * @param addressDelReq 삭제할 주소 코드 정보 (@Valid로 유효성 검사 수행)
     * @return 처리 성공 메시지를 담은 응답 객체
     */
    @DeleteMapping("/delete")
    public ResponseEntity<BaseResponse<String>> deleteAddress(@RequestHeader("X-USERS-IDX") Long userIdx,
                                                              @RequestBody @Valid AddressDelReq addressDelReq) {
        return ResponseEntity.ok(new BaseResponse<>("SUCCESS", addressService.deleteAddress(userIdx, addressDelReq.getAddressCode())));
    }
}