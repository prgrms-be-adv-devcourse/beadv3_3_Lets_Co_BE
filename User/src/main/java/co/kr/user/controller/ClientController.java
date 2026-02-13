package co.kr.user.controller;

import co.kr.user.model.dto.address.AddressReq;
import co.kr.user.model.dto.client.BalanceReq;
import co.kr.user.model.dto.card.CardReq;
import co.kr.user.model.dto.client.ClientAddressDTO;
import co.kr.user.model.dto.client.ClientRoleDTO;
import co.kr.user.service.ClientService;
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
    public ResponseEntity<ClientRoleDTO> getRole(@RequestParam @Valid Long userIdx) {
        return ResponseEntity.ok(clientService.getRole(userIdx));
    }

    @PostMapping("/{userIdx}/balance")
    public ResponseEntity<String> balance(@PathVariable Long userIdx, @RequestBody BalanceReq balanceReq) {
        return ResponseEntity.ok(clientService.balance(userIdx, balanceReq));
    }

    @PostMapping("/{userIdx}/address/default")
    public ResponseEntity<ClientAddressDTO> defaultAddress(@PathVariable Long userIdx) {
        return ResponseEntity.ok(clientService.defaultAddress(userIdx));
    }

    @PostMapping("/{userIdx}/address/search")
    public ResponseEntity<ClientAddressDTO> searchAddress(@PathVariable Long userIdx, @RequestBody AddressReq addressReq) {
        return ResponseEntity.ok(clientService.searchAddress(userIdx, addressReq.getAddressCode()));
    }

    @PostMapping("/{userIdx}/card/default")
    public ResponseEntity<Long> defaultCard(@PathVariable Long userIdx) {
        return ResponseEntity.ok(clientService.defaultCard(userIdx));
    }

    @PostMapping("/{userIdx}/card/search")
    public ResponseEntity<Long> searchCard(@PathVariable Long userIdx, @RequestBody CardReq cardReq) {
        return ResponseEntity.ok(clientService.searchCard(userIdx, cardReq.getCardCode()));
    }
}
