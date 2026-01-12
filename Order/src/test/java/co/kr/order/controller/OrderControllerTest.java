package co.kr.order.controller;

import co.kr.order.client.ProductClient;
import co.kr.order.client.UserClient;
import co.kr.order.model.dto.CartOrderRequest;
import co.kr.order.model.dto.GetOrderData;
import co.kr.order.model.dto.OrderRequest;
import co.kr.order.model.dto.ProductInfo;
import co.kr.order.model.entity.OrderEntity;
import co.kr.order.model.entity.OrderItemEntity;
import co.kr.order.model.vo.OrderStatus;
import co.kr.order.repository.OrderItemJpaRepository;
import co.kr.order.repository.OrderJpaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
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
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
        GetOrderData orderData = new GetOrderData(1L, 2L, 3L);
        given(userClient.getOrderData(anyString())).willReturn(orderData);

        ProductInfo productInfo1 = new ProductInfo(100L, "테스트 상품1", "옵션A", new BigDecimal("10000.00"));
        given(productClient.getProduct(100L, 10L)).willReturn(productInfo1);
    }

    @Test
    @Transactional
    void 단일상품_주문 () throws Exception {

        OrderRequest request = new OrderRequest(100L, 10L, 3);

        ResultActions resultActions = mvc
                .perform(
                        post("/order")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .header("Authorization", "Bearer Temp")
                                .content(objectMapper.writeValueAsString(request))
                )
                .andDo(print());

        resultActions.andExpect(status().isOk())
                .andExpect(handler().handlerType(OrderController.class))
                .andExpect(jsonPath("$.resultCode").value("ok"))
                .andExpect(jsonPath("$.data.productIdx").value(100L))
                .andExpect(jsonPath("$.data.productName").value("테스트 상품1"))
                .andExpect(jsonPath("$.data.optionContent").value("옵션A"))
                .andExpect(jsonPath("$.data.price").value("10000.0"))
                .andExpect(jsonPath("$.data.quantity").value(3));

        // Order 테이블
        OrderEntity orderEntity = orderRepository.findById(1L).get();
        Assertions.assertThat(orderEntity.getUserIdx()).isEqualTo(1L);
        Assertions.assertThat(orderEntity.getAddressIdx()).isEqualTo(2L);
        Assertions.assertThat(orderEntity.getCardIdx()).isEqualTo(3L);
        Assertions.assertThat(orderEntity.getOrderCode()).isNotNull();
        Assertions.assertThat(orderEntity.getStatus()).isEqualTo(OrderStatus.CREATED.name());
        Assertions.assertThat(orderEntity.getItemsAmount()).isEqualByComparingTo("30000.00");
        Assertions.assertThat(orderEntity.getTotalAmount()).isEqualByComparingTo("30000.00");

        // OrderItem 테이블
        OrderItemEntity itemEntity = orderItemRepository.findById(1L).get();
        Assertions.assertThat(itemEntity.getProductIdx()).isEqualTo(100L);
        Assertions.assertThat(itemEntity.getOptionIdx()).isEqualTo(10L);
        Assertions.assertThat(itemEntity.getProductName()).isEqualTo("테스트 상품1");
        Assertions.assertThat(itemEntity.getOptionName()).isEqualTo("옵션A");
        Assertions.assertThat(itemEntity.getPrice()).isEqualByComparingTo("10000.00");
        Assertions.assertThat(itemEntity.getQuantity()).isEqualTo(3);
    }

    @Test
    @Transactional
    void 카트로_주문() throws Exception {
        OrderRequest item1 = new OrderRequest(100L, 10L, 3);
        OrderRequest item2 = new OrderRequest(101L, 11L, 2);

        List<OrderRequest> items = new ArrayList<>();
        items.add(item1);
        items.add(item2);

        CartOrderRequest cart = new CartOrderRequest(items);

//        ResultActions resultActions = mvc
//                .perform(
//                        post("/order")
//                                .contentType(MediaType.APPLICATION_JSON)
//                                .accept(MediaType.APPLICATION_JSON)
//                                .header("Authorization", "Bearer Temp")
//                                .content(objectMapper.writeValueAsString(cart))
//                )
//                .andDo(print());
//
//        resultActions.andExpect(status().isOk())
//                .andExpect(handler().handlerType(OrderController.class))
//                .andExpect(jsonPath("$.resultCode").value("ok"))
//                .andExpect(jsonPath("$.data.productIdx").value(100L))
//                .andExpect(jsonPath("$.data.productName").value("테스트 상품1"))
//                .andExpect(jsonPath("$.data.optionContent").value("옵션A"))
//                .andExpect(jsonPath("$.data.price").value("10000.0"))
//                .andExpect(jsonPath("$.data.quantity").value(3));
//
//        // Order 테이블
//        OrderEntity orderEntity = orderRepository.findById(1L).get();
//        Assertions.assertThat(orderEntity.getUserIdx()).isEqualTo(1L);
//        Assertions.assertThat(orderEntity.getAddressIdx()).isEqualTo(2L);
//        Assertions.assertThat(orderEntity.getCardIdx()).isEqualTo(3L);
//        Assertions.assertThat(orderEntity.getOrderCode()).isNotNull();
//        Assertions.assertThat(orderEntity.getStatus()).isEqualTo(OrderStatus.CREATED.name());
//        Assertions.assertThat(orderEntity.getItemsAmount()).isEqualByComparingTo("30000.00");
//        Assertions.assertThat(orderEntity.getTotalAmount()).isEqualByComparingTo("30000.00");
//
//        // OrderItem 테이블
//        OrderItemEntity itemEntity = orderItemRepository.findById(1L).get();
//        Assertions.assertThat(itemEntity.getProductIdx()).isEqualTo(100L);
//        Assertions.assertThat(itemEntity.getOptionIdx()).isEqualTo(10L);
//        Assertions.assertThat(itemEntity.getProductName()).isEqualTo("테스트 상품1");
//        Assertions.assertThat(itemEntity.getOptionName()).isEqualTo("옵션A");
//        Assertions.assertThat(itemEntity.getPrice()).isEqualByComparingTo("10000.00");
//        Assertions.assertThat(itemEntity.getQuantity()).isEqualTo(3);
    }
}