package co.kr.order.controller;

import co.kr.order.domain.client.ProductClient;
import co.kr.order.domain.client.UserClient;
import co.kr.order.domain.controller.CartController;
import co.kr.order.domain.model.dto.CartRequest;
import co.kr.order.domain.model.dto.ProductInfo;
import co.kr.order.domain.model.entity.CartEntity;
import co.kr.order.domain.repository.CartJpaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CartControllerTest {

    @Autowired MockMvc mvc;
    @Autowired CartJpaRepository cartJpaRepository;

    @MockitoBean UserClient userClient;
    @MockitoBean ProductClient productClient;

    @Autowired ObjectMapper objectMapper;


    @BeforeEach
    void setUp() {
        // given:
        given(userClient.getUserIdx(anyString())).willReturn(1L);

        ProductInfo mockProduct1 = new ProductInfo(100L, "테스트 상품1", "옵션A", new BigDecimal("10000.00"), null, null);
        ProductInfo mockProduct2 = new ProductInfo(101L, "테스트 상품2", "옵션B", new BigDecimal("12000.00"), null, null);

        given(productClient.getProductById(100L)).willReturn(mockProduct1);
        given(productClient.getProductById(101L)).willReturn(mockProduct2);

        CartEntity cartItem1 = new CartEntity();
        cartItem1.setUserIdx(1L);
        cartItem1.setProductIdx(100L);
        cartItem1.setOptionIdx(10L);
        cartItem1.setQuantity(2);
        cartItem1.setPrice(new BigDecimal("20000.00"));
        cartItem1.setDel(false);
        cartJpaRepository.save(cartItem1);

        CartEntity cartItem2 = new CartEntity();
        cartItem2.setUserIdx(1L);
        cartItem2.setProductIdx(101L);
        cartItem2.setOptionIdx(11L);
        cartItem2.setQuantity(3);
        cartItem2.setPrice(new BigDecimal("36000.00"));
        cartItem2.setDel(false);
        cartJpaRepository.save(cartItem2);
    }

    @Test
    @Transactional
    @DisplayName("장바구니 조회 - 정상")
    void 장바구니_조회() throws Exception {

        // when:
        ResultActions resultActions = mvc
                .perform(
                        get("/cart")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .header("Authorization", "Bearer test-token")
                )
                .andDo(print());

        // then:
        resultActions
                .andExpect(status().isOk())
                .andExpect(handler().handlerType(CartController.class))
                .andExpect(jsonPath("$.resultCode").value("ok"))
                .andExpect(jsonPath("$.data.productList[0].productName").value("테스트 상품1"))
                .andExpect(jsonPath("$.data.productList[0].optionContent").value("옵션A"))
                .andExpect(jsonPath("$.data.productList[0].price").value("10000.0"))
                .andExpect(jsonPath("$.data.productList[0].quantity").value(2))
                .andExpect(jsonPath("$.data.productList[0].totalPrice").value("20000.0"))

                .andExpect(jsonPath("$.data.productList[1].productName").value("테스트 상품2"))
                .andExpect(jsonPath("$.data.productList[1].optionContent").value("옵션B"))
                .andExpect(jsonPath("$.data.productList[1].price").value("12000.0"))
                .andExpect(jsonPath("$.data.productList[1].quantity").value(3))
                .andExpect(jsonPath("$.data.productList[1].totalPrice").value("36000.0"));

        CartEntity entity = cartJpaRepository.findByUserIdxAndProductIdxAndOptionIdx(1L, 100L, 10L).get();
        Assertions.assertThat(entity.getQuantity()).isEqualTo(2);
        Assertions.assertThat(entity.getPrice()).isEqualByComparingTo("20000.00");
    }

    @Test
    @Transactional
    @DisplayName("장바구니 제품 추가 - 정상(상품 목록에서 장바구니 추가를 눌렀을 경우)")
    void 장바구니_추가_1() throws Exception {

        CartRequest request = new CartRequest(102L, 10L);

        ProductInfo mockProduct3 = new ProductInfo(102L, "테스트 상품3", "옵션C", new BigDecimal("13000.00"), null, null);
        given(productClient.getProductById(102L)).willReturn(mockProduct3);

        // when
        ResultActions resultActions = mvc
                .perform(
                        post("/cart/add")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .header("Authorization", "Bearer dd")
                                .content(objectMapper.writeValueAsString(request))
                )
                .andDo(print());

                resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("ok"))
                // productList[]의 1번 2번은 데이터가 있으니 3번으로 비교 (개수가 1개인지)
                .andExpect(jsonPath("$.data.productList[2].quantity").value(1));

        CartEntity entity = cartJpaRepository.findByUserIdxAndProductIdxAndOptionIdx(1L, 102L, 10L).get();
        Assertions.assertThat(entity.getQuantity()).isEqualTo(1);
        Assertions.assertThat(entity.getPrice()).isEqualByComparingTo("13000.00");
    }

    @Test
    @Transactional
    @DisplayName("장바구니 제품 추가 - 정상(장바구니에서 상품에 +를 눌렀을 경우)")
    void 장바구니_추가_2() throws Exception {

        CartRequest request = new CartRequest(100L, 10L);

        // when
        ResultActions resultActions = mvc
                .perform(
                        post("/cart/add")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .header("Authorization", "Bearer dd")
                                .content(objectMapper.writeValueAsString(request))
                )
                .andDo(print());

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("ok"))
                .andExpect(jsonPath("$.data.productList[0].quantity").value(3));

        CartEntity entity = cartJpaRepository.findByUserIdxAndProductIdxAndOptionIdx(1L, 100L, 10L).get();
        Assertions.assertThat(entity.getQuantity()).isEqualTo(3);
    }

    @Test
    @Transactional
    @DisplayName("장바구니 제품 빼기 - 정상(장바구니에서 상품에 -를 눌렀을 경우)")
    void 장바구니_빼기() throws Exception {

        CartRequest request = new CartRequest(100L, 10L);

        // when
        ResultActions resultActions = mvc
                .perform(
                        post("/cart/subtract")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .header("Authorization", "Bearer dd")
                                .content(objectMapper.writeValueAsString(request))
                )
                .andDo(print());

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("ok"))
                .andExpect(jsonPath("$.data.productList[0].quantity").value(1));

        CartEntity entity = cartJpaRepository.findByUserIdxAndProductIdxAndOptionIdx(1L, 100L, 10L).get();
        Assertions.assertThat(entity.getQuantity()).isEqualTo(1);
    }

    @Test
    @Transactional
    @DisplayName("장바구니 제품 삭제 - 정상(장바구니에서 상품에 x를 눌렀을 경우)")
    void 장바구니_삭제() throws Exception {

        CartRequest request = new CartRequest(100L, 10L);

        // when
        ResultActions resultActions = mvc
                .perform(
                        delete("/cart")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .header("Authorization", "Bearer dd")
                                .content(objectMapper.writeValueAsString(request))
                )
                .andDo(print());

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("ok"));

        Optional<CartEntity> entity = cartJpaRepository.findByUserIdxAndProductIdxAndOptionIdx(1L, 100L, 10L);
        Assertions.assertThat(entity).isEmpty();
    }
}
