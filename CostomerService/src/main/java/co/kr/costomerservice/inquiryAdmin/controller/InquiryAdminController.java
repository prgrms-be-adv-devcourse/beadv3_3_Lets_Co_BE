package co.kr.costomerservice.inquiryAdmin.controller;


import co.kr.costomerservice.common.response.ResultResponse;
import co.kr.costomerservice.inquiryAdmin.dto.request.InquiryUpsertRequest;
import co.kr.costomerservice.inquiryAdmin.dto.response.InquiryDetailResponse;
import co.kr.costomerservice.inquiryAdmin.dto.response.InquiryListResponse;
import co.kr.costomerservice.inquiryAdmin.service.InquiryAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/inquiry")
// 유저가 관리자에게 남기는 문의 관련 기능
public class InquiryAdminController {

    private final InquiryAdminService inquiryAdminService;

    /**
     * 전체 문의 목록 조회(비밀 x )
     * @param pageable
     * @return 문의 목록
     */
    @GetMapping
    public ResponseEntity<InquiryListResponse> getInquiryList(
            @PageableDefault Pageable pageable
            ){

        return ResponseEntity.ok(inquiryAdminService.getInquiryList(pageable));
    }

    /**
     * 문의 추가
     * @param request
     * @return 문의 상세 내용
     */
    @PostMapping
    public ResponseEntity<InquiryDetailResponse> addInquiry(
            @RequestHeader("X-USERS-IDX") Long usersIdx,
            @RequestBody InquiryUpsertRequest request
    ){
        return ResponseEntity.ok(inquiryAdminService.addInquiry(usersIdx, request));
    }

    /**
     * 문의 상세 내용 조회
     * @param inquiryCode
     * @return 문의 상세 내용
     */
    @GetMapping("/{inquiryCode}")
    public ResponseEntity<InquiryDetailResponse> getInquiryDetail(
            @RequestHeader("X-USERS-IDX") Long usersIdx,
            @PathVariable("inquiryCode") String inquiryCode
    ){

        return ResponseEntity.ok(inquiryAdminService.getInquiryDetail(usersIdx, inquiryCode));

    }

    /**
     * 문의 수정
     * @param inquiryCode
     * @param request
     * @return 문의 상세 내용
     */
    @PutMapping("/{inquiryCode}")
    public ResponseEntity<InquiryDetailResponse> updateInquiry(
            @RequestHeader("X-USERS-IDX") Long usersIdx,
            @PathVariable("inquiryCode") String inquiryCode,
            @RequestBody  InquiryUpsertRequest request
    ){
        return ResponseEntity.ok(inquiryAdminService.updateInquiry(usersIdx, inquiryCode ,request));
    }

    /**
     * 문의 삭제
     * @param inquiryCode
     * @return resultCode
     */
    @DeleteMapping("/{inquiryCode}")
    public ResponseEntity<ResultResponse> deleteInquiry(
            @RequestHeader("X-USERS-IDX") Long usersIdx,
            @PathVariable("inquiryCode") String inquiryCode
    ){
        return ResponseEntity.ok(inquiryAdminService.deleteInquiry(usersIdx,inquiryCode));
    }


    /**
     * 본인의 문의 목록 조회
     * @param usersIdx
     * @param pageable
     * @return
     */
    @GetMapping("/me")
    public ResponseEntity<InquiryListResponse> getMyInquiryList(
            @RequestHeader("X-USERS-IDX") Long usersIdx,
            @PageableDefault Pageable pageable
    ){

        return ResponseEntity.ok(inquiryAdminService.getMyInquiryList(usersIdx,pageable));

    }

}
