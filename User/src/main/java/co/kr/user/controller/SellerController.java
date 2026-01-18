package co.kr.user.controller;

import co.kr.user.model.DTO.auth.AuthenticationReq;
import co.kr.user.model.DTO.seller.SellerRegisterDTO;
import co.kr.user.model.DTO.seller.SellerRegisterReq;
import co.kr.user.service.SellerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/seller/users")
public class SellerController {
    private final SellerService sellerService;

    @PostMapping("/register")
    public ResponseEntity<SellerRegisterDTO> sellerRegister(@RequestHeader("X-USERS-IDX") Long userIdx,
                                                            @RequestBody @Valid SellerRegisterReq sellerRegisterReq) {
        return ResponseEntity.ok(sellerService.sellerRegister(userIdx, sellerRegisterReq));
    }

    @PostMapping("/register/check")
    public ResponseEntity<String> sellerRegisterCheck(@RequestHeader("X-USERS-IDX") Long userIdx,
                                                      @RequestBody @Valid AuthenticationReq authenticationReq) {
        return ResponseEntity.ok(sellerService.sellerRegisterCheck(userIdx, authenticationReq.getCode()));
    }
}