package co.kr.costomerservice.notice.controller;


import co.kr.costomerservice.notice.model.dto.request.NoticeUpsertRequest;
import co.kr.costomerservice.notice.model.dto.response.AdminNoticeDetailResponse;
import co.kr.costomerservice.notice.model.dto.response.NoticeListResponse;
import co.kr.costomerservice.common.response.ResultResponse;
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

            @RequestBody NoticeUpsertRequest request){
        // 임시. 확인용
        Long userId =1L;
        return ResponseEntity.ok(

                adminNoticeService.addNotice(userId,request)
        );
    }

    /**
     * 공지 리스트 조회
     * @param userId
     * @param pageable
     * @return 공지 리스트
     */
    @GetMapping
    public ResponseEntity<NoticeListResponse> getNoticeList(
            // 임시. 확인용
            Long userId,
            @PageableDefault Pageable pageable){

        return ResponseEntity.ok(
                adminNoticeService.getNoticeList(userId,pageable)
        );
    }

    /**
     * 공지 상세 조회
     * @param userId
     * @param noticeCode
     * @return 공지 상세 정보
     */
    @GetMapping("/{noticeCode}")
    public ResponseEntity<AdminNoticeDetailResponse> getNoticeDetail(
            // 임시. 확인용
            Long userId ,
            @PathVariable("noticeCode") String noticeCode
    ){

        return ResponseEntity.ok(
                adminNoticeService.getNoticeDetail(userId,noticeCode )
        );
    }

    /**
     * 공지 수정
     * @param userId
     * @param noticeCode
     * @param request
     * @return 공지 상세 정보
     */
    @PutMapping("/{noticeCode}")
    public ResponseEntity<AdminNoticeDetailResponse> updateNotice(
            // 임시. 확인용
            Long userId ,
            @PathVariable("noticeCode") String noticeCode,
            @RequestBody NoticeUpsertRequest request
    ){

        return ResponseEntity.ok(
                adminNoticeService.updateNotice(userId,noticeCode,request )
        );
    }

    /**
     * 공지 삭제
     * @param userId
     * @param noticeCode
     * @return resultCode
     */
    @DeleteMapping("/{noticeCode}")
    public ResponseEntity<ResultResponse> deleteNotice(
            // 임시. 확인용
            Long userId ,
            @PathVariable("noticeCode") String noticeCode
    )
    {
        return ResponseEntity.ok(
                adminNoticeService.deleteNotice(userId,noticeCode)
        );
    }

}
