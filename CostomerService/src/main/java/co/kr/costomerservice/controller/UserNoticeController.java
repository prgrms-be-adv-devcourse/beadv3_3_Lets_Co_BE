package co.kr.costomerservice.controller;


import co.kr.costomerservice.model.dto.response.NoticeDetailResponse;
import co.kr.costomerservice.model.dto.response.NoticeListResponse;
import co.kr.costomerservice.service.UserNoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/notice")
public class UserNoticeController {

    private final UserNoticeService userNoticeService;

    @GetMapping
    public ResponseEntity<NoticeListResponse> getNoticeList(
            @PageableDefault Pageable pageable
            ){
        return ResponseEntity.ok(userNoticeService.getNoticeList(pageable));
    }

    @GetMapping("/{noticeCode}")
    public ResponseEntity<NoticeDetailResponse> getNoticeDetail(
        @PathVariable("noticeCode") String noticeCode
    ){
        return ResponseEntity.ok(userNoticeService.getNoticeDetail(noticeCode));
    }
}
