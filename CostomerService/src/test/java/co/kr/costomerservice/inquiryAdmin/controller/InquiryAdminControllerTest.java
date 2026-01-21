package co.kr.costomerservice.inquiryAdmin.controller;

import co.kr.costomerservice.common.dto.response.ResultResponse;
import co.kr.costomerservice.common.vo.CustomerServiceCategory;
import co.kr.costomerservice.common.vo.CustomerServiceStatus;
import co.kr.costomerservice.inquiryAdmin.dto.InquiryDTO;
import co.kr.costomerservice.inquiryAdmin.dto.InquiryDetailDTO;
import co.kr.costomerservice.inquiryAdmin.dto.request.InquiryUpsertRequest;
import co.kr.costomerservice.inquiryAdmin.dto.response.InquiryDetailResponse;
import co.kr.costomerservice.inquiryAdmin.dto.response.InquiryListResponse;
import co.kr.costomerservice.inquiryAdmin.service.InquiryAdminService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@WebMvcTest(InquiryAdminController.class)
class InquiryAdminControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    @MockitoBean InquiryAdminService inquiryAdminService;

    Long userIdx = 1L;
    String inquiryCode = "INQ-001";

    InquiryUpsertRequest createRequest() {
        return new InquiryUpsertRequest(
                null,
                CustomerServiceCategory.ACCOUNT,
                "계정 관련 문의드립니다.",
                "로그인이 안되는데 확인 부탁드립니다.",
                true
        );
    }

    InquiryDTO createInquiryDTO() {
        return new InquiryDTO(
                inquiryCode,
                CustomerServiceCategory.ACCOUNT,
                CustomerServiceStatus.WAITING,
                "문의 제목입니다.",
                true,
                LocalDateTime.now()
        );
    }

    InquiryDetailDTO createInquiryDetailDTO() {
        return new InquiryDetailDTO(
                "DTL-001",
                "문의 내용 상세입니다.",
                LocalDateTime.now()
        );
    }

    @Test
    @DisplayName("전체 문의 목록 조회 (성공)")
    void getInquiryList() throws Exception {
        // Given
        List<InquiryDTO> dtoList = List.of(createInquiryDTO());
        InquiryListResponse response = new InquiryListResponse("success", dtoList);

        given(inquiryAdminService.getInquiryList(any(Pageable.class)))
                .willReturn(response);

        // When & Then
        mvc.perform(get("/inquiry")
                        .param("page", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("success"))
                .andExpect(jsonPath("$.list[0].code").value(inquiryCode));

        verify(inquiryAdminService).getInquiryList(any(Pageable.class));
    }

    @Test
    @DisplayName("문의 등록 (성공)")
    void addInquiry() throws Exception {
        // Given
        InquiryUpsertRequest request = createRequest();
        InquiryDTO newInquiry = createInquiryDTO();
        InquiryDetailDTO detailDTO = createInquiryDetailDTO();

        InquiryDetailResponse response = new InquiryDetailResponse("success", true, newInquiry, List.of(detailDTO));

        given(inquiryAdminService.addInquiry(eq(userIdx), any(InquiryUpsertRequest.class)))
                .willReturn(response);

        // When & Then
        mvc.perform(post("/inquiry")
                        .header("X-USERS-IDX", userIdx)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("success"))
                .andExpect(jsonPath("$.info.code").value(inquiryCode));

        verify(inquiryAdminService).addInquiry(eq(userIdx), any(InquiryUpsertRequest.class));
    }

    @Test
    @DisplayName("문의 상세 조회 (성공)")
    void getInquiryDetail() throws Exception {
        // Given
        InquiryDTO infoDTO = createInquiryDTO();
        InquiryDetailDTO detailDTO = createInquiryDetailDTO();

        InquiryDetailResponse response = new InquiryDetailResponse("success", true, infoDTO, List.of(detailDTO));

        given(inquiryAdminService.getInquiryDetail(eq(userIdx), eq(inquiryCode)))
                .willReturn(response);

        // When & Then
        mvc.perform(get("/inquiry/{inquiryCode}", inquiryCode)
                        .header("X-USERS-IDX", userIdx)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("success"))
                .andExpect(jsonPath("$.isOwner").value(true))
                .andExpect(jsonPath("$.info.code").value(inquiryCode))
                .andExpect(jsonPath("$.info.title").value("문의 제목입니다."))
                .andExpect(jsonPath("$.details[0].detailCode").value("DTL-001"))
                .andExpect(jsonPath("$.details[0].content").value("문의 내용 상세입니다."));

        verify(inquiryAdminService).getInquiryDetail(eq(userIdx), eq(inquiryCode));
    }

    @Test
    @DisplayName("문의 수정 (성공)")
    void updateInquiry() throws Exception {
        // Given
        InquiryUpsertRequest request = createRequest();
        InquiryDTO updatedDTO = createInquiryDTO();
        InquiryDetailDTO detailDTO = createInquiryDetailDTO();

        InquiryDetailResponse response = new InquiryDetailResponse("success", true, updatedDTO, List.of(detailDTO));

        given(inquiryAdminService.updateInquiry(eq(userIdx), eq(inquiryCode), any(InquiryUpsertRequest.class)))
                .willReturn(response);

        // When & Then
        mvc.perform(put("/inquiry/{inquiryCode}", inquiryCode)
                        .header("X-USERS-IDX", userIdx)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("success"))
                .andExpect(jsonPath("$.info.code").value(inquiryCode));

        verify(inquiryAdminService).updateInquiry(eq(userIdx), eq(inquiryCode), any(InquiryUpsertRequest.class));
    }

    @Test
    @DisplayName("문의 삭제 (성공)")
    void deleteInquiry() throws Exception {
        ResultResponse response = new ResultResponse("success");

        given(inquiryAdminService.deleteInquiry(eq(userIdx), eq(inquiryCode)))
                .willReturn(response);

        // When & Then
        mvc.perform(delete("/inquiry/{inquiryCode}", inquiryCode)
                        .header("X-USERS-IDX", userIdx))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("success"));

        verify(inquiryAdminService).deleteInquiry(eq(userIdx), eq(inquiryCode));
    }

    @Test
    @DisplayName("본인의 문의 목록 조회 (성공)")
    void getMyInquiryList() throws Exception {
        // Given
        List<InquiryDTO> dtoList = List.of(createInquiryDTO());
        InquiryListResponse response = new InquiryListResponse("success", dtoList);

        given(inquiryAdminService.getMyInquiryList(eq(userIdx), any(Pageable.class)))
                .willReturn(response);

        // When & Then
        mvc.perform(get("/inquiry/me")
                        .header("X-USERS-IDX", userIdx)
                        .param("page", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("success"))
                .andExpect(jsonPath("$.list[0].code").value(inquiryCode));

        verify(inquiryAdminService).getMyInquiryList(eq(userIdx), any(Pageable.class));
    }
}