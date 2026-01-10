package co.kr.order.controller;

import co.kr.order.client.ProductClient;
import co.kr.order.client.UserClient;
import co.kr.order.model.dto.ProductInfo;
import co.kr.order.model.entity.CartEntity;
import co.kr.order.repository.CartJpaRepository;
import jakarta.transaction.Transactional;
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

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CartControllerTest {

    @Autowired MockMvc mvc;
    @Autowired CartJpaRepository cartJpaRepository;

    @MockitoBean UserClient userClient;
    @MockitoBean ProductClient productClient;

    @BeforeEach
    void setUp() {
        // given:
        given(userClient.getUserIdx(anyString())).willReturn(1L);

        ProductInfo mockProduct1 = new ProductInfo(100L, "테스트 상품1", "옵션A", new BigDecimal("10000.00"), 2);
        ProductInfo mockProduct2 = new ProductInfo(101L, "테스트 상품2", "옵션B", new BigDecimal("12000.00"), 3);

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
    @DisplayName("장바구니 조회 - 정상 케이스")
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
                .andExpect(jsonPath("$.resultCode").value("ok"))
                .andExpect(jsonPath("$.data.productList[0].productName").value("테스트 상품1"))
                .andExpect(jsonPath("$.data.productList[1].productName").value("테스트 상품2"))
                .andExpect(jsonPath("$.data.totalAmount").value(56000.00));
    }
}
