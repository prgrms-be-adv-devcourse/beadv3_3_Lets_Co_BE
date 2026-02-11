package co.kr.order.controller;

import co.kr.order.HelperMethod;
import co.kr.order.model.dto.ProductInfo;
import co.kr.order.model.dto.request.ProductRequest;
import co.kr.order.model.entity.CartEntity;
import jakarta.transaction.Transactional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CartControllerTest extends HelperMethod {

    @BeforeEach
    void setUp() {
        // given:
        ProductInfo productInfo1 = createProductInfo(100L, 10L, 1L, "테스트 상품1", "옵션A", 10000, 100);
        ProductInfo productInfo2 = createProductInfo(101L, 11L, 2L, "테스트 상품2", "옵션B", 12000, 80);

        given(productClient.getProduct(100L, 10L)).willReturn(productInfo1);
        given(productClient.getProduct(101L, 11L)).willReturn(productInfo2);
        given(productClient.getProductList(any())).willReturn(List.of(productInfo1, productInfo2));

        createCart(1L, 100L, 10L, new BigDecimal("20000"), 2);
        createCart(1L, 101L, 11L, new BigDecimal("36000"), 3);

        em.flush();
        em.clear();
    }

    @Test
    @Transactional
    @DisplayName("장바구니 조회 - 정상")
    void 장바구니_리스트_조회() throws Exception {

        // when:
        ResultActions resultActions = mvc
                .perform(
                        get("/carts")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .header("X-USERS-IDX", "1")
                )
                .andDo(print());

        // then:
        resultActions
                .andExpect(status().isOk())
                .andExpect(handler().handlerType(CartController.class))
                .andExpect(jsonPath("$.resultCode").value("ok"))
                .andExpect(jsonPath("$.data.cartItemList[0].product.productName").value("테스트 상품1"))
                .andExpect(jsonPath("$.data.cartItemList[0].product.optionName").value("옵션A"))
                .andExpect(jsonPath("$.data.cartItemList[0].product.price").value("10000"))
                .andExpect(jsonPath("$.data.cartItemList[0].quantity").value(2))
                .andExpect(jsonPath("$.data.cartItemList[0].amount").value("20000"))

                .andExpect(jsonPath("$.data.cartItemList[1].product.productName").value("테스트 상품2"))
                .andExpect(jsonPath("$.data.cartItemList[1].product.optionName").value("옵션B"))
                .andExpect(jsonPath("$.data.cartItemList[1].product.price").value("12000"))
                .andExpect(jsonPath("$.data.cartItemList[1].quantity").value(3))
                .andExpect(jsonPath("$.data.cartItemList[1].amount").value("36000"));

        CartEntity entity = cartRepository.findCartEntity(1L, 100L, 10L).get();
        Assertions.assertThat(entity.getQuantity()).isEqualTo(2);
        Assertions.assertThat(entity.getPrice()).isEqualByComparingTo("20000");
    }

    @Test
    @Transactional
    @DisplayName("장바구니 제품 추가 - 정상(상품 목록에서 장바구니 추가를 눌렀을 경우)")
    void 장바구니_추가_1() throws Exception {

        ProductRequest request = new ProductRequest(102L, 12L);

        ProductInfo mockProduct3 = new ProductInfo(102L, 12L, 3L,"테스트 상품3","옵션C", new BigDecimal("13000.00"), 10);
        given(productClient.getProduct(102L, 12L)).willReturn(mockProduct3);

        // when
        ResultActions resultActions = mvc
                .perform(
                        post("/carts/add")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .header("X-USERS-IDX", "1")
                                .content(om.writeValueAsString(request))
                )
                .andDo(print());

        resultActions
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.resultCode").value("ok"))
                .andExpect(jsonPath("$.data.product.productName").value("테스트 상품3"))
                .andExpect(jsonPath("$.data.product.price").value("13000.0"))
                .andExpect(jsonPath("$.data.quantity").value(1))
                .andExpect(jsonPath("$.data.amount").value("13000.0"));

        CartEntity entity = cartRepository.findCartEntity(1L, 102L, 12L).get();
        Assertions.assertThat(entity.getQuantity()).isEqualTo(1);
        Assertions.assertThat(entity.getPrice()).isEqualByComparingTo("13000");
    }

    @Test
    @Transactional
    @DisplayName("장바구니 제품 추가 - 정상(장바구니에서 상품에 +를 눌렀을 경우)")
    void 장바구니_추가_2() throws Exception {

        ProductRequest request = new ProductRequest(100L, 10L);

        // when
        ResultActions resultActions = mvc
                .perform(
                        post("/carts/add")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .header("X-USERS-IDX", "1")
                                .content(om.writeValueAsString(request))
                )
                .andDo(print());

        resultActions
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.resultCode").value("ok"))
                .andExpect(jsonPath("$.data.product.productName").value("테스트 상품1"))
                .andExpect(jsonPath("$.data.quantity").value(3))
                .andExpect(jsonPath("$.data.amount").value("30000"));

        CartEntity entity = cartRepository.findCartEntity(1L, 100L, 10L).get();
        Assertions.assertThat(entity.getQuantity()).isEqualTo(3);
    }

    @Test
    @Transactional
    @DisplayName("장바구니 제품 빼기 - 정상(장바구니에서 상품에 -를 눌렀을 경우)")
    void 장바구니_빼기() throws Exception {

        ProductRequest request = new ProductRequest(100L, 10L);

        // when
        ResultActions resultActions = mvc
                .perform(
                        post("/carts/subtract")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .header("X-USERS-IDX", "1")
                                .content(om.writeValueAsString(request))
                )
                .andDo(print());

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("ok"))
                .andExpect(jsonPath("$.data.product.productName").value("테스트 상품1"))
                .andExpect(jsonPath("$.data.quantity").value(1))
                .andExpect(jsonPath("$.data.amount").value("10000"));

        CartEntity entity = cartRepository.findCartEntity(1L, 100L, 10L).get();
        Assertions.assertThat(entity.getQuantity()).isEqualTo(1);
    }

    @Test
    @Transactional
    @DisplayName("장바구니 제품 삭제 - 정상(장바구니에서 상품에 x를 눌렀을 경우)")
    void 장바구니_삭제() throws Exception {

        ProductRequest request = new ProductRequest(100L, 10L);

        // when
        ResultActions resultActions = mvc
                .perform(
                        delete("/carts")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .header("X-USERS-IDX", "1")
                                .content(om.writeValueAsString(request))
                )
                .andDo(print());

        resultActions
                .andExpect(status().isNoContent())
                .andExpect(jsonPath("$.resultCode").value("ok"));

        Optional<CartEntity> entity = cartRepository.findCartEntity(1L, 100L, 10L);
        Assertions.assertThat(entity).isEmpty();
    }
}