package co.kr.order.domain.controller;

import co.kr.order.client.ProductClient;
import co.kr.order.client.UserClient;
import co.kr.order.controller.OrderController;
import co.kr.order.model.dto.ProductInfo;
import co.kr.order.model.entity.OrderEntity;
import co.kr.order.model.entity.OrderItemEntity;
import co.kr.order.repository.OrderItemJpaRepository;
import co.kr.order.repository.OrderJpaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OrderControllerTest {

    @MockitoBean UserClient userClient;
    @MockitoBean ProductClient productClient;

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;

    @Autowired OrderJpaRepository orderRepository;
    @Autowired OrderItemJpaRepository orderItemRepository;


    @BeforeEach
    void init() {
        // given
        given(userClient.getUserIdx(anyString())).willReturn(1L);

        ProductInfo productInfo1 = new ProductInfo(103L, "상품2", "옵션B", new BigDecimal("15000.00"));
        given(productClient.getProduct(100L, 10L)).willReturn(productInfo1);

        OrderEntity order1 = new OrderEntity();
        order1.setUserIdx(1L);
        order1.setAddressIdx(1L);
        order1.setCardIdx(1L);
        order1.setOrderCode("tempString");
        order1.setStatus("PENDING");
        order1.setItemsAmount(new BigDecimal("20000.00"));
        order1.setTotalAmount(new BigDecimal("20000.00"));
        order1.setDel(false);

        OrderItemEntity orderItem1 = new OrderItemEntity();
        orderItem1.setOrderIdx(order1);
        orderItem1.setProductIdx(100L);
        orderItem1.setOptionIdx(10L);
        orderItem1.setProductName("상품1");
        orderItem1.setOptionName("옵션A");
        orderItem1.setPrice(new BigDecimal("10000.00"));
        orderItem1.setQuantity(2);
        orderItem1.setDel(false);
    }

    @Test
    @Transactional
    void 주문_아이템_조회 () throws Exception {

        ResultActions resultActions = mvc
                .perform(
                    get("/order/{ordersItemIdx}")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .header("Authorization", "Bearer token")
                )
                .andDo(print());

        resultActions.andExpect(status().isOk());
        resultActions.andExpect(handler().handlerType(OrderController.class));
        resultActions.andExpect(jsonPath("$.result").value("ok"));
        resultActions.andExpect(jsonPath("$.data.productName").value("상품1"));
        resultActions.andExpect(jsonPath("$.data.option").value("옵션A"));
        resultActions.andExpect(jsonPath("$.data.price").value(new BigDecimal("10000.0")));
        resultActions.andExpect(jsonPath("$.data.quantity").value(2));
    }
}