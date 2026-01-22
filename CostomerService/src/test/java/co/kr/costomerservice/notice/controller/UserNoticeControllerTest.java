package co.kr.costomerservice.notice.controller;

import co.kr.costomerservice.common.vo.CustomerServiceCategory;
import co.kr.costomerservice.common.vo.CustomerServiceStatus;
import co.kr.costomerservice.notice.model.dto.response.NoticeDetailResponse;
import co.kr.costomerservice.notice.model.dto.response.NoticeListResponse;
import co.kr.costomerservice.notice.model.dto.response.NoticeResponse;
import co.kr.costomerservice.notice.service.UserNoticeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserNoticeController.class)
class UserNoticeControllerTest {

    @Autowired MockMvc mvc;

    @MockitoBean UserNoticeService userNoticeService;

    // 공통 테스트 데이터
    final String NOTICE_CODE = "NOTICE-001";

    @Test
    @DisplayName("공지 목록 조회 (성공)")
    void getNoticeList() throws Exception {
        // Given
        NoticeResponse notice1 = createNoticeResponse(1L, "NOTICE-001", "시스템 점검 안내");
        NoticeResponse notice2 = createNoticeResponse(2L, "NOTICE-002", "이벤트 당첨자 발표");

        // 빈 리스트가 아닌 실제 데이터가 담긴 리스트 반환
        NoticeListResponse response = new NoticeListResponse("success", List.of(notice1, notice2));

        given(userNoticeService.getNoticeList(any(Pageable.class)))
                .willReturn(response);

        // When & Then
        mvc.perform(get("/notice")
                        .param("page", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("success"))
                // 리스트 검증
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items.length()").value(2))
                // DTO 필드명이 대문자로 시작하므로(Code, Idx) jsonPath도 대문자로 맞춰야 함
                .andExpect(jsonPath("$.items[0].Code").value("NOTICE-001"))
                .andExpect(jsonPath("$.items[0].title").value("시스템 점검 안내"))
                .andExpect(jsonPath("$.items[1].Code").value("NOTICE-002"))
                .andExpect(jsonPath("$.items[1].title").value("이벤트 당첨자 발표"));

        verify(userNoticeService).getNoticeList(any(Pageable.class));
    }

    @Test
    @DisplayName("공지 상세 조회 (성공)")
    void getNoticeDetail() throws Exception {
        // Given
        NoticeDetailResponse response = createDetailResponse();

        given(userNoticeService.getNoticeDetail(eq(NOTICE_CODE)))
                .willReturn(response);

        // When & Then
        mvc.perform(get("/notice/{noticeCode}", NOTICE_CODE)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("success"))
                .andExpect(jsonPath("$.title").value("공지 제목"))
                .andExpect(jsonPath("$.content").value("공지 내용"))
                .andExpect(jsonPath("$.viewCount").value(100));

        verify(userNoticeService).getNoticeDetail(eq(NOTICE_CODE));
    }

    // --- Helper Methods ---

    // 목록 조회용 Response 생성
    private NoticeResponse createNoticeResponse(Long idx, String code, String title) {
        return new NoticeResponse(
                idx,
                code,
                CustomerServiceCategory.ACCOUNT,
                title,
                CustomerServiceStatus.ANSWERED,
                LocalDateTime.now(),
                50L,   // viewCount
                false, // isPrivate
                false, // isPined
                LocalDateTime.now()
        );
    }

    // 상세 조회용 Response 생성
    private NoticeDetailResponse createDetailResponse() {
        return new NoticeDetailResponse(
                "success",
                CustomerServiceCategory.ACCOUNT,
                "공지 제목",
                "공지 내용",
                100L, // viewCount
                LocalDateTime.now(), // publishedAt
                LocalDateTime.now()  // updatedAt
        );
    }
}