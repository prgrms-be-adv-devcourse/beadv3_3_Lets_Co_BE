package co.kr.costomerservice.inquiryAdmin.controller;

import co.kr.costomerservice.common.model.dto.response.ResultResponse;
import co.kr.costomerservice.inquiryAdmin.model.dto.request.InquiryAnswerDeleteRequest;
import co.kr.costomerservice.inquiryAdmin.model.dto.request.InquiryAnswerUpsertRequest;
import co.kr.costomerservice.inquiryAdmin.model.dto.request.InquiryUpsertRequest;
import co.kr.costomerservice.inquiryAdmin.model.dto.response.InquiryDetailResponse;
import co.kr.costomerservice.inquiryAdmin.model.dto.response.InquiryListResponse;
import co.kr.costomerservice.inquiryAdmin.service.InquiryAdminManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/inquiry")
// 관리자가 문의를 관리하는 모든 기능
public class InquiryAdminManagementController {
    private final InquiryAdminManagementService inquiryAdminManagementService;


/*문의 목록 조회
[문의 답변 등록]
[문의 답변 수정]
[문의 답변 삭제]*/

    /**
     * 문의 목록 조회 (관리자이기에 private까지 다 보임)
     * @param pageable
     * @return 문의 목록
     */
    @GetMapping
    public ResponseEntity<InquiryListResponse> getInquiryList(
            @RequestHeader("X-USERS-IDX") Long usersIdx,
            @PageableDefault Pageable pageable
    ){
        return ResponseEntity.ok(inquiryAdminManagementService.getInquiryList(pageable,usersIdx));
    }

    /**
     * 문의 답변 추가
     * @param inquiryCode
     * @param request
     * @return 뮨의 상세 내용
     */
    @PostMapping("/{inquiryCode}")
    public ResponseEntity<InquiryDetailResponse> addInquiryAnswer(
            @RequestHeader("X-USERS-IDX") Long usersIdx,
            @PathVariable("inquiryCode") String inquiryCode,
            @RequestBody  InquiryAnswerUpsertRequest request
    ){

        return ResponseEntity.ok(inquiryAdminManagementService.addInquiryAnswer(usersIdx, inquiryCode , request));

    }


    /**
     * 문의 대답 삭제
     * @param inquiryCode
     * @param request
     * @return resultCode
     */
    @DeleteMapping("/answer/{inquiryCode}")
    public ResponseEntity<ResultResponse> deleteInquiryAnswer(
            @RequestHeader("X-USERS-IDX") Long usersIdx,
            @PathVariable("inquiryCode") String inquiryCode,
            @RequestBody InquiryAnswerDeleteRequest request
            ){

        return ResponseEntity.ok(inquiryAdminManagementService.deleteInquiryAnswer(inquiryCode, request, usersIdx));
    }
    /**
     * 문의 수정 (실제로 관리자가 사용 할 일이 있을지는 모르겠지만 권한은 필요하다 판단)
     * + 대답 수정
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

        return ResponseEntity.ok(inquiryAdminManagementService.updateInquiry(inquiryCode ,request, usersIdx));
    }

    /**
     * 문의 삭제 (실제로 관리자가 사용 할 일이 있을지는 모르겠지만 권한은 필요하다 판단)
     * @param inquiryCode
     * @return resultCode
     */
    @DeleteMapping("/{inquiryCode}")
    public ResponseEntity<ResultResponse> deleteInquiry(
            @RequestHeader("X-USERS-IDX") Long usersIdx,
            @PathVariable("inquiryCode") String inquiryCode
    ){

        return ResponseEntity.ok(inquiryAdminManagementService.deleteInquiry(inquiryCode, usersIdx));
    }
}
