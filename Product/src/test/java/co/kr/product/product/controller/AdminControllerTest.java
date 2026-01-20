package co.kr.product.product.controller;

import co.kr.product.product.dto.request.UpsertProductRequest;
import co.kr.product.product.dto.response.ProductDetailResponse;
import co.kr.product.product.dto.response.ProductListResponse;
import co.kr.product.product.dto.response.ProductResponse;
import co.kr.product.product.dto.vo.ProductStatus;
import co.kr.product.product.service.ProductManagerService;
import co.kr.product.product.service.ProductSearchService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@WebMvcTest(AdminController.class)
class AdminControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    @MockitoBean ProductManagerService productManagerService;
    @MockitoBean ProductSearchService productSearchService;

    Long adminUserIdx = 999L;
    String productCode1 = UUID.randomUUID().toString();

    /**
     * ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ
     * Given 데이터 추가
     * ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ
     */

    // 목록 조회용 Response
    ProductResponse productRes1 = new ProductResponse(
            1L, productCode1, "관리자용 상품 A",
            new BigDecimal("10000.00"), new BigDecimal("9000.00"), 50L
    );

    // 상세 조회 및 수정 결과용 Response
    ProductDetailResponse detailRes1 = new ProductDetailResponse(
            "ok",
            1L, productCode1, "관리자용 상품 A", "관리자만 볼 수 있는 상세",
            new BigDecimal("10000.00"), new BigDecimal("9000.00"), 50L,
            100, ProductStatus.ON_SALE,
            Collections.emptyList(), Collections.emptyList()
    );

    // 상품 수정 요청 DTO
    UpsertProductRequest updateReq = new UpsertProductRequest(
            1L, "수정된 상품명", "수정된 설명",
            new BigDecimal("20000.00"), new BigDecimal("18000.00"),
            200, ProductStatus.STOPPED,
            Collections.emptyList(), Collections.emptyList()
    );

    /**
     * ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ
     * Test Cases
     * ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ
     */

    @Test
    void 관리자_상품_목록_조회() throws Exception {
        // Given
        ProductListResponse fakeResponse = new ProductListResponse("ok", List.of(productRes1));
        given(productSearchService.getProductsList(any(Pageable.class), anyString())).willReturn(fakeResponse);

        // When
        ResultActions resultActions = mvc.perform(
                get("/admin/products")
                        .param("page", "0")
                        .param("size", "10")
                        .param("search", "상품")
                        .accept(MediaType.APPLICATION_JSON)
        ).andDo(print());

        // Then
        resultActions.andExpect(status().isOk())
                .andExpect(handler().handlerType(AdminController.class))
                .andExpect(handler().methodName("getProductList"))
                .andExpect(jsonPath("$.resultCode").value("ok"))
                .andExpect(jsonPath("$.items[0].productsCode").value(productCode1))
                .andExpect(jsonPath("$.items[0].name").value("관리자용 상품 A"))
                .andExpect(jsonPath("$.items[0].price").value(10000.00));
    }

    @Test
    void 관리자_상품_상세_조회() throws Exception {
        // Given
        given(productManagerService.getManagerProductDetail(eq(adminUserIdx), eq(productCode1)))
                .willReturn(detailRes1);

        // When
        ResultActions resultActions = mvc.perform(
                get("/admin/products/{code}", productCode1)
                        .header("X-USERS-IDX", String.valueOf(adminUserIdx))
                        .accept(MediaType.APPLICATION_JSON)
        ).andDo(print());

        // Then
        resultActions.andExpect(status().isOk())
                .andExpect(handler().handlerType(AdminController.class))
                .andExpect(handler().methodName("getProductDetail"))
                .andExpect(jsonPath("$.productsCode").value(productCode1))
                .andExpect(jsonPath("$.status").value("ON_SALE"))
                .andExpect(jsonPath("$.stock").value(100));
    }

    @Test
    void 관리자_상품_수정() throws Exception {
        // Given
        // 수정 후 반환될 응답 (예시로 detailRes1 재사용하되 이름만 변경되었다고 가정 가능)
        ProductDetailResponse updatedRes = new ProductDetailResponse(
                "ok", 1L, productCode1, "수정된 상품명", "수정된 설명",
                new BigDecimal("20000.00"), new BigDecimal("18000.00"), 50L,
                200, ProductStatus.STOPPED,
                Collections.emptyList(), Collections.emptyList()
        );

        given(productManagerService.updateProduct(eq(adminUserIdx), eq(productCode1), any(UpsertProductRequest.class)))
                .willReturn(updatedRes);

        // When
        ResultActions resultActions = mvc.perform(
                put("/admin/products/{code}", productCode1)
                        .header("X-USERS-IDX", String.valueOf(adminUserIdx))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(updateReq))
                        .accept(MediaType.APPLICATION_JSON)
        ).andDo(print());

        // Then
        resultActions.andExpect(status().isOk())
                .andExpect(handler().handlerType(AdminController.class))
                .andExpect(handler().methodName("updateProduct"))
                .andExpect(jsonPath("$.name").value("수정된 상품명"))
                .andExpect(jsonPath("$.status").value("STOPPED"));
    }

    @Test
    void 관리자_상품_삭제() throws Exception {
        // Given (Void 메서드라 given 불필요, 호출 여부만 확인)

        // When
        ResultActions resultActions = mvc.perform(
                delete("/admin/products/{code}", productCode1)
                        .header("X-USERS-IDX", String.valueOf(adminUserIdx))
        ).andDo(print());

        // Then
        resultActions.andExpect(status().isOk())
                .andExpect(handler().handlerType(AdminController.class))
                .andExpect(handler().methodName("deleteProduct"))
                .andExpect(jsonPath("$.resultCode").value("ok"));

        verify(productManagerService).deleteProduct(eq(adminUserIdx), eq(productCode1));
    }
}