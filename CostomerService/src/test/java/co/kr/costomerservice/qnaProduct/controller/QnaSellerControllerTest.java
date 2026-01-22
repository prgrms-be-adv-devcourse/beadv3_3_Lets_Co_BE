package co.kr.costomerservice.qnaProduct.controller;

import co.kr.costomerservice.common.vo.CustomerServiceCategory;
import co.kr.costomerservice.common.vo.CustomerServiceStatus;
import co.kr.costomerservice.qnaProduct.model.QnaProductDetailDTO;
import co.kr.costomerservice.qnaProduct.model.QnaProductQuestionDTO;
import co.kr.costomerservice.qnaProduct.model.request.QnaAnswerUpsertRequest;
import co.kr.costomerservice.qnaProduct.model.response.QnaAndProductInfoListResponse;
import co.kr.costomerservice.qnaProduct.model.response.QnaAndProductInfoResponse;
import co.kr.costomerservice.qnaProduct.model.response.QnaProductDetailResponse;
import co.kr.costomerservice.qnaProduct.service.QnaSellerService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(QnaSellerController.class)
class QnaSellerControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    @MockitoBean QnaSellerService qnaSellerService;

    // 공통 상수
    final Long SELLER_IDX = 10L;
    final String QNA_CODE = "QNA-001";

    @Test
    @DisplayName("판매자 내 상품 문의 목록 조회 (성공)")
    void getMyQnaList() throws Exception {
        // Given
        QnaAndProductInfoResponse item1 = createListResponse("QNA-001", "배송 언제 되나요?", "맛있는 사과");
        QnaAndProductInfoResponse item2 = createListResponse("QNA-002", "재입고 문의", "싱싱한 배");

        // 실제 데이터가 담긴 리스트 반환
        QnaAndProductInfoListResponse response =
                new QnaAndProductInfoListResponse("success", List.of(item1, item2));

        given(qnaSellerService.getMyQnaList(eq(SELLER_IDX), any(Pageable.class)))
                .willReturn(response);

        // When & Then
        mvc.perform(get("/seller/product_qna")
                        .header("X-USERS-IDX", SELLER_IDX)
                        .param("page", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("success"))
                // 리스트 및 상품 정보 검증
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items[0].title").value("배송 언제 되나요?"))
                .andExpect(jsonPath("$.items[0].name").value("맛있는 사과")) // 상품명 확인
                .andExpect(jsonPath("$.items[1].name").value("싱싱한 배"));

        verify(qnaSellerService).getMyQnaList(eq(SELLER_IDX), any(Pageable.class));
    }

    @Test
    @DisplayName("판매자 답변 등록 (성공)")
    void addAnswer() throws Exception {
        // Given
        QnaAnswerUpsertRequest request = new QnaAnswerUpsertRequest(
                "판매자",
                "DETAIL-001",
                "답변 내용입니다."
        );

        // 답변이 등록된 상태의 DetailDTO 생성
        QnaProductDetailDTO answer = createAnswerDTO("DTL-NEW", "답변 내용입니다.");

        // 생성된 답변을 포함한 상세 응답
        QnaProductDetailResponse response = createDetailResponse(List.of(answer));

        given(qnaSellerService.addAnswer(eq(QNA_CODE), eq(SELLER_IDX), any(QnaAnswerUpsertRequest.class)))
                .willReturn(response);

        // When & Then
        mvc.perform(post("/seller/product_qna/{qnaCode}", QNA_CODE)
                        .header("X-USERS-IDX", SELLER_IDX)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("success"))
                .andExpect(jsonPath("$.questionDTO.status").value("ANSWERED"))
                // 등록된 답변이 반환되는지 확인
                .andExpect(jsonPath("$.answerDTOs[0].content").value("답변 내용입니다."))
                .andExpect(jsonPath("$.answerDTOs[0].userName").value("판매자"));

        verify(qnaSellerService).addAnswer(eq(QNA_CODE), eq(SELLER_IDX), any(QnaAnswerUpsertRequest.class));
    }

    // --- Helper Methods ---

    // 목록 조회용 아이템 생성 (상품 정보 포함)
    private QnaAndProductInfoResponse createListResponse(String code, String title, String productName) {
        return new QnaAndProductInfoResponse(
                code,
                CustomerServiceCategory.PRODUCT,
                CustomerServiceStatus.WAITING,
                title,
                0L,
                LocalDateTime.now(),
                "구매자",
                // 상품 정보
                "PROD-100",
                productName,
                "http://img.url/sample.jpg"
        );
    }

    // 상세 조회용 답변 DTO 생성
    private QnaProductDetailDTO createAnswerDTO(String detailCode, String content) {
        return new QnaProductDetailDTO(
                detailCode,
                QNA_CODE,   // parentCode
                content,
                "판매자",    // userName
                LocalDateTime.now()
        );
    }

    // 상세 응답 객체 생성 (답변 리스트 수신)
    private QnaProductDetailResponse createDetailResponse(List<QnaProductDetailDTO> answers) {
        QnaProductQuestionDTO question = new QnaProductQuestionDTO(
                QNA_CODE,
                CustomerServiceCategory.PRODUCT,
                CustomerServiceStatus.ANSWERED,
                "문의 제목",
                "구매자",
                0L,
                LocalDateTime.now(),
                false,
                1L,
                100L
        );

        return new QnaProductDetailResponse(
                "success",
                question,
                answers
        );
    }
}