package co.kr.order.controller;

import co.kr.order.client.ProductClient;
import co.kr.order.client.UserClient;
import co.kr.order.model.dto.AddressInfo;
import co.kr.order.model.dto.CardInfo;
import co.kr.order.model.dto.ProductInfo;
import co.kr.order.model.dto.UserData;
import co.kr.order.model.dto.request.*;
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

import static org.mockito.ArgumentMatchers.any;
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
        UserData userData = new UserData(1L, 2L, 3L);
        given(userClient.getUserData(anyString(), any())).willReturn(userData);

        ProductInfo productInfo1 = new ProductInfo(100L, 10L, "테스트 상품1", "옵션A", new BigDecimal("10000.00"), 100);
        ProductInfo productInfo2 = new ProductInfo(101L, 11L, "테스트 상품2", "옵션B", new BigDecimal("15000.00"), 100);

        // 직접 주문
        given(productClient.getProduct(new ProductRequest(100L, 10L))).willReturn(productInfo1);
        given(productClient.getProduct(new ProductRequest(101L, 11L))).willReturn(productInfo2);

        // 카트 주문
        List<ProductInfo> productList = List.of(productInfo1, productInfo2);
        given(productClient.getProductList(any())).willReturn(productList);
    }

    @Test
    @Transactional
    void 단일상품_주문 () throws Exception {

        OrderRequest orderRequest = new OrderRequest(100L, 10L, 3);
        AddressInfo addressInfo = new AddressInfo("홍길동", "주소1", "상세1", "01012345678");
        CardInfo cardInfo = new CardInfo("브랜드", "카드이름", "card token", 12, 2029);
        UserDataRequest userData = new UserDataRequest(addressInfo, cardInfo);

        OrderDirectRequest request = new OrderDirectRequest(orderRequest, userData);

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
                .andExpect(jsonPath("$.data.item.product.productIdx").value(100L))
                .andExpect(jsonPath("$.data.item.product.productName").value("테스트 상품1"))
                .andExpect(jsonPath("$.data.item.product.optionContent").value("옵션A"))
                .andExpect(jsonPath("$.data.item.product.price").value("10000.0"))
                .andExpect(jsonPath("$.data.item.quantity").value(3))
                .andExpect(jsonPath("$.data.item.amount").value("30000.0"));

        // Order 테이블
        OrderEntity orderEntity = orderRepository.findById(1L).get();
        Assertions.assertThat(orderEntity.getUserIdx()).isEqualTo(1L);
        Assertions.assertThat(orderEntity.getAddressIdx()).isEqualTo(2L);
        Assertions.assertThat(orderEntity.getCardIdx()).isEqualTo(3L);
        Assertions.assertThat(orderEntity.getOrderCode()).isNotNull();
        Assertions.assertThat(orderEntity.getStatus()).isEqualTo(OrderStatus.CREATED.name());
        Assertions.assertThat(orderEntity.getItemsAmount()).isEqualByComparingTo("30000.00");

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

        AddressInfo addressInfo = new AddressInfo("홍길동", "주소1", "상세1", "01012345678");
        CardInfo cardInfo = new CardInfo("브랜드", "카드이름", "card token", 12, 2029);
        UserDataRequest userData = new UserDataRequest(addressInfo, cardInfo);

        OrderRequest item1 = new OrderRequest(100L, 10L, 3);
        OrderRequest item2 = new OrderRequest(101L, 11L, 2);

        List<OrderRequest> items = new ArrayList<>();
        items.add(item1);
        items.add(item2);

        OrderCartRequest cart = new OrderCartRequest(items, userData);

        // todo
        ResultActions resultActions = mvc
                .perform(
                        post("/order/cart")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .header("Authorization", "Bearer Temp")
                                .content(objectMapper.writeValueAsString(cart))
                )
                .andDo(print());

        resultActions.andExpect(status().isOk())
                .andExpect(handler().handlerType(OrderController.class))
                .andExpect(jsonPath("$.resultCode").value("ok"))
                .andExpect(jsonPath("$.data.itemList[0].product.productIdx").value(100L))
                .andExpect(jsonPath("$.data.itemList[0].product.productName").value("테스트 상품1"))
                .andExpect(jsonPath("$.data.itemList[0].product.optionContent").value("옵션A"))
                .andExpect(jsonPath("$.data.itemList[0].product.price").value("10000.0"))
                .andExpect(jsonPath("$.data.itemList[0].quantity").value(3))
                .andExpect(jsonPath("$.data.itemList[0].amount").value("30000.0"))
                .andExpect(jsonPath("$.data.itemList[1].product.productIdx").value(101L))
                .andExpect(jsonPath("$.data.itemList[1].product.productName").value("테스트 상품2"))
                .andExpect(jsonPath("$.data.itemList[1].product.optionContent").value("옵션B"))
                .andExpect(jsonPath("$.data.itemList[1].product.price").value("15000.0"))
                .andExpect(jsonPath("$.data.itemList[1].quantity").value(2))
                .andExpect(jsonPath("$.data.itemList[1].amount").value("30000.0"))
                .andExpect(jsonPath("$.data.itemsAmount").value("60000.0"));

        // Order 테이블
        OrderEntity orderEntity = orderRepository.findById(1L).get();
        Assertions.assertThat(orderEntity.getUserIdx()).isEqualTo(1L);
        Assertions.assertThat(orderEntity.getAddressIdx()).isEqualTo(2L);
        Assertions.assertThat(orderEntity.getCardIdx()).isEqualTo(3L);
        Assertions.assertThat(orderEntity.getOrderCode()).isNotNull();
        Assertions.assertThat(orderEntity.getStatus()).isEqualTo(OrderStatus.CREATED.name());
        Assertions.assertThat(orderEntity.getItemsAmount()).isEqualByComparingTo("60000.00");

        // OrderItem 테이블
        OrderItemEntity itemEntity1 = orderItemRepository.findById(1L).get();
        Assertions.assertThat(itemEntity1.getProductIdx()).isEqualTo(100L);
        Assertions.assertThat(itemEntity1.getOptionIdx()).isEqualTo(10L);
        Assertions.assertThat(itemEntity1.getProductName()).isEqualTo("테스트 상품1");
        Assertions.assertThat(itemEntity1.getOptionName()).isEqualTo("옵션A");
        Assertions.assertThat(itemEntity1.getPrice()).isEqualByComparingTo("10000.00");
        Assertions.assertThat(itemEntity1.getQuantity()).isEqualTo(3);

        OrderItemEntity itemEntity2 = orderItemRepository.findById(2L).get();
        Assertions.assertThat(itemEntity2.getProductIdx()).isEqualTo(101L);
        Assertions.assertThat(itemEntity2.getOptionIdx()).isEqualTo(11L);
        Assertions.assertThat(itemEntity2.getProductName()).isEqualTo("테스트 상품2");
        Assertions.assertThat(itemEntity2.getOptionName()).isEqualTo("옵션B");
        Assertions.assertThat(itemEntity2.getPrice()).isEqualByComparingTo("15000.00");
        Assertions.assertThat(itemEntity2.getQuantity()).isEqualTo(2);
    }
}