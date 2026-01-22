package co.kr.product.product.controller;

import co.kr.product.product.dto.response.ProductDetailResponse;
import co.kr.product.product.dto.response.ProductListResponse;
import co.kr.product.product.dto.response.ProductOptionsResponse;
import co.kr.product.product.dto.response.ProductResponse;
import co.kr.product.product.dto.vo.ProductStatus;
import co.kr.product.product.service.ProductSearchService;
import co.kr.product.product.service.ProductService;
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
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    @MockitoBean ProductService productService;
    @MockitoBean ProductSearchService productSearchService;

    String searchKeyword = "test";
    String productCode1 = UUID.randomUUID().toString();
    String productCode2 = UUID.randomUUID().toString();

    /**
     * ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ
     * Given 데이터 추가
     * ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ
     */

    // Given
    ProductResponse product1 = new ProductResponse(
            1L,
            productCode1,
            "삼성 노트북",
            new BigDecimal("1000.00"),
            new BigDecimal("990.00"),
            200L
    );

    ProductOptionsResponse option1_1 = new ProductOptionsResponse(
            1L,
            UUID.randomUUID().toString(),
            "노트북-노랑",
            2,
            new BigDecimal("1000.00"),
            new BigDecimal("990.00"),
            100,
            ProductStatus.ON_SALE.name()
    );

    ProductOptionsResponse option1_2 = new ProductOptionsResponse(
            2L,
            UUID.randomUUID().toString(),
            "노트북-파랑",
            1,
            new BigDecimal("1000.00"),
            new BigDecimal("990.00"),
            50,
            ProductStatus.ON_SALE.name()
    );

    ProductResponse product2 = new ProductResponse(
            2L,
            productCode2,
            "맥북",
            new BigDecimal("2000.00"),
            new BigDecimal("1980.00"),
            100L
    );

    ProductOptionsResponse option2_1 = new ProductOptionsResponse(
            20L,
            UUID.randomUUID().toString(),
            "스페이스 그레이 / M3 Pro",
            1,
            new BigDecimal("2000.00"),
            new BigDecimal("1980.00"),
            50,
            ProductStatus.ON_SALE.name()
    );

    ProductResponse product3 = new ProductResponse(
            3L,
            UUID.randomUUID().toString(),
            "삼성 핸드폰",
            new BigDecimal("3000.00"),
            new BigDecimal("2970.00"),
            120L
    );

    ProductOptionsResponse option3_1 = new ProductOptionsResponse(
            30L,
            UUID.randomUUID().toString(),
            "팬텀 블랙 / 512GB",
            1,
            new BigDecimal("3000.00"),
            new BigDecimal("2970.00"),
            30,
            ProductStatus.ON_SALE.name()
    );

    // ---------------------------------------------------------
    // Case 4: 품절된 상품 (SOLD_OUT)
    // ---------------------------------------------------------
    ProductResponse product4 = new ProductResponse(
            4L,
            UUID.randomUUID().toString(),
            "LG 그램 2025",
            new BigDecimal("1500000.00"),
            new BigDecimal("1450000.00"),
            500L // 조회수
    );

    // 재고 0개, 상태 SOLD_OUT
    ProductOptionsResponse option4_1 = new ProductOptionsResponse(
            10L,
            UUID.randomUUID().toString(),
            "16인치-화이트",
            1, // 정렬순서
            new BigDecimal("1500000.00"),
            new BigDecimal("1450000.00"),
            0, // 재고 0
            ProductStatus.SOLD_OUT.name()
    );

    // ---------------------------------------------------------
    // Case 5: 판매자가 판매 중지한 상품 (STOPPED)
    // ---------------------------------------------------------
    ProductResponse product5 = new ProductResponse(
            5L,
            UUID.randomUUID().toString(),
            "게이밍 의자",
            new BigDecimal("350000.00"),
            new BigDecimal("300000.00"),
            10L
    );

    ProductOptionsResponse option5_1 = new ProductOptionsResponse(
            11L,
            UUID.randomUUID().toString(),
            "레드-가죽",
            1,
            new BigDecimal("350000.00"),
            new BigDecimal("300000.00"),
            10, // 재고는 있지만 판매 중지 상태
            ProductStatus.STOPPED.name()
    );

    // ---------------------------------------------------------
    // Case 6: 관리자에 의해 차단된 상품 (BLOCKED)
    // ---------------------------------------------------------
    ProductResponse product6 = new ProductResponse(
            6L,
            UUID.randomUUID().toString(),
            "위험한 레이저 포인터",
            new BigDecimal("5000.00"),
            new BigDecimal("5000.00"),
            1200L
    );

    ProductOptionsResponse option6_1 = new ProductOptionsResponse(
            12L,
            UUID.randomUUID().toString(),
            "기본",
            1,
            new BigDecimal("5000.00"),
            new BigDecimal("5000.00"),
            100,
            ProductStatus.BLOCKED.name()
    );

    // ---------------------------------------------------------
    // Case 7: 옵션이 여러 개인 베스트 셀러
    // ---------------------------------------------------------
    ProductResponse product7 = new ProductResponse(
            7L,
            UUID.randomUUID().toString(),
            "아이폰 15 Pro",
            new BigDecimal("1500000.00"),
            new BigDecimal("1400000.00"),
            9999L // 높은 조회수
    );

    ProductOptionsResponse option7_1 = new ProductOptionsResponse(
            13L,
            UUID.randomUUID().toString(),
            "내추럴 티타늄-256GB",
            1,
            new BigDecimal("1500000.00"),
            new BigDecimal("1400000.00"),
            50,
            ProductStatus.ON_SALE.name()
    );

    ProductOptionsResponse option7_2 = new ProductOptionsResponse(
            14L,
            UUID.randomUUID().toString(),
            "블루 티타늄-512GB",
            2,
            new BigDecimal("1800000.00"),
            new BigDecimal("1700000.00"),
            30,
            ProductStatus.ON_SALE.name()
    );

    ProductOptionsResponse option7_3 = new ProductOptionsResponse(
            15L,
            UUID.randomUUID().toString(),
            "화이트 티타늄-1TB",
            3,
            new BigDecimal("2200000.00"),
            new BigDecimal("2100000.00"),
            0, // 인기 많아서 품절된 옵션
            ProductStatus.SOLD_OUT.name()
    );

    /**
     * ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ
     * Given 데이터 끝
     * ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ
     */

    @Test
    void 상품_목록_조회 () throws Exception {

        ProductListResponse fakeResponse = new ProductListResponse(
                "ok",
                List.of(product1, product2, product3, product5, product6, product7)
        );

        given(productSearchService.getProductsList(any(Pageable.class), eq(searchKeyword))).willReturn(fakeResponse);

        ResultActions resultActions = mvc
                .perform(
                    get("/products")
                            .param("search", searchKeyword)
                            .param("page", "0")
                            .param("size", "20")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                ).andDo(print());

        resultActions.andExpect(status().isOk())
                .andExpect(handler().handlerType(ProductController.class))
                .andExpect(handler().methodName("getProducts"))
                .andExpect(jsonPath("$.resultCode").value("ok"))
                .andExpect(jsonPath("$.items[0].productsIdx").value(1L))
                .andExpect(jsonPath("$.items[0].productsCode").value(notNullValue()))
                .andExpect(jsonPath("$.items[0].name").value("삼성 노트북"))
                .andExpect(jsonPath("$.items[0].price").value(new BigDecimal("1000.0")))
                .andExpect(jsonPath("$.items[0].salePrice").value(new BigDecimal("990.0")))
                .andExpect(jsonPath("$.items[0].viewCount").value(200))

                .andExpect(jsonPath("$.items[1].productsIdx").value(2L))
                .andExpect(jsonPath("$.items[1].productsCode").value(notNullValue()))
                .andExpect(jsonPath("$.items[1].name").value("맥북"))
                .andExpect(jsonPath("$.items[1].price").value(new BigDecimal("2000.0")))
                .andExpect(jsonPath("$.items[1].salePrice").value(new BigDecimal("1980.0")))
                .andExpect(jsonPath("$.items[1].viewCount").value(100))

                .andExpect(jsonPath("$.items[2].productsIdx").value(3L))
                .andExpect(jsonPath("$.items[2].productsCode").value(notNullValue()))
                .andExpect(jsonPath("$.items[2].name").value("삼성 핸드폰"))
                .andExpect(jsonPath("$.items[2].price").value(new BigDecimal("3000.0")))
                .andExpect(jsonPath("$.items[2].salePrice").value(new BigDecimal("2970.0")))
                .andExpect(jsonPath("$.items[2].viewCount").value(120));
    }

    @Test
    void 상품_상세_조회 () throws Exception {

        ProductOptionsResponse optionsResponse1 = new ProductOptionsResponse(
                option1_1.optionGroupIdx(),
                option1_1.code(),
                option1_1.name(),
                option1_1.sortOrder(),
                option1_1.price(),
                option1_1.salePrice(),
                option1_1.stock(),
                option1_1.status()
        );

        ProductOptionsResponse optionsResponse2 = new ProductOptionsResponse(
                option1_2.optionGroupIdx(),
                option1_2.code(),
                option1_2.name(),
                option1_2.sortOrder(),
                option1_2.price(),
                option1_2.salePrice(),
                option1_2.stock(),
                option1_2.status()
        );

        ProductDetailResponse fakeResponse = new ProductDetailResponse(
                "ok",
                product1.productsIdx(),
                product1.productsCode(),
                product1.name(),
                "상세설명1",
                product1.price(),
                product1.salePrice(),
                product1.viewCount(),
                null,
                null,
                List.of(optionsResponse1, optionsResponse2),
                null
        );

        given(productService.getProductDetail(product1.productsCode())).willReturn(fakeResponse);

        ResultActions resultActions = mvc
                .perform(
                        get("/products/" + productCode1)
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                ).andDo(print());

        resultActions.andExpect(status().isOk())
                .andExpect(handler().handlerType(ProductController.class))
                .andExpect(handler().methodName("getProductDetail"))
                .andExpect(jsonPath("$.resultCode").value("ok"))
                .andExpect(jsonPath("$.productsIDX").value(1L))
                .andExpect(jsonPath("$.productsCode").value(productCode1))
                .andExpect(jsonPath("$.name").value("삼성 노트북"))
                .andExpect(jsonPath("$.description").value("상세설명1"))
                .andExpect(jsonPath("$.price").value(new BigDecimal("1000.0")))
                .andExpect(jsonPath("$.salePrice").value(new BigDecimal("990.0")))
                .andExpect(jsonPath("$.viewCount").value(200))
                .andExpect(jsonPath("$.stock").value(nullValue()))
                .andExpect(jsonPath("$.status").value(nullValue()))
                .andExpect(jsonPath("$.options[0].optionGroupIdx").value(1L))
                .andExpect(jsonPath("$.options[0].code").value(option1_1.code()))
                .andExpect(jsonPath("$.options[0].name").value("노트북-노랑"))
                .andExpect(jsonPath("$.options[0].sortOrder").value(2))
                .andExpect(jsonPath("$.options[0].price").value(new BigDecimal("1000.0")))
                .andExpect(jsonPath("$.options[0].salePrice").value(new BigDecimal("990.0")))
                .andExpect(jsonPath("$.options[0].status").value("ON_SALE"))
                .andExpect(jsonPath("$.options[1].optionGroupIdx").value(2L));
    }
}
