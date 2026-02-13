package co.kr.user.controller;

import co.kr.user.model.dto.admin.SellerDeleteSecondStepReq;
import co.kr.user.model.dto.my.UserAmendReq;
import co.kr.user.model.dto.my.UserDTO;
import co.kr.user.model.dto.my.UserDeleteDTO;
import co.kr.user.model.dto.my.UserDeleteSecondStepReq;
import co.kr.user.model.dto.seller.*;
import co.kr.user.service.SellerService;
import jakarta.servlet.http.HttpServletResponse;
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
                                                      @RequestBody @Valid SellerAuthenticationReq sellerAuthenticationReq) {
        return ResponseEntity.ok(sellerService.sellerRegisterCheck(userIdx, sellerAuthenticationReq.getAuthCode()));
    }

    @PostMapping("/my")
    public ResponseEntity<SellerProfileDTO> my(@RequestHeader ("X-USERS-IDX") Long userIdx) {
        return ResponseEntity.ok(sellerService.my(userIdx));
    }

    @PutMapping("/my")
    public ResponseEntity<String> sellerAmendReqResponseEntity(@RequestHeader ("X-USERS-IDX") Long userIdx,
                                                               @RequestBody SellerAmendReq sellerAmendReq) {
        return ResponseEntity.ok(sellerService.myAmend(userIdx, sellerAmendReq));
    }

        @PostMapping("/my/delete")
    public ResponseEntity<UserDeleteDTO> myDelete(@RequestHeader ("X-USERS-IDX") Long userIdx) {
        return ResponseEntity.ok(sellerService.myDelete(userIdx));
    }

    @DeleteMapping("/my/delete")
    public ResponseEntity<String> deleteUser(@RequestHeader ("X-USERS-IDX") Long userIdx,
                                             @RequestBody @Valid SellerDeleteSecondStepReq sellerDeleteSecondStepReq) {
        return ResponseEntity.ok(sellerService.myDelete(userIdx, sellerDeleteSecondStepReq.getAuthCode()));
    }
}