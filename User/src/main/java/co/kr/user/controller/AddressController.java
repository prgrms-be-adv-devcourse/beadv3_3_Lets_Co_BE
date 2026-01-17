package co.kr.user.controller;

import co.kr.user.model.DTO.address.AddressDelReq;
import co.kr.user.model.DTO.address.AddressListDTO;
import co.kr.user.model.DTO.address.AddressRequestReq;
import co.kr.user.service.AddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j // Lombok: 로그를 남기기 위한 log 객체를 자동으로 생성해줍니다.
@Validated // Controller 레벨에서 @RequestParam 등의 유효성 검증(Validation)을 활성화합니다.
@RestController // 이 클래스가 REST API용 컨트롤러임을 명시 (모든 메서드의 리턴값이 JSON 형태가 됨)
@RequiredArgsConstructor // final이 붙은 필드에 대해 생성자를 자동으로 만들어주어 의존성을 주입받습니다.
@RequestMapping("/users") // 이 컨트롤러의 기본 URL 경로를 '/auth'로 설정합니다. (예: /auth/signup)
public class AddressController {

    private final AddressService addressService;


    @PostMapping("/list")
    public ResponseEntity<List<AddressListDTO>> addressList(@RequestHeader("X-USERS-IDX") Long userIdx) {
        return ResponseEntity.ok(addressService.addressList(userIdx));
    }

    // [New] 주소 추가
    @PostMapping("/add")
    public ResponseEntity<String> addAddress(@RequestHeader("X-USERS-IDX") Long userIdx,
                                             @RequestBody AddressRequestReq addressRequestReq) {
        return ResponseEntity.ok(addressService.addAddress(userIdx, addressRequestReq));
    }

    // [New] 주소 수정
    @PutMapping("/update")
    public ResponseEntity<String> updateAddress(@RequestHeader("X-USERS-IDX") Long userIdx,
                                                @RequestBody AddressRequestReq addressRequestReq) {
        return ResponseEntity.ok(addressService.updateAddress(userIdx, addressRequestReq));
    }

    // [New] 주소 삭제 (Soft Delete)
    @DeleteMapping("/delete/{addressIdx}")
    public ResponseEntity<String> deleteAddress(@RequestHeader("X-USERS-IDX") Long userIdx,
                                                @RequestBody @Valid AddressDelReq addressDelReq) {
        return ResponseEntity.ok(addressService.deleteAddress(userIdx, addressDelReq.getAddressCode()));
    }
}
