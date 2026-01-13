package co.kr.costomerservice.controller;


import co.kr.costomerservice.model.dto.request.NoticeUpsertRequest;
import co.kr.costomerservice.model.dto.response.AdminNoticeDetailResponse;
import co.kr.costomerservice.model.dto.response.NoticeListResponse;
import co.kr.costomerservice.model.dto.response.ResultResponse;
import co.kr.costomerservice.service.AdminNoticeService;
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

    @PostMapping
    public ResponseEntity<AdminNoticeDetailResponse> addNotice(

            @RequestBody NoticeUpsertRequest request){
        // 임시. 확인용
        Long userId =1L;
        return ResponseEntity.ok(

                adminNoticeService.addNotice(userId,request)
        );
    }

    @GetMapping
    public ResponseEntity<NoticeListResponse> getNoticeList(
            // 임시. 확인용
            Long userId,
            @PageableDefault Pageable pageable){

        return ResponseEntity.ok(
                adminNoticeService.getNoticeList(userId,pageable)
        );
    }

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
