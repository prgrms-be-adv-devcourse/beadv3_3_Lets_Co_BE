package co.kr.costomerservice.inquiryAdmin.controller;

import co.kr.costomerservice.common.dto.response.ResultResponse;
import co.kr.costomerservice.common.vo.CustomerServiceCategory;
import co.kr.costomerservice.common.vo.CustomerServiceStatus;
import co.kr.costomerservice.inquiryAdmin.dto.InquiryDTO;
import co.kr.costomerservice.inquiryAdmin.dto.InquiryDetailDTO;
import co.kr.costomerservice.inquiryAdmin.dto.request.InquiryAnswerDeleteRequest;
import co.kr.costomerservice.inquiryAdmin.dto.request.InquiryAnswerUpsertRequest;
import co.kr.costomerservice.inquiryAdmin.dto.request.InquiryUpsertRequest;
import co.kr.costomerservice.inquiryAdmin.dto.response.InquiryDetailResponse;
import co.kr.costomerservice.inquiryAdmin.dto.response.InquiryListResponse;
import co.kr.costomerservice.inquiryAdmin.service.InquiryAdminManagementService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InquiryAdminManagementController.class)
class InquiryAdminManagementControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    @MockitoBean InquiryAdminManagementService managementService;

    // 공통 사용 변수
    Long adminIdx = 99L;
    String inquiryCode = "INQ-001";

    @Test
    @DisplayName("관리자 문의 목록 조회")
    void getInquiryList() throws Exception {
        // Given
        InquiryDTO inquiry1 = createInquiryDTO("INQ-001", "첫 번째 문의");
        InquiryDTO inquiry2 = createInquiryDTO("INQ-002", "두 번째 문의");

        // 빈 리스트 대신 데이터가 2개 들어있는 리스트 반환
        InquiryListResponse response = new InquiryListResponse("success", List.of(inquiry1, inquiry2));

        given(managementService.getInquiryList(any(Pageable.class), eq(adminIdx)))
                .willReturn(response);

        // When & Then
        mvc.perform(get("/admin/inquiry")
                        .header("X-USERS-IDX", adminIdx)
                        .param("page", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("success"))
                // 리스트 검증 추가
                .andExpect(jsonPath("$.list").isArray())
                .andExpect(jsonPath("$.list.length()").value(2))
                .andExpect(jsonPath("$.list[0].code").value("INQ-001"))
                .andExpect(jsonPath("$.list[1].title").value("두 번째 문의"));
    }

    @Test
    @DisplayName("관리자 문의 답변 등록")
    void addInquiryAnswer() throws Exception {
        // Given
        InquiryAnswerUpsertRequest request = new InquiryAnswerUpsertRequest("DTL-NEW", "새로운 답변 내용입니다.");

        InquiryDTO info = createInquiryDTO(inquiryCode, "답변이 달린 문의");
        InquiryDetailDTO detail = createDetailDTO("DTL-NEW", "새로운 답변 내용입니다.");

        // Response에 실제 문의 정보와 추가된 답변 포함
        InquiryDetailResponse response = new InquiryDetailResponse("success", true, info, List.of(detail));

        given(managementService.addInquiryAnswer(eq(adminIdx), eq(inquiryCode), any(InquiryAnswerUpsertRequest.class)))
                .willReturn(response);

        // When & Then
        mvc.perform(post("/admin/inquiry/{inquiryCode}", inquiryCode)
                        .header("X-USERS-IDX", adminIdx)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("success"))
                // 응답 데이터 검증 추가
                .andExpect(jsonPath("$.info.code").value(inquiryCode))
                .andExpect(jsonPath("$.details[0].content").value("새로운 답변 내용입니다."));
    }

    @Test
    @DisplayName("관리자 문의 답변 삭제")
    void deleteInquiryAnswer() throws Exception {
        // Given
        InquiryAnswerDeleteRequest request = new InquiryAnswerDeleteRequest("DTL-001");
        ResultResponse response = new ResultResponse("success");

        given(managementService.deleteInquiryAnswer(eq(inquiryCode), any(InquiryAnswerDeleteRequest.class), eq(adminIdx)))
                .willReturn(response);

        // When & Then
        mvc.perform(delete("/admin/inquiry/answer/{inquiryCode}", inquiryCode)
                        .header("X-USERS-IDX", adminIdx)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("success"));
    }

    @Test
    @DisplayName("관리자 문의 수정 (답변 수정 포함)")
    void updateInquiry() throws Exception {
        // Given
        InquiryUpsertRequest request = new InquiryUpsertRequest(
                "DTL-001",
                CustomerServiceCategory.ACCOUNT,
                "수정된 제목",
                "수정된 내용",
                false
        );

        InquiryDTO updatedInfo = createInquiryDTO(inquiryCode, "수정된 제목"); // 제목이 수정된 상태 가정
        InquiryDetailDTO updatedDetail = createDetailDTO("DTL-001", "수정된 답변 내용"); // 답변도 수정됐을 수 있음

        // 수정된 결과를 담은 Response
        InquiryDetailResponse response = new InquiryDetailResponse("success", true, updatedInfo, List.of(updatedDetail));

        given(managementService.updateInquiry(eq(inquiryCode), any(InquiryUpsertRequest.class), eq(adminIdx)))
                .willReturn(response);

        // When & Then
        mvc.perform(put("/admin/inquiry/{inquiryCode}", inquiryCode)
                        .header("X-USERS-IDX", adminIdx)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("success"))
                // 수정된 데이터 검증
                .andExpect(jsonPath("$.info.title").value("수정된 제목"))
                .andExpect(jsonPath("$.details[0].detailCode").value("DTL-001"));
    }

    @Test
    @DisplayName("관리자 문의 삭제")
    void deleteInquiry() throws Exception {
        // Given
        ResultResponse response = new ResultResponse("success");

        given(managementService.deleteInquiry(eq(inquiryCode), eq(adminIdx)))
                .willReturn(response);

        // When & Then
        mvc.perform(delete("/admin/inquiry/{inquiryCode}", inquiryCode)
                        .header("X-USERS-IDX", adminIdx))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("success"));

        verify(managementService).deleteInquiry(eq(inquiryCode), eq(adminIdx));
    }

    // --- Helper Methods ---

    private InquiryDTO createInquiryDTO(String code, String title) {
        // InquiryDTO의 생성자 구조에 맞춰서 작성 (필드 순서는 실제 DTO에 따름)
        return new InquiryDTO(
                code,
                CustomerServiceCategory.ACCOUNT,
                CustomerServiceStatus.WAITING,
                title,
                true,
                LocalDateTime.now()
        );
    }

    private InquiryDetailDTO createDetailDTO(String detailCode, String content) {
        return new InquiryDetailDTO(
                detailCode,
                content,
                LocalDateTime.now()
        );
    }
}