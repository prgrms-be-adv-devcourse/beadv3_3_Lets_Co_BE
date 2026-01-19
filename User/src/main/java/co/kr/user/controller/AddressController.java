package co.kr.user.controller;

import co.kr.user.model.DTO.address.AddressDelReq;
import co.kr.user.model.DTO.address.AddressListDTO;
import co.kr.user.model.DTO.address.AddressReq;
import co.kr.user.model.DTO.address.AddressRequestReq;
import co.kr.user.model.DTO.card.CardReq;
import co.kr.user.service.AddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 사용자 배송지(Address) 관리 컨트롤러
 * * <p>사용자의 배송 주소 목록을 조회하거나 새로운 주소를 추가, 수정, 삭제하는 기능을 제공합니다.</p>
 * <p>또한 기본 배송지 설정 및 특정 주소 검색 기능도 포함합니다.</p>
 */
@Validated // 데이터 유효성 검증(Validation) 활성화
@RestController // JSON 응답을 반환하는 RESTful 컨트롤러
@RequiredArgsConstructor // final 필드에 대한 생성자 자동 생성 (의존성 주입)
@RequestMapping("/users/address") // 기본 API 경로: /users/address
public class AddressController {
    // 주소 관련 비즈니스 로직을 처리하는 서비스
    private final AddressService addressService;

    /**
     * 기본 배송지 조회 API
     * * <p>사용자가 설정한 '기본 배송지'의 고유 ID를 조회합니다.</p>
     * <p>HTTP Method: POST</p>
     * <p>Path: /users/address/default</p>
     * * @param userIdx HTTP 헤더(X-USERS-IDX)에 포함된 사용자 고유 식별자
     * @return 기본 배송지의 ID(Long)를 반환 (200 OK)
     */
    @PostMapping("/default")
    public ResponseEntity<Long> defaultAddress(@RequestHeader("X-USERS-IDX") Long userIdx) {
        return ResponseEntity.ok(addressService.defaultAddress(userIdx));
    }

    /**
     * 배송지 검색 API
     * * <p>특정 주소 코드(Address Code)를 기반으로 해당 주소의 ID를 조회합니다.</p>
     * <p>HTTP Method: POST</p>
     * <p>Path: /users/address/search</p>
     * * @param userIdx 사용자 고유 식별자 (Header)
     * @param addressReq 검색할 주소 코드 정보가 담긴 요청 객체 (Body)
     * @return 검색된 주소의 ID(Long)를 반환 (200 OK)
     */
    @PostMapping("/search")
    public ResponseEntity<Long> searchAddress(@RequestHeader("X-USERS-IDX") Long userIdx, @RequestBody AddressReq addressReq) {
        return ResponseEntity.ok(addressService.searchAddress(userIdx, addressReq.getAddressCode()));
    }

    /**
     * 배송지 목록 조회 API
     * * <p>사용자가 등록한 모든 배송지 목록을 조회합니다.</p>
     * <p>HTTP Method: POST</p>
     * <p>Path: /users/address/list</p>
     * * @param userIdx 사용자 고유 식별자 (Header)
     * @return 배송지 정보(AddressListDTO) 리스트를 반환 (200 OK)
     */
    @PostMapping("/list")
    public ResponseEntity<List<AddressListDTO>> addressList(@RequestHeader("X-USERS-IDX") Long userIdx) {
        return ResponseEntity.ok(addressService.addressList(userIdx));
    }

    /**
     * 배송지 추가 API
     * * <p>새로운 배송지를 등록합니다.</p>
     * <p>HTTP Method: POST</p>
     * <p>Path: /users/address/add</p>
     * * @param userIdx 사용자 고유 식별자 (Header)
     * @param addressRequestReq 등록할 배송지 상세 정보(이름, 주소, 전화번호 등)가 담긴 요청 객체 (Body)
     * @return 처리 결과 메시지(String)를 반환 (200 OK)
     */
    @PostMapping("/add")
    public ResponseEntity<String> addAddress(@RequestHeader("X-USERS-IDX") Long userIdx,
                                             @RequestBody AddressRequestReq addressRequestReq) {
        return ResponseEntity.ok(addressService.addAddress(userIdx, addressRequestReq));
    }

    /**
     * 배송지 수정 API
     * * <p>기존에 등록된 배송지 정보를 수정합니다.</p>
     * <p>HTTP Method: PUT</p>
     * <p>Path: /users/address/update</p>
     * * @param userIdx 사용자 고유 식별자 (Header)
     * @param addressRequestReq 수정할 배송지 정보가 담긴 요청 객체 (Body) - 보통 ID와 변경할 데이터를 포함
     * @return 처리 결과 메시지(String)를 반환 (200 OK)
     */
    @PutMapping("/update")
    public ResponseEntity<String> updateAddress(@RequestHeader("X-USERS-IDX") Long userIdx,
                                                @RequestBody AddressRequestReq addressRequestReq) {
        return ResponseEntity.ok(addressService.updateAddress(userIdx, addressRequestReq));
    }

    /**
     * 배송지 삭제 API
     * * <p>등록된 배송지를 삭제합니다.</p>
     * <p>HTTP Method: DELETE</p>
     * <p>Path: /users/address/delete</p>
     * * @param userIdx 사용자 고유 식별자 (Header)
     * @param addressDelReq 삭제할 배송지의 코드(혹은 ID)가 담긴 요청 객체 (Body)
     * @return 처리 결과 메시지(String)를 반환 (200 OK)
     */
    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteAddress(@RequestHeader("X-USERS-IDX") Long userIdx,
                                                @RequestBody @Valid AddressDelReq addressDelReq) {
        return ResponseEntity.ok(addressService.deleteAddress(userIdx, addressDelReq.getAddressCode()));
    }
}