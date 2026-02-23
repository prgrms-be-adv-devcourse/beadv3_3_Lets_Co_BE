package co.kr.user.controller;

import co.kr.user.model.dto.admin.SellerDeleteSecondStepReq;
import co.kr.user.model.dto.my.UserDeleteDTO;
import co.kr.user.model.dto.seller.*;
import co.kr.user.service.SellerService;
import co.kr.user.util.BaseResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 판매자(Seller) 관련 기능을 제공하는 REST 컨트롤러입니다.
 * 판매자 등록 신청, 승인 확인, 프로필 조회/수정/탈퇴 기능을 처리합니다.
 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/seller/users") // 판매자 사용자 관련 경로 설정
public class SellerController {
    private final SellerService sellerService;

    /**
     * 판매자 등록(입점)을 신청합니다.
     * @param userIdx 요청 헤더(X-USERS-IDX)에서 추출한 사용자 식별자
     * @param sellerRegisterReq 판매자 등록 정보(상점명, 사업자번호 등)
     * @return 등록 신청 결과 DTO (인증 메일 정보 등)
     */
    @PostMapping("/register")
    public ResponseEntity<BaseResponse<SellerRegisterDTO>> sellerRegister(@RequestHeader("X-USERS-IDX") Long userIdx,
                                                                          @RequestBody @Valid SellerRegisterReq sellerRegisterReq) {
        return ResponseEntity.ok(new BaseResponse<>("SUCCESS", sellerService.sellerRegister(userIdx, sellerRegisterReq)));
    }

    /**
     * 판매자 등록을 위한 이메일 인증 코드를 확인하고 승인을 완료합니다.
     * @param userIdx 요청 헤더에서 추출한 사용자 식별자
     * @param sellerAuthenticationReq 인증 코드 정보
     * @return 처리 성공 메시지
     */
    @PostMapping("/register/check")
    public ResponseEntity<BaseResponse<String>> sellerRegisterCheck(@RequestHeader("X-USERS-IDX") Long userIdx,
                                                                    @RequestBody @Valid SellerAuthenticationReq sellerAuthenticationReq) {
        return ResponseEntity.ok(new BaseResponse<>("SUCCESS", sellerService.sellerRegisterCheck(userIdx, sellerAuthenticationReq.getAuthCode())));
    }

    /**
     * 현재 로그인한 판매자의 프로필 정보를 조회합니다.
     * @param userIdx 요청 헤더에서 추출한 판매자 식별자
     * @return 판매자 프로필 정보 DTO
     */
    @PostMapping("/my")
    public ResponseEntity<BaseResponse<SellerProfileDTO>> my(@RequestHeader ("X-USERS-IDX") Long userIdx) {
        return ResponseEntity.ok(new BaseResponse<>("SUCCESS", sellerService.my(userIdx)));
    }

    /**
     * 판매자 정보를 수정합니다.
     * @param userIdx 요청 헤더에서 추출한 판매자 식별자
     * @param sellerAmendReq 수정할 판매자 정보 (상점명, 계좌 등)
     * @return 처리 성공 메시지
     */
    @PutMapping("/my")
    public ResponseEntity<BaseResponse<String>> sellerAmend(@RequestHeader ("X-USERS-IDX") Long userIdx,
                                                            @RequestBody SellerAmendReq sellerAmendReq) {
        return ResponseEntity.ok(new BaseResponse<>("SUCCESS", sellerService.myAmend(userIdx, sellerAmendReq)));
    }

    /**
     * [판매자 탈퇴 1단계]
     * 판매자 탈퇴를 위한 인증 메일을 요청합니다.
     * @param userIdx 요청 헤더에서 추출한 판매자 식별자
     * @return 탈퇴 요청 결과 DTO (인증 메일 정보)
     */
    @PostMapping("/my/delete")
    public ResponseEntity<BaseResponse<UserDeleteDTO>> myDelete(@RequestHeader ("X-USERS-IDX") Long userIdx) {
        return ResponseEntity.ok(new BaseResponse<>("SUCCESS", sellerService.myDelete(userIdx)));
    }

    /**
     * [판매자 탈퇴 2단계]
     * 인증 코드를 검증하고 판매자 자격을 삭제합니다.
     * @param userIdx 요청 헤더에서 추출한 판매자 식별자
     * @param sellerDeleteSecondStepReq 인증 코드 정보
     * @return 처리 성공 메시지
     */
    @DeleteMapping("/my/delete")
    public ResponseEntity<BaseResponse<String>> deleteUser(@RequestHeader ("X-USERS-IDX") Long userIdx,
                                                           @RequestBody @Valid SellerDeleteSecondStepReq sellerDeleteSecondStepReq) {
        return ResponseEntity.ok(new BaseResponse<>("SUCCESS", sellerService.myDelete(userIdx, sellerDeleteSecondStepReq.getAuthCode())));
    }

    /**
     * [PUT] 판매자 본인 프로필 이미지 등록 및 수정
     * 등록과 수정을 PUT 메서드로 통합하여 처리합니다.
     */
    @PutMapping(value = "/my/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BaseResponse<String>> updateProfileImage(
            @RequestHeader("X-USERS-IDX") Long userIdx,
            @RequestParam("file") MultipartFile file) throws IOException {

        String resultMessage = sellerService.updateProfileImage(userIdx, file);
        return ResponseEntity.ok(new BaseResponse<>("SUCCESS", resultMessage));
    }

    /**
     * [POST] 판매자 본인 프로필 이미지 조회
     * 본인의 이미지를 조회할 때도 POST 메서드를 사용합니다.
     */
    @PostMapping("/my/profile-image")
    public ResponseEntity<BaseResponse<String>> getMyProfileImage(
            @RequestHeader("X-USERS-IDX") Long userIdx) {

        String imageUrl = sellerService.getMyProfileImage(userIdx);
        return ResponseEntity.ok(new BaseResponse<>("SUCCESS", imageUrl));
    }
}