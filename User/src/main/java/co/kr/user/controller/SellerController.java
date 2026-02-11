package co.kr.user.controller;

import co.kr.user.model.dto.admin.SellerDeleteSecondStepReq;
import co.kr.user.model.dto.my.UserDeleteDTO;
import co.kr.user.model.dto.seller.*;
import co.kr.user.service.SellerService;
import co.kr.user.util.BaseResponse;
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
    public ResponseEntity<BaseResponse<SellerRegisterDTO>> sellerRegister(@RequestHeader("X-USERS-IDX") Long userIdx,
                                                                          @RequestBody @Valid SellerRegisterReq sellerRegisterReq) {
        return ResponseEntity.ok(new BaseResponse<>("SUCCESS", sellerService.sellerRegister(userIdx, sellerRegisterReq)));
    }

    @PostMapping("/register/check")
    public ResponseEntity<BaseResponse<String>> sellerRegisterCheck(@RequestHeader("X-USERS-IDX") Long userIdx,
                                                                    @RequestBody @Valid SellerAuthenticationReq sellerAuthenticationReq) {
        return ResponseEntity.ok(new BaseResponse<>("SUCCESS", sellerService.sellerRegisterCheck(userIdx, sellerAuthenticationReq.getAuthCode())));
    }

    @PostMapping("/my")
    public ResponseEntity<BaseResponse<SellerProfileDTO>> my(@RequestHeader ("X-USERS-IDX") Long userIdx) {
        return ResponseEntity.ok(new BaseResponse<>("SUCCESS", sellerService.my(userIdx)));
    }

    @PutMapping("/my")
    public ResponseEntity<BaseResponse<String>> sellerAmend(@RequestHeader ("X-USERS-IDX") Long userIdx,
                                                            @RequestBody SellerAmendReq sellerAmendReq) {
        return ResponseEntity.ok(new BaseResponse<>("SUCCESS", sellerService.myAmend(userIdx, sellerAmendReq)));
    }

    @PostMapping("/my/delete")
    public ResponseEntity<BaseResponse<UserDeleteDTO>> myDelete(@RequestHeader ("X-USERS-IDX") Long userIdx) {
        return ResponseEntity.ok(new BaseResponse<>("SUCCESS", sellerService.myDelete(userIdx)));
    }

    @DeleteMapping("/my/delete")
    public ResponseEntity<BaseResponse<String>> deleteUser(@RequestHeader ("X-USERS-IDX") Long userIdx,
                                                           @RequestBody @Valid SellerDeleteSecondStepReq sellerDeleteSecondStepReq) {
        return ResponseEntity.ok(new BaseResponse<>("SUCCESS", sellerService.myDelete(userIdx, sellerDeleteSecondStepReq.getAuthCode())));
    }
}