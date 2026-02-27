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
 * 입점한 판매자(Seller)의 정보를 관리하는 컨트롤러입니다.
 * 판매자 입점 신청, 프로필 관리, 자격 탈퇴(Soft Delete) 기능을 포함합니다.
 */
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/seller/users")
public class SellerController {
    private final SellerService sellerService;

    /**
     * 일반 사용자가 판매자로 입점하기 위해 사업자 정보를 입력하고 등록을 신청합니다.
     *
     * @param userIdx 게이트웨이에서 전달받은 현재 로그인 사용자 식별자
     * @param sellerRegisterReq 사업자 번호, 상점명, 정산 계좌 등 등록 정보
     * @return 등록 신청 완료 메시지 및 인증 메일 발송 정보
     */
    @PostMapping("/register")
    public ResponseEntity<BaseResponse<SellerRegisterDTO>> sellerRegister(@RequestHeader("X-USERS-IDX") Long userIdx,
                                                                          @RequestBody @Valid SellerRegisterReq sellerRegisterReq) {
        return ResponseEntity.ok(new BaseResponse<>("SUCCESS", sellerService.sellerRegister(userIdx, sellerRegisterReq)));
    }

    /**
     * 판매자 등록 신청 후 발송된 이메일의 인증 코드를 확인하여 최종 입점 승인을 완료합니다.
     *
     * @param userIdx 사용자 고유 식별자
     * @param sellerAuthenticationReq 사용자가 입력한 인증 코드
     * @return 최종 승인 성공 여부 메시지
     */
    @PostMapping("/register/check")
    public ResponseEntity<BaseResponse<String>> sellerRegisterCheck(@RequestHeader("X-USERS-IDX") Long userIdx,
                                                                    @RequestBody @Valid SellerAuthenticationReq sellerAuthenticationReq) {
        return ResponseEntity.ok(new BaseResponse<>("SUCCESS", sellerService.sellerRegisterCheck(userIdx, sellerAuthenticationReq.getAuthCode())));
    }

    /**
     * 판매자 본인의 현재 상점 및 사업자 정보를 조회합니다.
     *
     * @param userIdx 사용자 고유 식별자
     * @return 상점명, 사업자번호, 정산 계좌 정보 등이 포함된 프로필 DTO
     */
    @PostMapping("/my")
    public ResponseEntity<BaseResponse<SellerProfileDTO>> my(@RequestHeader ("X-USERS-IDX") Long userIdx) {
        return ResponseEntity.ok(new BaseResponse<>("SUCCESS", sellerService.my(userIdx)));
    }

    /**
     * 판매자의 상점 정보 또는 정산 계좌 정보를 수정합니다.
     *
     * @param userIdx 사용자 고유 식별자
     * @param sellerAmendReq 변경하고자 하는 상점명, 은행, 계좌 토큰 등
     * @return 정보 수정 성공 메시지
     */
    @PutMapping("/my")
    public ResponseEntity<BaseResponse<String>> sellerAmend(@RequestHeader ("X-USERS-IDX") Long userIdx,
                                                            @RequestBody SellerAmendReq sellerAmendReq) {
        return ResponseEntity.ok(new BaseResponse<>("SUCCESS", sellerService.myAmend(userIdx, sellerAmendReq)));
    }

    /**
     * 판매자 권한 자진 탈퇴를 위한 1단계 요청(인증 메일 발송)을 수행합니다.
     *
     * @param userIdx 사용자 고유 식별자
     * @return 탈퇴 인증 코드 발송 결과
     */
    @PostMapping("/my/delete")
    public ResponseEntity<BaseResponse<UserDeleteDTO>> myDelete(@RequestHeader ("X-USERS-IDX") Long userIdx) {
        return ResponseEntity.ok(new BaseResponse<>("SUCCESS", sellerService.myDelete(userIdx)));
    }

    /**
     * 인증 코드를 확인하여 판매자 권한을 완전히 삭제(Soft Delete) 처리합니다.
     *
     * @param userIdx 사용자 고유 식별자
     * @param sellerDeleteSecondStepReq 사용자가 입력한 탈퇴 인증 코드
     * @return 판매자 자격 해지 성공 메시지
     */
    @DeleteMapping("/my/delete")
    public ResponseEntity<BaseResponse<String>> deleteUser(@RequestHeader ("X-USERS-IDX") Long userIdx,
                                                           @RequestBody @Valid SellerDeleteSecondStepReq sellerDeleteSecondStepReq) {
        return ResponseEntity.ok(new BaseResponse<>("SUCCESS", sellerService.myDelete(userIdx, sellerDeleteSecondStepReq.getAuthCode())));
    }

    /**
     * 판매자 상점의 프로필 이미지를 등록하거나 기존 이미지를 수정(교체)합니다.
     * Multipart 요청을 받아 S3 업로드 및 파일 정보 갱신을 수행합니다.
     *
     * @param userIdx 사용자 고유 식별자
     * @param file 업로드할 이미지 파일
     * @return 프로필 이미지 갱신 성공 메시지
     * @throws IOException 파일 처리 중 예외 발생 시
     */
    @PutMapping(value = "/my/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BaseResponse<String>> updateProfileImage(@RequestHeader("X-USERS-IDX") Long userIdx,
                                                                   @RequestParam("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(new BaseResponse<>("SUCCESS", sellerService.updateProfileImage(userIdx, file)));
    }

    /**
     * 판매자 본인의 현재 프로필 이미지 정보를 조회합니다.
     *
     * @param userIdx 사용자 고유 식별자
     * @return 프로필 이미지의 S3 접근 가능 주소(URL)
     */
    @PostMapping("/my/profile-image")
    public ResponseEntity<BaseResponse<String>> getMyProfileImage(@RequestHeader("X-USERS-IDX") Long userIdx) {
        return ResponseEntity.ok(new BaseResponse<>("SUCCESS", sellerService.getMyProfileImage(userIdx)));
    }
}