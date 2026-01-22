package co.kr.costomerservice.notice.controller;

import co.kr.costomerservice.common.dto.response.ResultResponse;
import co.kr.costomerservice.common.vo.CustomerServiceCategory;
import co.kr.costomerservice.common.vo.CustomerServiceStatus;
import co.kr.costomerservice.notice.model.dto.request.NoticeUpsertRequest;
import co.kr.costomerservice.notice.model.dto.response.AdminNoticeDetailResponse;
import co.kr.costomerservice.notice.model.dto.response.NoticeListResponse;
import co.kr.costomerservice.notice.model.dto.response.NoticeResponse;
import co.kr.costomerservice.notice.service.AdminNoticeService;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminNoticeController.class)
class AdminNoticeControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    @MockitoBean AdminNoticeService adminNoticeService;

    // 공통 테스트 데이터
    final Long ADMIN_IDX = 99L;
    final String NOTICE_CODE = "NOTICE-001";

    @Test
    @DisplayName("공지 추가 (성공)")
    void addNotice() throws Exception {
        // Given
        NoticeUpsertRequest request = createRequest("공지사항 제목입니다.", "공지사항 내용입니다.");

        // 생성된 결과로 돌아올 응답 객체
        AdminNoticeDetailResponse response = createDetailResponse(NOTICE_CODE, "공지사항 제목입니다.");

        given(adminNoticeService.addNotice(eq(ADMIN_IDX), any(NoticeUpsertRequest.class)))
                .willReturn(response);

        // When & Then
        mvc.perform(post("/admin/notice")
                        .header("X-USERS-IDX", ADMIN_IDX)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("success"))
                .andExpect(jsonPath("$.csCode").value(NOTICE_CODE))
                .andExpect(jsonPath("$.title").value("공지사항 제목입니다.")); // 제목 검증

        verify(adminNoticeService).addNotice(eq(ADMIN_IDX), any(NoticeUpsertRequest.class));
    }

    @Test
    @DisplayName("공지 리스트 조회 (성공)")
    void getNoticeList() throws Exception {
        // Given
        NoticeResponse notice1 = createNoticeResponse(1L, "NOTICE-001", "첫 번째 공지");
        NoticeResponse notice2 = createNoticeResponse(2L, "NOTICE-002", "두 번째 공지");

        // 빈 리스트 대신 데이터가 2개 포함된 리스트 반환
        NoticeListResponse response = new NoticeListResponse("success", List.of(notice1, notice2));

        given(adminNoticeService.getNoticeList(eq(ADMIN_IDX), any(Pageable.class)))
                .willReturn(response);

        // When & Then
        mvc.perform(get("/admin/notice")
                        .header("X-USERS-IDX", ADMIN_IDX)
                        .param("page", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("success"))
                // 리스트 데이터 검증
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.items[0].Code").value("NOTICE-001"))
                .andExpect(jsonPath("$.items[1].title").value("두 번째 공지"));

        verify(adminNoticeService).getNoticeList(eq(ADMIN_IDX), any(Pageable.class));
    }

    @Test
    @DisplayName("공지 상세 조회 (성공)")
    void getNoticeDetail() throws Exception {
        // Given
        AdminNoticeDetailResponse response = createDetailResponse(NOTICE_CODE, "상세 조회 제목");

        given(adminNoticeService.getNoticeDetail(eq(ADMIN_IDX), eq(NOTICE_CODE)))
                .willReturn(response);

        // When & Then
        mvc.perform(get("/admin/notice/{noticeCode}", NOTICE_CODE)
                        .header("X-USERS-IDX", ADMIN_IDX)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("success"))
                .andExpect(jsonPath("$.csCode").value(NOTICE_CODE))
                .andExpect(jsonPath("$.title").value("상세 조회 제목"))
                .andExpect(jsonPath("$.category").value("PAYMENT")); // 카테고리 검증

        verify(adminNoticeService).getNoticeDetail(eq(ADMIN_IDX), eq(NOTICE_CODE));
    }

    @Test
    @DisplayName("공지 수정 (성공)")
    void updateNotice() throws Exception {
        // Given
        NoticeUpsertRequest request = createRequest("수정된 제목", "수정된 내용");

        // 수정 완료 후 리턴될 응답 (제목이 수정됨)
        AdminNoticeDetailResponse response = createDetailResponse(NOTICE_CODE, "수정된 제목");

        given(adminNoticeService.updateNotice(eq(ADMIN_IDX), eq(NOTICE_CODE), any(NoticeUpsertRequest.class)))
                .willReturn(response);

        // When & Then
        mvc.perform(put("/admin/notice/{noticeCode}", NOTICE_CODE)
                        .header("X-USERS-IDX", ADMIN_IDX)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("success"))
                .andExpect(jsonPath("$.title").value("수정된 제목")); // 수정된 값 확인

        verify(adminNoticeService).updateNotice(eq(ADMIN_IDX), eq(NOTICE_CODE), any(NoticeUpsertRequest.class));
    }

    @Test
    @DisplayName("공지 삭제 (성공)")
    void deleteNotice() throws Exception {
        // Given
        ResultResponse response = new ResultResponse("success");

        given(adminNoticeService.deleteNotice(eq(ADMIN_IDX), eq(NOTICE_CODE)))
                .willReturn(response);

        // When & Then
        mvc.perform(delete("/admin/notice/{noticeCode}", NOTICE_CODE)
                        .header("X-USERS-IDX", ADMIN_IDX))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("success"));

        verify(adminNoticeService).deleteNotice(eq(ADMIN_IDX), eq(NOTICE_CODE));
    }

    // --- Helper Methods ---

    private NoticeUpsertRequest createRequest(String title, String content) {
        return new NoticeUpsertRequest(
                CustomerServiceCategory.ACCOUNT,
                CustomerServiceStatus.ANSWERED,
                title,
                content,
                false, // isPrivate
                true,  // isPinned
                LocalDateTime.now()
        );
    }

    private AdminNoticeDetailResponse createDetailResponse(String code, String title) {
        return new AdminNoticeDetailResponse(
                "success",
                code,
                "DETAIL-001",
                CustomerServiceCategory.PAYMENT,
                CustomerServiceStatus.ANSWERED,
                title,
                "공지사항 내용입니다.",
                0L,
                true,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    private NoticeResponse createNoticeResponse(Long idx, String code, String title) {
        return new NoticeResponse(
                idx,
                code,
                CustomerServiceCategory.ACCOUNT,
                title,
                CustomerServiceStatus.ANSWERED,
                LocalDateTime.now(),
                10L,    // viewCount
                false,  // isPrivate
                false,  // isPined (User record definition check)
                LocalDateTime.now()
        );
    }
}