package co.kr.product.product.controller;

import co.kr.product.common.vo.UserRole;
import co.kr.product.product.model.dto.request.ProductListReq;
import co.kr.product.product.model.dto.request.UpsertProductReq;
import co.kr.product.product.model.dto.response.ProductDetailRes;
import co.kr.product.product.model.dto.response.ProductListRes;
import co.kr.product.product.model.dto.response.ProductRes;
import co.kr.product.product.model.vo.ProductStatus;
import co.kr.product.product.service.ProductManagerService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@WebMvcTest(SellerController.class)
class SellerControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    @MockitoBean ProductManagerService productManagerService;

    Long sellerUserIdx = 10L;
    String productCode1 = UUID.randomUUID().toString();

    /**
     * ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ
     * Given 데이터 추가
     * ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ
     */
    ProductRes product1 = new ProductRes(
            productCode1, "판매자 상품 1",
            new BigDecimal("50000.00"), new BigDecimal("45000.00"), 10L
    );

    UpsertProductReq createReq = new UpsertProductReq(
            "신규 등록 상품",
            "아주 좋은 상품입니다.",
            new BigDecimal("30000.00"),
            new BigDecimal("29000.00"),
            100,
            ProductStatus.ON_SALE,
            Collections.emptyList(),
            Collections.emptyList()
    );

    ProductDetailRes createRes = new ProductDetailRes("NEW_CODE_123", "신규 등록 상품", "아주 좋은 상품입니다.",
            new BigDecimal("30000.00"), new BigDecimal("29000.00"), 0L,
            100, ProductStatus.ON_SALE,
            Collections.emptyList()
    );

    /**
     * ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ
     * Test Cases
     * ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ
     */

    @Test
    void 판매자_상품_목록_조회() throws Exception {
        // Given
        ProductListRes fakeResponse = new ProductListRes( List.of(product1));
        given(productManagerService.getListsBySeller(eq(sellerUserIdx), any(Pageable.class), any(ProductListReq.class)))
                .willReturn(fakeResponse);

        // When
        ResultActions resultActions = mvc.perform(
                get("/seller/products")
                        .header("X-USERS-IDX", String.valueOf(sellerUserIdx))
                        .param("page", "0")
                        .param("size", "20")
                        .accept(MediaType.APPLICATION_JSON)
        ).andDo(print());

        // Then
        resultActions.andExpect(status().isOk())
                .andExpect(handler().handlerType(SellerController.class))
                .andExpect(handler().methodName("getLists"))
                .andExpect(jsonPath("$.items[0].name").value("판매자 상품 1"));
    }

    @Test
    void 판매자_상품_등록() throws Exception {
        // Given
        given(productManagerService.addProduct(eq(sellerUserIdx), any(UpsertProductReq.class)))
                .willReturn(createRes);

        // When
        ResultActions resultActions = mvc.perform(
                post("/seller/products")
                        .header("X-USERS-IDX", String.valueOf(sellerUserIdx))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(createReq))
                        .accept(MediaType.APPLICATION_JSON)
        ).andDo(print());

        // Then
        resultActions.andExpect(status().isOk())
                .andExpect(handler().handlerType(SellerController.class))
                .andExpect(handler().methodName("addProduct"))
                .andExpect(jsonPath("$.productsCode").value("NEW_CODE_123"))
                .andExpect(jsonPath("$.name").value("신규 등록 상품"))
                .andExpect(jsonPath("$.price").value(30000.00));
    }

    @Test
    void 판매자_상품_상세_조회() throws Exception {
        // Given
        // 기존 createRes를 재활용하여 상세 조회 결과로 사용
        given(productManagerService.getManagerProductDetail(eq(sellerUserIdx), eq("NEW_CODE_123")))
                .willReturn(createRes);

        // When
        ResultActions resultActions = mvc.perform(
                get("/seller/products/{code}", "NEW_CODE_123")
                        .header("X-USERS-IDX", String.valueOf(sellerUserIdx))
                        .accept(MediaType.APPLICATION_JSON)
        ).andDo(print());

        // Then
        resultActions.andExpect(status().isOk())
                .andExpect(handler().handlerType(SellerController.class))
                .andExpect(handler().methodName("getProductDetail"))
                .andExpect(jsonPath("$.productsCode").value("NEW_CODE_123"));
    }

    @Test
    void 판매자_상품_수정() throws Exception {
        // Given
        String targetCode = "NEW_CODE_123";
        given(productManagerService.updateProduct(eq(sellerUserIdx), eq(targetCode), any(UpsertProductReq.class), eq(UserRole.SELLER)))
                .willReturn(createRes); // 수정된 결과 리턴

        // When
        ResultActions resultActions = mvc.perform(
                put("/seller/products/{code}", targetCode)
                        .header("X-USERS-IDX", String.valueOf(sellerUserIdx))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(createReq)) // Request Body 필요
                        .accept(MediaType.APPLICATION_JSON)
        ).andDo(print());

        // Then
        resultActions.andExpect(status().isOk())
                .andExpect(handler().handlerType(SellerController.class))
                .andExpect(handler().methodName("updateProduct"))
                .andExpect(jsonPath("$.name").value("신규 등록 상품"));
    }

    @Test
    void 판매자_상품_삭제() throws Exception {
        // Given
        String targetCode = "DELETE_CODE";

        // When
        ResultActions resultActions = mvc.perform(
                delete("/seller/products/{code}", targetCode)
                        .header("X-USERS-IDX", String.valueOf(sellerUserIdx))
        ).andDo(print());

        // Then
        resultActions.andExpect(status().isOk())
                .andExpect(handler().handlerType(SellerController.class))
                .andExpect(handler().methodName("deleteProduct"))
                .andExpect(jsonPath("$.resultCode").value("ok"));

        verify(productManagerService).deleteProduct(eq(sellerUserIdx), eq(targetCode), eq(UserRole.SELLER));
    }
}