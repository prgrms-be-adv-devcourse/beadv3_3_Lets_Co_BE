package co.kr.costomerservice.qnaProduct.controller;

import co.kr.costomerservice.common.dto.response.ResultResponse;
import co.kr.costomerservice.common.vo.CustomerServiceCategory;
import co.kr.costomerservice.common.vo.CustomerServiceStatus;
import co.kr.costomerservice.qnaProduct.model.QnaProductDetailDTO;
import co.kr.costomerservice.qnaProduct.model.QnaProductQuestionDTO;
import co.kr.costomerservice.qnaProduct.model.request.QnaProductListRequest;
import co.kr.costomerservice.qnaProduct.model.request.QnaProductUpsertRequest;
import co.kr.costomerservice.qnaProduct.model.response.*;
import co.kr.costomerservice.qnaProduct.service.QnaProductService;
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
@WebMvcTest(QnaProductController.class)
class QnaProductControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    @MockitoBean QnaProductService qnaProductService;

    // 공통 상수
    final Long USER_IDX = 1L;
    final String PRODUCTS_CODE = "PROD-123";
    final String QNA_CODE = "QNA-001";
    final Long PRODUCTS_IDX = 100L;

    @Test
    @DisplayName("상품 QnA 리스트 조회")
    void getProductQnaList() throws Exception {
        // Given
        QnaProductListRequest request = new QnaProductListRequest(PRODUCTS_IDX);

        QnaProductResponse item1 = createProductQnaResponse("QNA-001", "배송 문의");
        QnaProductResponse item2 = createProductQnaResponse("QNA-002", "상품 상세 문의");

        QnaProductListResponse response = new QnaProductListResponse("success", List.of(item1, item2));

        given(qnaProductService.getProductQnaList(eq(PRODUCTS_IDX), any(Pageable.class)))
                .willReturn(response);

        // When & Then
        mvc.perform(get("/qna/products/{productsCode}", PRODUCTS_CODE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("success"))
                .andExpect(jsonPath("$.items.length()").value(2));

        verify(qnaProductService).getProductQnaList(eq(PRODUCTS_IDX), any(Pageable.class));
    }

    @Test
    @DisplayName("상품 QnA 상세 조회 (헤더 필수)")
    void getProductQnaDetail() throws Exception {
        // Given
        QnaProductDetailDTO answer = createAnswerDTO("DTL-001", "답변입니다.");
        QnaProductDetailResponse response = createDetailResponse(List.of(answer));

        given(qnaProductService.getProductQnaDetail(eq(QNA_CODE), eq(USER_IDX)))
                .willReturn(response);

        // When & Then
        mvc.perform(get("/qna/products/{productsCode}/{qnaCode}", PRODUCTS_CODE, QNA_CODE)
                        .header("X-USERS-IDX", USER_IDX)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("success"))
                .andExpect(jsonPath("$.questionDTO.title").value("문의 제목"))
                .andExpect(jsonPath("$.answerDTOs[0].content").value("답변입니다."));

        verify(qnaProductService).getProductQnaDetail(eq(QNA_CODE), eq(USER_IDX));
    }

    @Test
    @DisplayName("상품 QnA 등록")
    void addProductQna() throws Exception {
        // Given
        QnaProductDetailDTO answer = createAnswerDTO("DTL-001", "답변 내용입니다.");

        QnaProductUpsertRequest request = createUpsertRequest();
        QnaProductDetailResponse response = createDetailResponse(List.of(answer));

        given(qnaProductService.addProductQna(any(QnaProductUpsertRequest.class), eq(USER_IDX)))
                .willReturn(response);

        // When & Then
        mvc.perform(post("/qna/products/{productsCode}", PRODUCTS_CODE)
                        .header("X-USERS-IDX", USER_IDX)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("success"));

        verify(qnaProductService).addProductQna(any(QnaProductUpsertRequest.class), eq(USER_IDX));
    }

    @Test
    @DisplayName("상품 QnA 수정")
    void updateQna() throws Exception {
        // Given

        QnaProductDetailDTO answer = createAnswerDTO("DTL-001", "답변 내용입니다.");

        QnaProductUpsertRequest request = createUpsertRequest();
        QnaProductDetailResponse response = createDetailResponse(List.of(answer));

        given(qnaProductService.updateQna(eq(QNA_CODE), any(QnaProductUpsertRequest.class), eq(USER_IDX)))
                .willReturn(response);

        // When & Then
        mvc.perform(put("/qna/products/{productsCode}/{qnaCode}", PRODUCTS_CODE, QNA_CODE)
                        .header("X-USERS-IDX", USER_IDX)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("success"));

        verify(qnaProductService).updateQna(eq(QNA_CODE), any(QnaProductUpsertRequest.class), eq(USER_IDX));
    }

    @Test
    @DisplayName("상품 QnA 삭제")
    void deleteQna() throws Exception {
        // Given
        ResultResponse response = new ResultResponse("success");

        given(qnaProductService.deleteQna(eq(QNA_CODE), eq(USER_IDX)))
                .willReturn(response);

        // When & Then
        mvc.perform(delete("/qna/products/{productsCode}/{qnaCode}", PRODUCTS_CODE, QNA_CODE)
                        .header("X-USERS-IDX", USER_IDX))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("success"));

        verify(qnaProductService).deleteQna(eq(QNA_CODE), eq(USER_IDX));
    }

    @Test
    @DisplayName("내 QnA 목록 조회")
    void getMyProductQnaList() throws Exception {
        // Given
        // [수정] 이제 타입에 맞는 객체를 생성합니다.
        QnaAndProductInfoResponse myQna = createQnaAndProductInfoResponse("MY-001", "내가 남긴 문의");

        QnaAndProductInfoListResponse response = new QnaAndProductInfoListResponse("success", List.of(myQna));

        given(qnaProductService.getMyProductQnaList(eq(USER_IDX), any(Pageable.class)))
                .willReturn(response);

        // When & Then
        mvc.perform(get("/qna/products/me")
                        .header("X-USERS-IDX", USER_IDX)
                        .param("page", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("success"))
                // 리스트 내부 데이터 검증
                .andExpect(jsonPath("$.items[0].code").value("MY-001"))
                .andExpect(jsonPath("$.items[0].title").value("내가 남긴 문의"))
                .andExpect(jsonPath("$.items[0].name").value("맛있는 사과")); // 상품명 검증 추가

        verify(qnaProductService).getMyProductQnaList(eq(USER_IDX), any(Pageable.class));
    }

    // --- Helper Methods ---

    private QnaProductUpsertRequest createUpsertRequest() {
        return new QnaProductUpsertRequest(
                PRODUCTS_IDX,
                CustomerServiceCategory.PRODUCT,
                "문의 제목",
                true,
                null,
                "홍길동",
                "1234",
                "문의 내용입니다."
        );
    }

    private QnaProductResponse createProductQnaResponse(String code, String title) {
        return new QnaProductResponse(
                code,
                CustomerServiceCategory.PRODUCT,
                CustomerServiceStatus.WAITING,
                title,
                0L,
                LocalDateTime.now(),
                "홍길동"
        );
    }

    private QnaProductDetailDTO createAnswerDTO(String detailCode, String content) {
        return new QnaProductDetailDTO(
                detailCode,
                QNA_CODE,
                content,
                "관리자",
                LocalDateTime.now()
        );
    }

    private QnaProductDetailResponse createDetailResponse(List<QnaProductDetailDTO> answers) {
        QnaProductQuestionDTO question = new QnaProductQuestionDTO(
                QNA_CODE,
                CustomerServiceCategory.PRODUCT,
                CustomerServiceStatus.WAITING,
                "문의 제목",
                "홍길동",
                0L,
                LocalDateTime.now(),
                true,
                USER_IDX,
                PRODUCTS_IDX
        );

        return new QnaProductDetailResponse(
                "success",
                question,
                answers
        );
    }

    private QnaAndProductInfoResponse createQnaAndProductInfoResponse(String code, String title) {
        return new QnaAndProductInfoResponse(
                code,
                CustomerServiceCategory.PRODUCT,
                CustomerServiceStatus.WAITING,
                title,
                0L,
                LocalDateTime.now(),
                "홍길동",
                "PROD-999",
                "맛있는 사과",
                "http://img.url/1.jpg"
        );
    }
}