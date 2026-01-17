package co.kr.user.controller;

import co.kr.user.model.DTO.auth.AuthenticationReq;
import co.kr.user.model.DTO.seller.SellerRegisterDTO;
import co.kr.user.model.DTO.seller.SellerRegisterReq;
import co.kr.user.service.SellerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/seller/users")
public class SellerController {

    private final SellerService sellerService;

    @PostMapping("/register")
    // 메서드 이름: sellerRegister, 변수명: userIdx 로 변경
    public ResponseEntity<SellerRegisterDTO> sellerRegister(
            @RequestHeader("X-USERS-IDX") Long userIdx,
            @RequestBody @Valid SellerRegisterReq sellerRegisterReq) {

        log.info("=======================================================");
        log.info("SellerRegister - Seller Register Request");
        log.info("userIdx : {}", userIdx);
        log.info("sellerRegisterReq : {}", sellerRegisterReq.toString());
        log.info("=======================================================");

        // 서비스 메서드 호출 시 수정된 이름 사용
        return ResponseEntity.ok(sellerService.sellerRegister(userIdx, sellerRegisterReq));
    }

    @PostMapping("/register/check")
    public ResponseEntity<String> sellerRegisterCheck(@RequestHeader("X-USERS-IDX") Long userIdx,
                                                        @RequestBody @Valid AuthenticationReq authenticationReq) {
        log.info("=======================================================");
        log.info("SellerRegisterCheck - Seller Register Check Request");
        log.info("userIdx : {}", userIdx);
        log.info("authenticationReq : {}", authenticationReq.toString());
        log.info("=======================================================");

        return ResponseEntity.ok(sellerService.sellerRegisterCheck(userIdx, authenticationReq.getCode()));
    }
}