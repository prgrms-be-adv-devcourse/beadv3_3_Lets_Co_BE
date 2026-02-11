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

@RestController
@RequiredArgsConstructor
@RequestMapping("/client/users")
public class ClientController {
    private final ClientService clientService;

    @GetMapping("/role")
    public ResponseEntity<BaseResponse<ClientRoleDTO>> getRole(@RequestParam @Valid Long userIdx) {
        return ResponseEntity.ok(new BaseResponse<>("SUCCESS", clientService.getRole(userIdx)));
    }

    @PostMapping("/{userIdx}/balance")
    public ResponseEntity<BaseResponse<String>> balance(@PathVariable Long userIdx,
                                                        @RequestBody BalanceReq balanceReq) {
        return ResponseEntity.ok(new BaseResponse<>("SUCCESS", clientService.balance(userIdx, balanceReq)));
    }

    @PostMapping("/{userIdx}/address/default")
    public ResponseEntity<BaseResponse<ClientAddressDTO>> defaultAddress(@PathVariable Long userIdx) {
        return ResponseEntity.ok(new BaseResponse<>("SUCCESS", clientService.defaultAddress(userIdx)));
    }

    @PostMapping("/{userIdx}/address/search")
    public ResponseEntity<BaseResponse<ClientAddressDTO>> searchAddress(@PathVariable Long userIdx,
                                                                        @RequestBody AddressReq addressReq) {
        return ResponseEntity.ok(new BaseResponse<>("SUCCESS", clientService.searchAddress(userIdx, addressReq.getAddressCode())));
    }

    @PostMapping("/{userIdx}/card/default")
    public ResponseEntity<BaseResponse<Long>> defaultCard(@PathVariable Long userIdx) {
        return ResponseEntity.ok(new BaseResponse<>("SUCCESS", clientService.defaultCard(userIdx)));
    }

    @PostMapping("/{userIdx}/card/search")
    public ResponseEntity<BaseResponse<Long>> searchCard(@PathVariable Long userIdx,
                                                         @RequestBody CardReq cardReq) {
        return ResponseEntity.ok(new BaseResponse<>("SUCCESS", clientService.searchCard(userIdx, cardReq.getCardCode())));
    }
}