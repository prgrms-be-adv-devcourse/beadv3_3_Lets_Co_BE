package co.kr.customerservice.notice.controller;


import co.kr.customerservice.common.model.dto.response.ResultResponse;
import co.kr.customerservice.notice.model.dto.request.NoticeUpsertReq;
import co.kr.customerservice.notice.model.dto.response.AdminNoticeDetailRes;
import co.kr.customerservice.notice.model.dto.response.NoticeListRes;
import co.kr.customerservice.notice.service.AdminNoticeService;
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
    public ResponseEntity<AdminNoticeDetailRes> addNotice(
            @RequestHeader("X-USERS-IDX") Long usersIdx,
            @RequestBody NoticeUpsertReq request
    ){

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
    public ResponseEntity<NoticeListRes> getNoticeList(
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
    public ResponseEntity<AdminNoticeDetailRes> getNoticeDetail(
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
    public ResponseEntity<AdminNoticeDetailRes> updateNotice(
            @RequestHeader("X-USERS-IDX") Long usersIdx,
            @PathVariable("noticeCode") String noticeCode,
            @RequestBody NoticeUpsertReq request
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
