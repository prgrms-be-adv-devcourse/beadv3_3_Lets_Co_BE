package co.kr.order.controller;

import co.kr.order.client.ProductClient;
import co.kr.order.client.UserClient;
import co.kr.order.model.dto.AddressInfo;
import co.kr.order.model.dto.CardInfo;
import co.kr.order.model.dto.ProductInfo;
import co.kr.order.model.dto.request.OrderDirectRequest;
import co.kr.order.model.dto.request.OrderRequest;
import co.kr.order.model.dto.UserData;
import co.kr.order.model.entity.CartEntity;
import co.kr.order.model.entity.OrderEntity;
import co.kr.order.model.entity.OrderItemEntity;
import co.kr.order.model.vo.OrderStatus;
import co.kr.order.repository.CartJpaRepository;
import co.kr.order.repository.OrderItemJpaRepository;
import co.kr.order.repository.OrderJpaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import feign.Request;
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
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
    @Autowired CartJpaRepository cartRepository;

    @BeforeEach
    void init() {
        // given
        UserData userData = new UserData(1L, 2L, 3L);
        given(userClient.getUserData(eq(1L), any())).willReturn(userData);

        ProductInfo productInfo1 = new ProductInfo(100L, 10L, "테스트 상품1", "옵션A", new BigDecimal("10000.00"), 100);
        ProductInfo productInfo2 = new ProductInfo(101L, 11L, "테스트 상품2", "옵션B", new BigDecimal("15000.00"), 100);

        // 직접 주문
        given(productClient.getProduct(100L, 10L)).willReturn(productInfo1);
        given(productClient.getProduct(101L, 11L)).willReturn(productInfo2);

        // 카트 주문
        List<ProductInfo> productList = List.of(productInfo1, productInfo2);
        given(productClient.getProductList(any())).willReturn(productList);
    }

    @Test
    @Transactional
    void 단일상품_주문_정상 () throws Exception {

        OrderRequest orderRequest = new OrderRequest(100L, 10L, 3);
        AddressInfo addressInfo = new AddressInfo("홍길동", "주소1", "상세1", "01012345678");
        CardInfo cardInfo = new CardInfo("브랜드", "카드이름", "card token", 12, 2029);
        UserData userData = new UserData(addressInfo, cardInfo);

        OrderDirectRequest request = new OrderDirectRequest(orderRequest, userData);

        ResultActions resultActions = mvc
                .perform(
                        post("/order")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .header("X-USERS-IDX", "1")
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
    void 카트로_주문_정상() throws Exception {

        CartEntity cart1 = CartEntity.builder()
                .userIdx(1L)
                .productIdx(100L)
                .optionIdx(10L)
                .quantity(2)
                .price(new BigDecimal("20000.00"))
                .del(false)
                .build();
        cartRepository.save(cart1);

        CartEntity cart2 = CartEntity.builder()
                .userIdx(1L)
                .productIdx(101L)
                .optionIdx(11L)
                .quantity(3)
                .price(new BigDecimal("10000.00"))
                .del(false)
                .build();
        cartRepository.save(cart2);

        AddressInfo addressInfo = new AddressInfo("홍길동", "주소1", "상세1", "01012345678");
        CardInfo cardInfo = new CardInfo("브랜드", "카드이름", "card token", 12, 2029);
        UserData userData = new UserData(addressInfo, cardInfo);

        ResultActions resultActions = mvc
                .perform(
                        post("/order/cart")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .header("X-USERS-IDX", "1")
                                .content(objectMapper.writeValueAsString(userData))
                )
                .andDo(print());

        resultActions.andExpect(status().isOk())
                .andExpect(handler().handlerType(OrderController.class))
                .andExpect(jsonPath("$.resultCode").value("ok"))
                .andExpect(jsonPath("$.data.itemList[0].product.productIdx").value(100L))
                .andExpect(jsonPath("$.data.itemList[0].product.productName").value("테스트 상품1"))
                .andExpect(jsonPath("$.data.itemList[0].product.optionContent").value("옵션A"))
                .andExpect(jsonPath("$.data.itemList[0].product.price").value("10000.0"))
                .andExpect(jsonPath("$.data.itemList[0].quantity").value(2))
                .andExpect(jsonPath("$.data.itemList[0].amount").value("20000.0"))
                .andExpect(jsonPath("$.data.itemList[1].product.productIdx").value(101L))
                .andExpect(jsonPath("$.data.itemList[1].product.productName").value("테스트 상품2"))
                .andExpect(jsonPath("$.data.itemList[1].product.optionContent").value("옵션B"))
                .andExpect(jsonPath("$.data.itemList[1].product.price").value("15000.0"))
                .andExpect(jsonPath("$.data.itemList[1].quantity").value(3))
                .andExpect(jsonPath("$.data.itemList[1].amount").value("45000.0"))
                .andExpect(jsonPath("$.data.itemsAmount").value("65000.0"));

        // Order 테이블
        OrderEntity orderEntity = orderRepository.findById(1L).get();
        Assertions.assertThat(orderEntity.getUserIdx()).isEqualTo(1L);
        Assertions.assertThat(orderEntity.getAddressIdx()).isEqualTo(2L);
        Assertions.assertThat(orderEntity.getCardIdx()).isEqualTo(3L);
        Assertions.assertThat(orderEntity.getOrderCode()).isNotNull();
        Assertions.assertThat(orderEntity.getStatus()).isEqualTo(OrderStatus.CREATED.name());
        Assertions.assertThat(orderEntity.getItemsAmount()).isEqualByComparingTo("65000.00");

        // OrderItem 테이블
        OrderItemEntity itemEntity1 = orderItemRepository.findById(1L).get();
        Assertions.assertThat(itemEntity1.getProductIdx()).isEqualTo(100L);
        Assertions.assertThat(itemEntity1.getOptionIdx()).isEqualTo(10L);
        Assertions.assertThat(itemEntity1.getProductName()).isEqualTo("테스트 상품1");
        Assertions.assertThat(itemEntity1.getOptionName()).isEqualTo("옵션A");
        Assertions.assertThat(itemEntity1.getPrice()).isEqualByComparingTo("10000.00");
        Assertions.assertThat(itemEntity1.getQuantity()).isEqualTo(2);

        OrderItemEntity itemEntity2 = orderItemRepository.findById(2L).get();
        Assertions.assertThat(itemEntity2.getProductIdx()).isEqualTo(101L);
        Assertions.assertThat(itemEntity2.getOptionIdx()).isEqualTo(11L);
        Assertions.assertThat(itemEntity2.getProductName()).isEqualTo("테스트 상품2");
        Assertions.assertThat(itemEntity2.getOptionName()).isEqualTo("옵션B");
        Assertions.assertThat(itemEntity2.getPrice()).isEqualByComparingTo("15000.00");
        Assertions.assertThat(itemEntity2.getQuantity()).isEqualTo(3);

        List<CartEntity> cartEntity = cartRepository.findAll();
        Assertions.assertThat(cartEntity).isEmpty();
    }

    @Test
    @Transactional
    void 단일상품_주문_실패_재고없음 () throws Exception {

        // 200개 주문했을 때 (재고는 100개)
        OrderRequest orderRequest = new OrderRequest(100L, 10L, 200);
        AddressInfo addressInfo = new AddressInfo("홍길동", "주소1", "상세1", "01012345678");
        CardInfo cardInfo = new CardInfo("브랜드", "카드이름", "card token", 12, 2029);
        UserData userData = new UserData(addressInfo, cardInfo);

        OrderDirectRequest request = new OrderDirectRequest(orderRequest, userData);

        ResultActions resultActions = mvc
                .perform(
                        post("/order")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .header("X-USERS-IDX", "1")
                                .content(objectMapper.writeValueAsString(request))
                )
                .andDo(print());

        resultActions.andExpect(status().is4xxClientError())
                .andExpect(handler().handlerType(OrderController.class))
                .andExpect(jsonPath("$.resultCode").value("OUT_OF_STOCK"))
                .andExpect(jsonPath("$.data").value("재고가 부족합니다."));

        // Order 테이블
        List<OrderEntity> orderEntity = orderRepository.findAll();
        Assertions.assertThat(orderEntity).hasSize(0);

        // OrderItem 테이블
        List<OrderItemEntity> itemEntity = orderItemRepository.findAll();
        Assertions.assertThat(itemEntity).hasSize(0);
    }

    @Test
    @Transactional
    void 단일상품_주문_실패_상품없음 () throws Exception {

        // 103L/10L 으로 찾았을 때 Product Not Found request 할 가짜 응답
        Request rq = Request.create(Request.HttpMethod.GET, "url", Collections.emptyMap(), null, null, null);
        given(productClient.getProduct(103L, 10L))
                .willThrow(new FeignException.NotFound("Product Not Found", rq, null, null));

        OrderRequest orderRequest = new OrderRequest(103L, 10L, 3);
        AddressInfo addressInfo = new AddressInfo("홍길동", "주소1", "상세1", "01012345678");
        CardInfo cardInfo = new CardInfo("브랜드", "카드이름", "card token", 12, 2029);
        UserData userData = new UserData(addressInfo, cardInfo);

        OrderDirectRequest request = new OrderDirectRequest(orderRequest, userData);

        ResultActions resultActions = mvc
                .perform(
                        post("/order")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .header("X-USERS-IDX", "1")
                                .content(objectMapper.writeValueAsString(request))
                )
                .andDo(print());

        resultActions.andExpect(status().is4xxClientError())
                .andExpect(handler().handlerType(OrderController.class))
                .andExpect(jsonPath("$.resultCode").value("PRODUCT_NOT_FOUND"));

        // Order 테이블
        List<OrderEntity> orderEntity = orderRepository.findAll();
        Assertions.assertThat(orderEntity).hasSize(0);

        // OrderItem 테이블
        List<OrderItemEntity> itemEntity = orderItemRepository.findAll();
        Assertions.assertThat(itemEntity).hasSize(0);
    }

    @Test
    @Transactional
    void 단일상품_주문_실패_주소정보_없음 () throws Exception {

        OrderRequest orderRequest = new OrderRequest(100L, 10L, 3);
        AddressInfo addressInfo = null;
        CardInfo cardInfo = new CardInfo("브랜드", "카드이름", "card token", 12, 2029);
        UserData userData = new UserData(addressInfo, cardInfo);
        OrderDirectRequest request = new OrderDirectRequest(orderRequest, userData);

        UserData noAddress = new UserData(1L, null, 3L);
        given(userClient.getUserData(eq(1L), any())).willReturn(noAddress);

        ResultActions resultActions = mvc
                .perform(
                        post("/order")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .header("X-USERS-IDX", "1")
                                .content(objectMapper.writeValueAsString(request))
                )
                .andDo(print());
//
        resultActions.andExpect(status().is4xxClientError())
                .andExpect(handler().handlerType(OrderController.class))
                .andExpect(jsonPath("$.resultCode").value("NO_INPUT_ADDRESS_DATA"));

        // Order 테이블
        List<OrderEntity> orderEntity = orderRepository.findAll();
        Assertions.assertThat(orderEntity).hasSize(0);

        // OrderItem 테이블
        List<OrderItemEntity> itemEntity = orderItemRepository.findAll();
        Assertions.assertThat(itemEntity).hasSize(0);
    }

    @Test
    @Transactional
    void 단일상품_주문_실패_카드정보_없음 () throws Exception {

        OrderRequest orderRequest = new OrderRequest(100L, 10L, 3);
        AddressInfo addressInfo = new AddressInfo("홍길동", "주소1", "상세1", "01012345678");
        CardInfo cardInfo = null;
        UserData userData = new UserData(addressInfo, cardInfo);
        OrderDirectRequest request = new OrderDirectRequest(orderRequest, userData);

        UserData noAddress = new UserData(1L, 2L, null);
        given(userClient.getUserData(eq(1L), any())).willReturn(noAddress);

        ResultActions resultActions = mvc
                .perform(
                        post("/order")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .header("X-USERS-IDX", "1")
                                .content(objectMapper.writeValueAsString(request))
                )
                .andDo(print());
//
        resultActions.andExpect(status().is4xxClientError())
                .andExpect(handler().handlerType(OrderController.class))
                .andExpect(jsonPath("$.resultCode").value("NO_INPUT_CARD_DATA"));

        // Order 테이블
        List<OrderEntity> orderEntity = orderRepository.findAll();
        Assertions.assertThat(orderEntity).hasSize(0);

        // OrderItem 테이블
        List<OrderItemEntity> itemEntity = orderItemRepository.findAll();
        Assertions.assertThat(itemEntity).hasSize(0);
    }

    @Test
    @Transactional
    void 단일상품_주문_실패_결제정보_없음 () throws Exception {

        OrderRequest orderRequest = new OrderRequest(100L, 10L, 3);
        AddressInfo addressInfo = null;
        CardInfo cardInfo = null;
        UserData userData = new UserData(addressInfo, cardInfo);
        OrderDirectRequest request = new OrderDirectRequest(orderRequest, userData);

        UserData noAddress = new UserData(1L, null, null);
        given(userClient.getUserData(eq(1L), any())).willReturn(noAddress);

        ResultActions resultActions = mvc
                .perform(
                        post("/order")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .header("X-USERS-IDX", "1")
                                .content(objectMapper.writeValueAsString(request))
                )
                .andDo(print());

        resultActions.andExpect(status().is4xxClientError())
                .andExpect(handler().handlerType(OrderController.class))
                .andExpect(jsonPath("$.resultCode").value("NO_INPUT_ORDER_DATA"));

        // Order 테이블
        List<OrderEntity> orderEntity = orderRepository.findAll();
        Assertions.assertThat(orderEntity).hasSize(0);

        // OrderItem 테이블
        List<OrderItemEntity> itemEntity = orderItemRepository.findAll();
        Assertions.assertThat(itemEntity).hasSize(0);
    }


    @Test
    @Transactional
    @DisplayName("주문 목록 조회 - 정상")
    void 주문_목록_조회_정상() throws Exception {
        // given: 조회할 데이터 DB에 미리 저장
        OrderEntity order1 = OrderEntity.builder()
                .userIdx(1L)
                .addressIdx(2L)
                .cardIdx(3L)
                .orderCode("ORDER-CODE-1")
                .status(OrderStatus.CREATED.name())
                .itemsAmount(new BigDecimal("80000.00"))
                .del(false)
                .build();
        orderRepository.save(order1);

        OrderItemEntity item1 = OrderItemEntity.builder()
                .order(order1)
                .productIdx(100L)
                .optionIdx(10L)
                .productName("기록된 상품1")
                .optionName("옵션A")
                .price(new BigDecimal("10000.00"))
                .quantity(2)
                .del(false)
                .build();
        orderItemRepository.save(item1);

        OrderItemEntity item2 = OrderItemEntity.builder()
                .order(order1)
                .productIdx(101L)
                .optionIdx(11L)
                .productName("기록된 상품2")
                .optionName("옵션B")
                .price(new BigDecimal("20000.00"))
                .quantity(3)
                .del(false)
                .build();
        orderItemRepository.save(item2);

        // when
        ResultActions resultActions = mvc
                .perform(
                        get("/order")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .header("X-USERS-IDX", "1")
                )
                .andDo(print());

    }

    @Test
    @Transactional
    @DisplayName("주문 상세 조회 - 정상")
    void 주문_상세_조회_정상() throws Exception {
        // given: 특정 주문 코드를 가진 데이터 저장
        String targetOrderCode = "TARGET-UUID-1234";

        OrderEntity order = OrderEntity.builder()
                .userIdx(1L)
                .addressIdx(2L)
                .cardIdx(3L)
                .orderCode(targetOrderCode)
                .status(OrderStatus.CREATED.name())
                .itemsAmount(new BigDecimal("15000.00"))
                .del(false)
                .build();
        orderRepository.save(order);

        OrderItemEntity item = OrderItemEntity.builder()
                .order(order)
                .productIdx(101L)
                .optionIdx(11L)
                .productName("상세보기 상품")
                .optionName("옵션B")
                .price(new BigDecimal("5000.00"))
                .quantity(3)
                .del(false)
                .build();
        orderItemRepository.save(item);

        // when
        ResultActions resultActions = mvc
                .perform(
                        get("/order/" + targetOrderCode)
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .header("X-USERS-IDX", "1")
                )
                .andDo(print());

    }

    @Test
    @Transactional
    @DisplayName("주문 상세 조회 - 실패 (주문 없음)")
    void 주문_상세_조회_실패_NOT_FOUND() throws Exception {

        String targetOrderCode = "TARGET-UUID-1234";

        // when
        ResultActions resultActions = mvc
                .perform(
                        get("/order/" + targetOrderCode)
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .header("X-USERS-IDX", "1")
                )
                .andDo(print());

        // then
        resultActions
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.resultCode").value("ORDER_NOT_FOUND"));
    }
}