package co.kr.costomerservice.notice.controller;


import co.kr.costomerservice.notice.model.dto.request.NoticeUpsertRequest;
import co.kr.costomerservice.notice.model.dto.response.AdminNoticeDetailResponse;
import co.kr.costomerservice.notice.model.dto.response.NoticeListResponse;
import co.kr.costomerservice.common.dto.response.ResultResponse;
import co.kr.costomerservice.notice.service.AdminNoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/notice")
public class AdminNoticeController {

    private final AdminNoticeService adminNoticeService;

    /**
     * 공지 추가
     * @param request
     * @return 공지 상세 정보
     */
    @PostMapping
    public ResponseEntity<AdminNoticeDetailResponse> addNotice(
            @RequestHeader("X-USERS-IDX") Long usersIdx,
            @RequestBody NoticeUpsertRequest request){
        return ResponseEntity.ok(

                adminNoticeService.addNotice(usersIdx,request)
        );
    }

    /**
     * 공지 리스트 조회
     * @param usersIdx
     * @param pageable
     * @return 공지 리스트
     */
    @GetMapping
    public ResponseEntity<NoticeListResponse> getNoticeList(
            @RequestHeader("X-USERS-IDX") Long usersIdx,
            @PageableDefault Pageable pageable){

        return ResponseEntity.ok(
                adminNoticeService.getNoticeList(usersIdx,pageable)
        );
    }

    /**
     * 공지 상세 조회
     * @param usersIdx
     * @param noticeCode
     * @return 공지 상세 정보
     */
    @GetMapping("/{noticeCode}")
    public ResponseEntity<AdminNoticeDetailResponse> getNoticeDetail(
            @RequestHeader("X-USERS-IDX") Long usersIdx,
            @PathVariable("noticeCode") String noticeCode
    ){

        return ResponseEntity.ok(
                adminNoticeService.getNoticeDetail(usersIdx,noticeCode )
        );
    }

    /**
     * 공지 수정
     * @param usersIdx
     * @param noticeCode
     * @param request
     * @return 공지 상세 정보
     */
    @PutMapping("/{noticeCode}")
    public ResponseEntity<AdminNoticeDetailResponse> updateNotice(
            @RequestHeader("X-USERS-IDX") Long usersIdx,
            @PathVariable("noticeCode") String noticeCode,
            @RequestBody NoticeUpsertRequest request
    ){

        return ResponseEntity.ok(
                adminNoticeService.updateNotice(usersIdx,noticeCode,request )
        );
    }

    /**
     * 공지 삭제
     * @param usersIdx
     * @param noticeCode
     * @return resultCode
     */
    @DeleteMapping("/{noticeCode}")
    public ResponseEntity<ResultResponse> deleteNotice(
            @RequestHeader("X-USERS-IDX") Long usersIdx,
            @PathVariable("noticeCode") String noticeCode
    )
    {
        return ResponseEntity.ok(
                adminNoticeService.deleteNotice(usersIdx,noticeCode)
        );
    }

}
