package co.kr.user.controller;

import co.kr.user.model.dto.address.AddressReq;
import co.kr.user.model.dto.client.BalanceReq;
import co.kr.user.model.dto.card.CardReq;
import co.kr.user.model.dto.client.ClientAddressDTO;
import co.kr.user.model.dto.client.ClientRoleDTO;
import co.kr.user.service.ClientService;
import co.kr.user.util.BaseResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 내부 시스템(MSA 환경의 타 서비스) 또는 클라이언트 앱 전용 기능을 제공하는 컨트롤러입니다.
 * 사용자 권한 확인, 잔액 관리, 기본 배송지/카드 조회 등 서비스 간 연동에 필요한 API를 주로 포함합니다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/client/users")
public class ClientController {
    private final ClientService clientService;

    /**
     * 특정 사용자의 권한(Role) 정보를 조회합니다.
     * @param userIdx 사용자 식별자 (Query Parameter)
     * @return 사용자 권한 정보 DTO
     */
    @GetMapping("/role")
    public ResponseEntity<BaseResponse<ClientRoleDTO>> getRole(@RequestParam @Valid Long userIdx) {
        return ResponseEntity.ok(new BaseResponse<>("SUCCESS", clientService.getRole(userIdx)));
    }

    /**
     * 사용자의 잔액(포인트/머니)을 변경합니다. (충전, 결제, 환불 등)
     * @param userIdx 사용자 식별자 (Path Variable)
     * @param balanceReq 잔액 변경 요청 정보 (금액, 유형)
     * @return 처리 성공 메시지
     */
    @PostMapping("/{userIdx}/balance")
    public ResponseEntity<BaseResponse<String>> balance(@PathVariable Long userIdx,
                                                        @RequestBody BalanceReq balanceReq) {
        return ResponseEntity.ok(new BaseResponse<>("SUCCESS", clientService.balance(userIdx, balanceReq)));
    }

    /**
     * 사용자의 기본 배송지 정보를 조회합니다.
     * 주문 서비스 등에서 배송 정보를 불러올 때 사용됩니다.
     * @param userIdx 사용자 식별자 (Path Variable)
     * @return 기본 배송지 정보 DTO
     */
    @PostMapping("/{userIdx}/address/default")
    public ResponseEntity<BaseResponse<ClientAddressDTO>> defaultAddress(@PathVariable Long userIdx) {
        return ResponseEntity.ok(new BaseResponse<>("SUCCESS", clientService.defaultAddress(userIdx)));
    }

    /**
     * 특정 주소 코드로 배송지 정보를 조회합니다.
     * @param userIdx 사용자 식별자 (Path Variable)
     * @param addressReq 조회할 주소 코드 정보
     * @return 해당 배송지 정보 DTO
     */
    @PostMapping("/{userIdx}/address/search")
    public ResponseEntity<BaseResponse<ClientAddressDTO>> searchAddress(@PathVariable Long userIdx,
                                                                        @RequestBody AddressReq addressReq) {
        return ResponseEntity.ok(new BaseResponse<>("SUCCESS", clientService.searchAddress(userIdx, addressReq.getAddressCode())));
    }

    /**
     * 사용자의 기본 카드 식별자(PK)를 조회합니다.
     * @param userIdx 사용자 식별자 (Path Variable)
     * @return 기본 카드 ID
     */
    @PostMapping("/{userIdx}/card/default")
    public ResponseEntity<BaseResponse<Long>> defaultCard(@PathVariable Long userIdx) {
        return ResponseEntity.ok(new BaseResponse<>("SUCCESS", clientService.defaultCard(userIdx)));
    }

    /**
     * 특정 카드 코드로 카드 식별자(PK)를 조회합니다.
     * @param userIdx 사용자 식별자 (Path Variable)
     * @param cardReq 조회할 카드 코드 정보
     * @return 해당 카드 ID
     */
    @PostMapping("/{userIdx}/card/search")
    public ResponseEntity<BaseResponse<Long>> searchCard(@PathVariable Long userIdx,
                                                         @RequestBody CardReq cardReq) {
        return ResponseEntity.ok(new BaseResponse<>("SUCCESS", clientService.searchCard(userIdx, cardReq.getCardCode())));
    }
}