package co.kr.user.controller;

import co.kr.user.model.DTO.address.AddressDelReq;
import co.kr.user.model.DTO.address.AddressListDTO;
import co.kr.user.model.DTO.address.AddressReq;
import co.kr.user.model.DTO.address.AddressRequestReq;
import co.kr.user.service.AddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 사용자 배송지(Address) 관리 컨트롤러 클래스입니다.
 * 사용자의 배송 주소 목록을 조회하거나 새로운 주소를 추가, 수정, 삭제하는 기능을 제공합니다.
 * 또한 기본 배송지 설정 및 특정 주소 검색 기능도 포함합니다.
 */
@Validated // 요청 데이터(파라미터, 바디 등)의 유효성 검증(Validation) 기능을 활성화합니다.
@RestController // RESTful API 컨트롤러임을 명시하며, 응답 데이터를 JSON 형식으로 반환합니다.
@RequiredArgsConstructor // final 필드에 대한 생성자를 자동으로 생성하여 의존성 주입(DI)을 받습니다.
@RequestMapping("/users/address") // 이 클래스 내의 모든 API 엔드포인트는 "/users/address" 경로로 시작합니다.
public class AddressController {

    // 주소 관련 비즈니스 로직을 처리하는 서비스 객체입니다.
    private final AddressService addressService;

    /**
     * 기본 배송지 조회 API입니다.
     * 사용자가 설정한 '기본 배송지'의 고유 ID(Address Index)를 조회합니다.
     *
     * @param userIdx HTTP 헤더(X-USERS-IDX)에 포함된 사용자 고유 식별자입니다.
     * @return 기본 배송지의 ID(Long)를 포함한 ResponseEntity 객체 (HTTP 200 OK)
     */
    @PostMapping("/default")
    public ResponseEntity<Long> defaultAddress(@RequestHeader("X-USERS-IDX") Long userIdx) {
        // AddressService를 호출하여 해당 사용자의 기본 배송지 식별자를 반환합니다.
        return ResponseEntity.ok(addressService.defaultAddress(userIdx));
    }

    /**
     * 배송지 검색 API입니다.
     * 특정 주소 코드(Address Code)를 기반으로 해당 주소의 실제 ID(Index)를 조회합니다.
     *
     * @param userIdx HTTP 헤더(X-USERS-IDX)에 포함된 사용자 고유 식별자입니다.
     * @param addressReq 검색할 주소 코드 정보가 담긴 요청 객체 (HTTP Body)입니다.
     * @return 검색된 주소의 ID(Long)를 포함한 ResponseEntity 객체 (HTTP 200 OK)
     */
    @PostMapping("/search")
    public ResponseEntity<Long> searchAddress(@RequestHeader("X-USERS-IDX") Long userIdx, @RequestBody AddressReq addressReq) {
        // AddressService를 호출하여 주소 코드에 해당하는 배송지 식별자를 반환합니다.
        return ResponseEntity.ok(addressService.searchAddress(userIdx, addressReq.getAddressCode()));
    }

    /**
     * 배송지 목록 조회 API입니다.
     * 사용자가 등록한 모든 배송지 목록을 조회합니다.
     *
     * @param userIdx HTTP 헤더(X-USERS-IDX)에 포함된 사용자 고유 식별자입니다.
     * @return 배송지 정보(AddressListDTO) 리스트를 포함한 ResponseEntity 객체 (HTTP 200 OK)
     */
    @PostMapping("/list")
    public ResponseEntity<List<AddressListDTO>> addressList(@RequestHeader("X-USERS-IDX") Long userIdx) {
        // AddressService를 호출하여 사용자의 전체 배송지 목록을 반환합니다.
        return ResponseEntity.ok(addressService.addressList(userIdx));
    }

    /**
     * 배송지 추가 API입니다.
     * 사용자의 새로운 배송지를 등록합니다.
     *
     * @param userIdx HTTP 헤더(X-USERS-IDX)에 포함된 사용자 고유 식별자입니다.
     * @param addressRequestReq 등록할 배송지 상세 정보(이름, 주소, 전화번호 등)가 담긴 요청 객체 (HTTP Body)입니다.
     * @return 처리 결과 메시지(String)를 포함한 ResponseEntity 객체 (HTTP 200 OK)
     */
    @PostMapping("/add")
    public ResponseEntity<String> addAddress(@RequestHeader("X-USERS-IDX") Long userIdx,
                                             @RequestBody AddressRequestReq addressRequestReq) {
        // AddressService를 호출하여 배송지 추가 로직을 수행하고 결과 메시지를 반환합니다.
        return ResponseEntity.ok(addressService.addAddress(userIdx, addressRequestReq));
    }

    /**
     * 배송지 수정 API입니다.
     * 기존에 등록된 배송지 정보를 수정합니다.
     *
     * @param userIdx HTTP 헤더(X-USERS-IDX)에 포함된 사용자 고유 식별자입니다.
     * @param addressRequestReq 수정할 배송지 정보가 담긴 요청 객체 (HTTP Body)입니다. 보통 ID와 변경할 데이터를 포함합니다.
     * @return 처리 결과 메시지(String)를 포함한 ResponseEntity 객체 (HTTP 200 OK)
     */
    @PutMapping("/update")
    public ResponseEntity<String> updateAddress(@RequestHeader("X-USERS-IDX") Long userIdx,
                                                @RequestBody AddressRequestReq addressRequestReq) {
        // AddressService를 호출하여 배송지 정보 수정 로직을 수행하고 결과 메시지를 반환합니다.
        return ResponseEntity.ok(addressService.updateAddress(userIdx, addressRequestReq));
    }

    /**
     * 배송지 삭제 API입니다.
     * 등록된 배송지를 삭제합니다. (실제 데이터 삭제 대신 Soft Delete 방식으로 처리될 수 있습니다.)
     *
     * @param userIdx HTTP 헤더(X-USERS-IDX)에 포함된 사용자 고유 식별자입니다.
     * @param addressDelReq 삭제할 배송지의 코드(혹은 ID)가 담긴 요청 객체 (HTTP Body)입니다. @Valid로 유효성을 검증합니다.
     * @return 처리 결과 메시지(String)를 포함한 ResponseEntity 객체 (HTTP 200 OK)
     */
    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteAddress(@RequestHeader("X-USERS-IDX") Long userIdx,
                                                @RequestBody @Valid AddressDelReq addressDelReq) {
        // AddressService를 호출하여 배송지 삭제 로직을 수행하고 결과 메시지를 반환합니다.
        return ResponseEntity.ok(addressService.deleteAddress(userIdx, addressDelReq.getAddressCode()));
    }
}