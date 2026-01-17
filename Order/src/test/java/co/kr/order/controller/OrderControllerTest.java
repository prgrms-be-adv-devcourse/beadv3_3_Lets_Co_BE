package co.kr.order.controller;

import co.kr.order.client.ProductClient;
import co.kr.order.client.UserClient;
import co.kr.order.model.dto.*;
import co.kr.order.model.dto.request.OrderCartRequest;
import co.kr.order.model.dto.request.OrderDirectRequest;
import co.kr.order.model.entity.CartEntity;
import co.kr.order.model.entity.OrderEntity;
import co.kr.order.model.entity.OrderItemEntity;
import co.kr.order.model.entity.PaymentEntity;
import co.kr.order.model.vo.OrderStatus;
import co.kr.order.model.vo.PaymentStatus;
import co.kr.order.model.vo.PaymentType;
import co.kr.order.repository.CartJpaRepository;
import co.kr.order.repository.OrderItemJpaRepository;
import co.kr.order.repository.OrderJpaRepository;
import co.kr.order.repository.PaymentJpaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import feign.Request;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
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
import java.util.Collections;
import java.util.List;
import java.util.UUID;

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

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;
    @PersistenceContext EntityManager em;

    @MockitoBean UserClient userClient;
    @MockitoBean ProductClient productClient;

    @Autowired OrderJpaRepository orderRepository;
    @Autowired OrderItemJpaRepository orderItemRepository;
    @Autowired CartJpaRepository cartRepository;
    @Autowired PaymentJpaRepository paymentRepository;

    String setOrderCode = "TARGET-UUID-1234";

    @BeforeEach
    void init() {
        // given

        // 초기 Entity 데이터 설정
        OrderEntity order = OrderEntity.builder()
                .userIdx(1L)
                .addressIdx(1L)
                .cardIdx(1L)
                .orderCode(setOrderCode)
                .status(OrderStatus.PAID)
                .itemsAmount(new BigDecimal("17000.00"))
                .del(false)
                .build();
        orderRepository.save(order);

        OrderItemEntity item1 = OrderItemEntity.builder()
                .order(order)
                .productIdx(1L)
                .optionIdx(1L)
                .productName("주문한 상품1")
                .optionName("옵션A")
                .price(new BigDecimal("1000.00"))
                .quantity(2)
                .del(false)
                .build();
        orderItemRepository.save(item1);

        OrderItemEntity item2 = OrderItemEntity.builder()
                .order(order)
                .productIdx(2L)
                .optionIdx(2L)
                .productName("주문한 상품2")
                .optionName("옵션B")
                .price(new BigDecimal("5000.00"))
                .quantity(3)
                .del(false)
                .build();
        orderItemRepository.save(item2);

        // 가짜 동기통신(FeignClient)
        // 1. 유저 정보
        AddressInfo addressInfo = new AddressInfo(1L, "홍길동", "주소1", "상세1", "01012345678");
        CardInfo cardInfo = new CardInfo(1L,"브랜드", "카드이름", "card token", 12, 2029);
        UserData userData = new UserData(1L, addressInfo, cardInfo);

        // 동기통신(Member) - 가짜 UserData 요청
        given(userClient.getUserData(eq(1L), any())).willReturn(userData);

        // 2. 상품 정보
        ProductInfo productInfo1 = new ProductInfo(100L, 10L, "테스트 상품1", "옵션A", new BigDecimal("10000.00"), 100);
        ProductInfo productInfo2 = new ProductInfo(101L, 11L, "테스트 상품2", "옵션B", new BigDecimal("15000.00"), 100);
        List<ProductInfo> productList = List.of(productInfo1, productInfo2);

        // 동기통신(Product) - 가짜 단일제품 정보 요청 (직접 결제)
        given(productClient.getProduct(100L, 10L)).willReturn(productInfo1);
        given(productClient.getProduct(101L, 11L)).willReturn(productInfo2);

        // 동기통신(Product) - 가짜 제품 리스트 요청 (카트 결제)
        given(productClient.getProductList(any())).willReturn(productList);
    }



    /*
     * ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ
     * 정상 케이스 테스트
     * - 단일상품 주문 (토스페이먼츠, 예치금, 카드)
     * - 장바구니 주문
     * - 주문 조회
     * - 주문 상세 조회
     * ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ
     */

    @Test
    @Transactional
    void 단일상품_주문_정상_토스 () throws Exception {

        // 제품ID(100), 옵션ID(10)인 제품 3개 주문 : 테스트 상품1 - 옵션A
        OrderItem orderItem = new OrderItem(100L, 10L, 3);

        // 사용자가 입력한 유저정보
        AddressInfo addressInfo = new AddressInfo(1L, "홍길동", "주소1", "상세1", "01012345678");
        CardInfo cardInfo = new CardInfo(1L, "브랜드", "카드이름", "card token", 12, 2029);
        UserData userData = new UserData(1L, addressInfo, cardInfo);

        // 요청 body
        OrderDirectRequest request = new OrderDirectRequest(orderItem, userData, PaymentType.TOSS_PAY);

        // 응답 테스트
        ResultActions resultActions = mvc.perform(
                post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-USERS-IDX", "1")
                        .content(objectMapper.writeValueAsString(request))
                )
                .andDo(print());

        // api 테스트
        resultActions.andExpect(handler().handlerType(OrderController.class))
                .andExpect(jsonPath("$.resultCode").value("ok"))
                .andExpect(jsonPath("$.data.itemList[0].product.productIdx").value(100L))
                .andExpect(jsonPath("$.data.itemList[0].product.productName").value("테스트 상품1"))
                .andExpect(jsonPath("$.data.itemList[0].product.optionName").value("옵션A"))
                .andExpect(jsonPath("$.data.itemList[0].product.price").value("10000.0"))
                .andExpect(jsonPath("$.data.itemList[0].quantity").value(3))
                .andExpect(jsonPath("$.data.itemList[0].amount").value("30000.0"))
                .andExpect(jsonPath("$.data.itemsAmount").value("30000.0"));

        // 영속성 컨텍스트
        em.flush();  // DB에 반영(flush)
        em.clear();  // 캐시 비우기

        // entity
        OrderEntity orderEntity = em
                .createQuery("SELECT o FROM OrderEntity o ORDER BY o.id DESC", OrderEntity.class)
                .setMaxResults(1)
                .getSingleResult();
        List<OrderItemEntity> orderItems = orderEntity.getOrderItems();

        OrderItemEntity itemEntity = orderItems.stream()
                .filter(i -> i.getProductIdx() == 100L)
                .findFirst()
                .orElseThrow();

        List<PaymentEntity> paymentEntity = em
                .createQuery("SELECT p FROM PaymentEntity p WHERE p.ordersIdx = :orderIdx", PaymentEntity.class)
                .setParameter("orderIdx", orderEntity.getId())
                .getResultList();

        // 실제 DB에 원하는 데이터가 들어갔는지 테스트
        // Order 테이블
        Assertions.assertThat(orderEntity.getUserIdx()).isEqualTo(1L);
        Assertions.assertThat(orderEntity.getAddressIdx()).isEqualTo(1L);
        Assertions.assertThat(orderEntity.getCardIdx()).isEqualTo(1L);
        Assertions.assertThat(orderEntity.getOrderCode()).isNotNull();
        Assertions.assertThat(orderEntity.getStatus()).isEqualTo(OrderStatus.CREATED);
        Assertions.assertThat(orderEntity.getItemsAmount()).isEqualByComparingTo("30000.00");
        // OrderItem 테이블
        Assertions.assertThat(itemEntity.getProductIdx()).isEqualTo(100L);
        Assertions.assertThat(itemEntity.getOptionIdx()).isEqualTo(10L);
        Assertions.assertThat(itemEntity.getProductName()).isEqualTo("테스트 상품1");
        Assertions.assertThat(itemEntity.getOptionName()).isEqualTo("옵션A");
        Assertions.assertThat(itemEntity.getPrice()).isEqualByComparingTo("10000.00");
        Assertions.assertThat(itemEntity.getQuantity()).isEqualTo(3);
        // Payment 테이블 (토스 결제는 이 시점에 PaymentEntity가 생성되지 않으므로 데이터가 없는것이 정상)
        Assertions.assertThat(paymentEntity).isEmpty();
    }

    @Test
    @Transactional
    void 단일상품_주문_정상_예치금 () throws Exception {

        // 제품ID(100), 옵션ID(10)인 제품 3개 주문 : 테스트 상품1 - 옵션A
        OrderItem orderItem = new OrderItem(100L, 10L, 3);

        // 사용자가 입력한 유저정보
        AddressInfo addressInfo = new AddressInfo(1L, "홍길동", "주소1", "상세1", "01012345678");
        CardInfo cardInfo = new CardInfo(1L, "브랜드", "카드이름", "card token", 12, 2029);
        UserData userData = new UserData(1L, addressInfo, cardInfo);

        // 요청 body
        OrderDirectRequest request = new OrderDirectRequest(orderItem, userData, PaymentType.DEPOSIT);

        // 응답 테스트
        ResultActions resultActions = mvc.perform(
                        post("/orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .header("X-USERS-IDX", "1")
                                .content(objectMapper.writeValueAsString(request))
                )
                .andDo(print());

        // api 테스트
        resultActions.andExpect(handler().handlerType(OrderController.class))
                .andExpect(jsonPath("$.resultCode").value("ok"))
                .andExpect(jsonPath("$.data.itemList[0].product.productIdx").value(100L))
                .andExpect(jsonPath("$.data.itemList[0].product.productName").value("테스트 상품1"))
                .andExpect(jsonPath("$.data.itemList[0].product.optionName").value("옵션A"))
                .andExpect(jsonPath("$.data.itemList[0].product.price").value("10000.0"))
                .andExpect(jsonPath("$.data.itemList[0].quantity").value(3))
                .andExpect(jsonPath("$.data.itemList[0].amount").value("30000.0"))
                .andExpect(jsonPath("$.data.itemsAmount").value("30000.0"));

        // 영속성 컨텍스트
        em.flush();  // DB에 반영(flush)
        em.clear();  // 캐시 비우기

        // entity
        OrderEntity orderEntity = em
                .createQuery("SELECT o FROM OrderEntity o ORDER BY o.id DESC", OrderEntity.class)
                .setMaxResults(1)
                .getSingleResult();
        List<OrderItemEntity> orderItems = orderEntity.getOrderItems();

        OrderItemEntity itemEntity = orderItems.stream()
                .filter(i -> i.getProductIdx() == 100L)
                .findFirst()
                .orElseThrow();

        List<PaymentEntity> entities = em
                .createQuery("SELECT p FROM PaymentEntity p WHERE p.ordersIdx = :orderIdx", PaymentEntity.class)
                .setParameter("orderIdx", orderEntity.getId())
                .getResultList();
        PaymentEntity paymentEntity = entities.get(0);

        // 실제 DB에 원하는 데이터가 들어갔는지 테스트
        // Order 테이블
        Assertions.assertThat(orderEntity.getUserIdx()).isEqualTo(1L);
        Assertions.assertThat(orderEntity.getAddressIdx()).isEqualTo(1L);
        Assertions.assertThat(orderEntity.getCardIdx()).isEqualTo(1L);
        Assertions.assertThat(orderEntity.getOrderCode()).isNotNull();
        Assertions.assertThat(orderEntity.getStatus()).isEqualTo(OrderStatus.PAID);
        Assertions.assertThat(orderEntity.getItemsAmount()).isEqualByComparingTo("30000.00");
        // OrderItem 테이블
        Assertions.assertThat(itemEntity.getProductIdx()).isEqualTo(100L);
        Assertions.assertThat(itemEntity.getOptionIdx()).isEqualTo(10L);
        Assertions.assertThat(itemEntity.getProductName()).isEqualTo("테스트 상품1");
        Assertions.assertThat(itemEntity.getOptionName()).isEqualTo("옵션A");
        Assertions.assertThat(itemEntity.getPrice()).isEqualByComparingTo("10000.00");
        Assertions.assertThat(itemEntity.getQuantity()).isEqualTo(3);
        // Payment 테이블
        Assertions.assertThat(paymentEntity.getOrdersIdx()).isEqualTo(orderEntity.getId());
        Assertions.assertThat(paymentEntity.getUsersIdx()).isEqualTo(1L);
        Assertions.assertThat(paymentEntity.getCardIdx()).isNull();
        Assertions.assertThat(paymentEntity.getStatus()).isEqualTo(PaymentStatus.PAYMENT);
        Assertions.assertThat(paymentEntity.getType()).isEqualTo(PaymentType.DEPOSIT);
        Assertions.assertThat(paymentEntity.getAmount()).isEqualByComparingTo("30000.00");
    }


    @Test
    @Transactional
    void 단일상품_주문_정상_카드 () throws Exception {

        // 제품ID(100), 옵션ID(10)인 제품 3개 주문 : 테스트 상품1 - 옵션A
        OrderItem orderItem = new OrderItem(100L, 10L, 3);

        // 사용자가 입력한 유저정보
        AddressInfo addressInfo = new AddressInfo(1L, "홍길동", "주소1", "상세1", "01012345678");
        CardInfo cardInfo = new CardInfo(1L, "브랜드", "카드이름", "card token", 12, 2029);
        UserData userData = new UserData(1L, addressInfo, cardInfo);

        // 요청 body
        OrderDirectRequest request = new OrderDirectRequest(orderItem, userData, PaymentType.CARD);

        // 응답 테스트
        ResultActions resultActions = mvc.perform(
                        post("/orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .header("X-USERS-IDX", "1")
                                .content(objectMapper.writeValueAsString(request))
                )
                .andDo(print());

        // api 테스트
        resultActions.andExpect(handler().handlerType(OrderController.class))
                .andExpect(jsonPath("$.resultCode").value("ok"))
                .andExpect(jsonPath("$.data.itemList[0].product.productIdx").value(100L))
                .andExpect(jsonPath("$.data.itemList[0].product.productName").value("테스트 상품1"))
                .andExpect(jsonPath("$.data.itemList[0].product.optionName").value("옵션A"))
                .andExpect(jsonPath("$.data.itemList[0].product.price").value("10000.0"))
                .andExpect(jsonPath("$.data.itemList[0].quantity").value(3))
                .andExpect(jsonPath("$.data.itemList[0].amount").value("30000.0"))
                .andExpect(jsonPath("$.data.itemsAmount").value("30000.0"));

        // 영속성 컨텍스트
        em.flush();  // DB에 반영(flush)
        em.clear();  // 캐시 비우기

        // entity
        OrderEntity orderEntity = em
                .createQuery("SELECT o FROM OrderEntity o ORDER BY o.id DESC", OrderEntity.class)
                .setMaxResults(1)
                .getSingleResult();
        List<OrderItemEntity> orderItems = orderEntity.getOrderItems();

        OrderItemEntity itemEntity = orderItems.stream()
                .filter(i -> i.getProductIdx() == 100L)
                .findFirst()
                .orElseThrow();

        List<PaymentEntity> entities = em
                .createQuery("SELECT p FROM PaymentEntity p WHERE p.ordersIdx = :orderIdx", PaymentEntity.class)
                .setParameter("orderIdx", orderEntity.getId())
                .getResultList();
        PaymentEntity paymentEntity = entities.get(0);

        // 실제 DB에 원하는 데이터가 들어갔는지 테스트
        // Order 테이블
        Assertions.assertThat(orderEntity.getUserIdx()).isEqualTo(1L);
        Assertions.assertThat(orderEntity.getAddressIdx()).isEqualTo(1L);
        Assertions.assertThat(orderEntity.getCardIdx()).isEqualTo(1L);
        Assertions.assertThat(orderEntity.getOrderCode()).isNotNull();
        Assertions.assertThat(orderEntity.getStatus()).isEqualTo(OrderStatus.PAID);
        Assertions.assertThat(orderEntity.getItemsAmount()).isEqualByComparingTo("30000.00");
        // OrderItem 테이블
        Assertions.assertThat(itemEntity.getProductIdx()).isEqualTo(100L);
        Assertions.assertThat(itemEntity.getOptionIdx()).isEqualTo(10L);
        Assertions.assertThat(itemEntity.getProductName()).isEqualTo("테스트 상품1");
        Assertions.assertThat(itemEntity.getOptionName()).isEqualTo("옵션A");
        Assertions.assertThat(itemEntity.getPrice()).isEqualByComparingTo("10000.00");
        Assertions.assertThat(itemEntity.getQuantity()).isEqualTo(3);
        // Payment 테이블
        Assertions.assertThat(paymentEntity.getOrdersIdx()).isEqualTo(orderEntity.getId());
        Assertions.assertThat(paymentEntity.getUsersIdx()).isEqualTo(1L);
        Assertions.assertThat(paymentEntity.getCardIdx()).isEqualTo(orderEntity.getCardIdx());
        Assertions.assertThat(paymentEntity.getStatus()).isEqualTo(PaymentStatus.PAYMENT);
        Assertions.assertThat(paymentEntity.getType()).isEqualTo(PaymentType.CARD);
        Assertions.assertThat(paymentEntity.getAmount()).isEqualByComparingTo("30000.00");
    }

    @Test
    @Transactional
    void 장바구니_주문_정상() throws Exception {

        // 장바구니에 데이터 집어넣기
        CartEntity cartItem1 = CartEntity.builder()
                .userIdx(1L)
                .productIdx(100L)
                .optionIdx(10L)
                .quantity(2)
                .price(new BigDecimal("20000.00"))
                .del(false)
                .build();
        cartRepository.save(cartItem1);

        CartEntity cartItem2 = CartEntity.builder()
                .userIdx(1L)
                .productIdx(101L)
                .optionIdx(11L)
                .quantity(3)
                .price(new BigDecimal("30000.00"))
                .del(false)
                .build();
        cartRepository.save(cartItem2);

        AddressInfo addressInfo = new AddressInfo(1L, "홍길동", "주소1", "상세1", "01012345678");
        CardInfo cardInfo = new CardInfo(1L, "브랜드", "카드이름", "card token", 12, 2029);
        UserData userData = new UserData(1L, addressInfo, cardInfo);

        OrderCartRequest body = new OrderCartRequest(userData, PaymentType.DEPOSIT);

        // 실제 상품 데이터 (상품 정보가 수정되어 장바구니의 데이터와 다른 상황)
        // 제품1 (100L, 10L, "테스트 상품1", "옵션A", new BigDecimal("10000.00"), 100)
        // 제품2 (101L, 11L, "테스트 상품2", "옵션B", new BigDecimal("15000.00"), 100)
        ResultActions resultActions = mvc
                .perform(
                        post("/orders/cart")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .header("X-USERS-IDX", "1")
                                .content(objectMapper.writeValueAsString(body))
                )
                .andDo(print());

        // 실제 상품 데이터로 결제가 진행
        // 상품A(10000 * 2) + 상품B(15000 * 3)
        resultActions.andExpect(status().isCreated())
                .andExpect(handler().handlerType(OrderController.class))
                .andExpect(jsonPath("$.resultCode").value("ok"))
                .andExpect(jsonPath("$.data.itemList[0].product.productIdx").value(100L))
                .andExpect(jsonPath("$.data.itemList[0].product.productName").value("테스트 상품1"))
                .andExpect(jsonPath("$.data.itemList[0].product.optionName").value("옵션A"))
                .andExpect(jsonPath("$.data.itemList[0].product.price").value("10000.0"))
                .andExpect(jsonPath("$.data.itemList[0].quantity").value(2))
                .andExpect(jsonPath("$.data.itemList[0].amount").value("20000.0"))
                .andExpect(jsonPath("$.data.itemList[1].product.productIdx").value(101L))
                .andExpect(jsonPath("$.data.itemList[1].product.productName").value("테스트 상품2"))
                .andExpect(jsonPath("$.data.itemList[1].product.optionName").value("옵션B"))
                .andExpect(jsonPath("$.data.itemList[1].product.price").value("15000.0"))
                .andExpect(jsonPath("$.data.itemList[1].quantity").value(3))
                .andExpect(jsonPath("$.data.itemList[1].amount").value("45000.0"))
                .andExpect(jsonPath("$.data.itemsAmount").value("65000.0"));

        // 영속성 컨텍스트의 변경 내용을 DB에 반영(flush), 캐시를 비우기(clear)
        em.flush();
        em.clear();

        // entity
        OrderEntity orderEntity = em.createQuery(
                "SELECT o FROM OrderEntity o ORDER BY o.id DESC", OrderEntity.class)
                .setMaxResults(1)
                .getSingleResult();

        List<OrderItemEntity> items = orderEntity.getOrderItems();
        OrderItemEntity itemEntity1 = items.stream()
                .filter(i -> i.getProductIdx() == 100L)
                .findFirst()
                .orElseThrow();
        OrderItemEntity itemEntity2 = items.stream()
                .filter(i -> i.getProductIdx() == 101L)
                .findFirst()
                .orElseThrow();

        // Order 테이블
        Assertions.assertThat(orderEntity.getUserIdx()).isEqualTo(1L);
        Assertions.assertThat(orderEntity.getAddressIdx()).isEqualTo(1L);
        Assertions.assertThat(orderEntity.getCardIdx()).isEqualTo(1L);
        Assertions.assertThat(orderEntity.getOrderCode()).isNotNull();
        Assertions.assertThat(orderEntity.getStatus()).isEqualTo(OrderStatus.PAID);
        Assertions.assertThat(orderEntity.getItemsAmount()).isEqualByComparingTo("65000.00");

        // OrderItem 테이블
        Assertions.assertThat(itemEntity1.getProductIdx()).isEqualTo(100L);
        Assertions.assertThat(itemEntity1.getOptionIdx()).isEqualTo(10L);
        Assertions.assertThat(itemEntity1.getProductName()).isEqualTo("테스트 상품1");
        Assertions.assertThat(itemEntity1.getOptionName()).isEqualTo("옵션A");
        Assertions.assertThat(itemEntity1.getPrice()).isEqualByComparingTo("10000.00");
        Assertions.assertThat(itemEntity1.getQuantity()).isEqualTo(2);

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
    void 환불_처리_정상() throws Exception {

        String refundOrderCode = "Test_Order_Code";

        // 환불 처리를 위한 데이터 추가
        OrderEntity order = OrderEntity.builder()
                .userIdx(1L)
                .addressIdx(1L)
                .cardIdx(1L)
                .orderCode(refundOrderCode)
                .status(OrderStatus.CREATED)
                .itemsAmount(new BigDecimal("20000.00"))
                .del(false)
                .build();
        orderRepository.save(order);

        OrderItemEntity itemEntity = OrderItemEntity.builder()
                .order(order)
                .productIdx(100L)
                .optionIdx(10L)
                .productName("테스트 상품")
                .optionName("옵션C")
                .price(new BigDecimal("10000.00"))
                .quantity(2)
                .del(false)
                .build();
        orderItemRepository.save(itemEntity);

        OrderEntity orderEntity = orderRepository.findByOrderCode(refundOrderCode).get();
        PaymentEntity payment = PaymentEntity.builder()
                .usersIdx(1L)
                .ordersIdx(orderEntity.getId())
                .amount(new BigDecimal("20000.00"))
                .status(PaymentStatus.PAYMENT)
                .type(PaymentType.CARD)
                .build();
        paymentRepository.save(payment);


        ResultActions resultActions = mvc
                .perform(
                        post("/orders/refund/" + refundOrderCode)
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .header("X-USERS-IDX", "1")
                ).andDo(print());

        resultActions.andExpect(status().isOk())
                .andExpect(handler().handlerType(OrderController.class))
                .andExpect(jsonPath("$.resultCode").value("ok"))
                .andExpect(jsonPath("$.data").value("환불처리가 완료 되었습니다."));

        OrderEntity updatedOrder = orderRepository.findByOrderCode(refundOrderCode).get();
        Assertions.assertThat(orderEntity.getStatus()).isEqualTo(OrderStatus.REFUNDED);

        List<PaymentEntity> payments = paymentRepository.findAll();
        PaymentEntity refund = payments.stream()
                .filter(p -> p.getOrdersIdx().equals(updatedOrder.getId()))
                .filter(p -> p.getStatus() == PaymentStatus.REFUND)
                .findFirst().get();

        Assertions.assertThat(refund.getStatus()).isEqualTo(PaymentStatus.REFUND);
    }

    @Test
    @Transactional
    void 주문_목록_조회_정상() throws Exception {

        // 주문 데이터 추가
        OrderEntity order2 = OrderEntity.builder()
                .userIdx(1L)
                .addressIdx(1L)
                .cardIdx(1L)
                .orderCode(UUID.randomUUID().toString())
                .status(OrderStatus.CREATED)
                .itemsAmount(new BigDecimal("20000.00"))
                .del(false)
                .build();
        orderRepository.save(order2);

        OrderItemEntity itemC = OrderItemEntity.builder()
                .order(order2)
                .productIdx(100L)
                .optionIdx(10L)
                .productName("테스트 상품") // 상품 C
                .optionName("옵션C")
                .price(new BigDecimal("10000.00"))
                .quantity(2)
                .del(false)
                .build();
        orderItemRepository.save(itemC);

        // order1 : 상품A (1L, 1L, "주문한 상품1", "옵션A", 1000, 2) / 상품B(2L, 2L, 2L, "주문한 상품2", "옵션B", 5000, 3)
        // order2 : 상품C (100L, 10L, "테스트 상품", "옵션C", 10000, 2)
        ResultActions resultActions = mvc
                .perform(
                        get("/orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .header("X-USERS-IDX", "1")
                )
                .andDo(print());

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("ok"))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].itemList[0].product.productIdx").value(1L))
                .andExpect(jsonPath("$.data[0].itemList[0].product.optionIdx").value(1L))
                .andExpect(jsonPath("$.data[0].itemList[0].product.productName").value("주문한 상품1"))
                .andExpect(jsonPath("$.data[0].itemList[0].product.optionName").value("옵션A"))
                .andExpect(jsonPath("$.data[0].itemList[0].product.price").value("1000.0"))
                .andExpect(jsonPath("$.data[0].itemList[0].quantity").value(2))
                .andExpect(jsonPath("$.data[0].itemList[0].amount").value("2000.0"))

                .andExpect(jsonPath("$.data[0].itemList[1].product.productIdx").value(2L))
                .andExpect(jsonPath("$.data[0].itemList[1].product.optionIdx").value(2L))
                .andExpect(jsonPath("$.data[0].itemList[1].product.productName").value("주문한 상품2"))
                .andExpect(jsonPath("$.data[0].itemList[1].product.optionName").value("옵션B"))
                .andExpect(jsonPath("$.data[0].itemList[1].product.price").value("5000.0"))
                .andExpect(jsonPath("$.data[0].itemList[1].quantity").value(3))
                .andExpect(jsonPath("$.data[0].itemList[1].amount").value("15000.0"))
                .andExpect(jsonPath("$.data[0].itemsAmount").value("17000.0"))

                .andExpect(jsonPath("$.data[1].itemList[0].product.productIdx").value(100L))
                .andExpect(jsonPath("$.data[1].itemList[0].product.optionIdx").value(10L))
                .andExpect(jsonPath("$.data[1].itemList[0].product.productName").value("테스트 상품"))
                .andExpect(jsonPath("$.data[1].itemList[0].product.optionName").value("옵션C"))
                .andExpect(jsonPath("$.data[1].itemList[0].product.price").value("10000.0"))
                .andExpect(jsonPath("$.data[1].itemList[0].quantity").value(2))
                .andExpect(jsonPath("$.data[1].itemList[0].amount").value("20000.0"))
                .andExpect(jsonPath("$.data[1].itemsAmount").value("20000.0"));

        // 주문건수 2개
        List<OrderEntity> orderEntities = orderRepository.findAllByUserIdx(1L);
        Assertions.assertThat(orderEntities).hasSize(2);
    }

    @Test
    @Transactional
    void 주문_상세_조회_정상() throws Exception {

        ResultActions resultActions = mvc
                .perform(
                        get("/orders/" + setOrderCode)
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .header("X-USERS-IDX", "1")
                )
                .andDo(print());
    }




    /**
     * ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ
     * 실패 테스트 케이스
     * - 재고 없음
     * - 상품 없음
     * - 주문정보 없음
     * ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ
     */

    @Test
    @Transactional
    void 단일상품_주문_실패_재고없음 () throws Exception {

        // 요청 전 개수 확인
        long beforeOrderCount = orderRepository.count();
        long beforeItemCount = orderItemRepository.count();

        // 200개 주문했을 때 (재고는 100개)
        OrderItem orderItem = new OrderItem(100L, 10L, 200);
        AddressInfo addressInfo = new AddressInfo(2L, "홍길동", "주소1", "상세1", "01012345678");
        CardInfo cardInfo = new CardInfo(3L, "브랜드", "카드이름", "card token", 12, 2029);
        UserData userData = new UserData(1L, addressInfo, cardInfo);

        OrderDirectRequest request = new OrderDirectRequest(orderItem, userData, PaymentType.TOSS_PAY);

        ResultActions resultActions = mvc
                .perform(
                        post("/orders")
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

        // 요청 후 변화
        Assertions.assertThat(orderRepository.count()).isEqualTo(beforeOrderCount);
        Assertions.assertThat(orderItemRepository.count()).isEqualTo(beforeItemCount);
    }

    @Test
    @Transactional
    void 단일상품_주문_실패_상품없음 () throws Exception {

        // 요청 전 개수 확인
        long beforeOrderCount = orderRepository.count();
        long beforeItemCount = orderItemRepository.count();

        // 103L/10L 으로 찾았을 때 Product Not Found request 할 가짜 응답
        Request rq = Request.create(Request.HttpMethod.GET, "url", Collections.emptyMap(), null, null, null);
        given(productClient.getProduct(103L, 10L))
                .willThrow(new FeignException.NotFound("Product Not Found", rq, null, null));

        OrderItem orderItem = new OrderItem(103L, 10L, 3);
        AddressInfo addressInfo = new AddressInfo(2L, "홍길동", "주소1", "상세1", "01012345678");
        CardInfo cardInfo = new CardInfo(3L,"브랜드", "카드이름", "card token", 12, 2029);
        UserData userData = new UserData(1L, addressInfo, cardInfo);

        OrderDirectRequest request = new OrderDirectRequest(orderItem, userData, PaymentType.TOSS_PAY);

        ResultActions resultActions = mvc
                .perform(
                        post("/orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .header("X-USERS-IDX", "1")
                                .content(objectMapper.writeValueAsString(request))
                )
                .andDo(print());

        resultActions.andExpect(status().is4xxClientError())
                .andExpect(handler().handlerType(OrderController.class))
                .andExpect(jsonPath("$.resultCode").value("PRODUCT_NOT_FOUND"));

        // 요청 후 변화
        Assertions.assertThat(orderRepository.count()).isEqualTo(beforeOrderCount);
        Assertions.assertThat(orderItemRepository.count()).isEqualTo(beforeItemCount);
    }

    @Test
    @Transactional
    void 단일상품_주문_실패_주소정보_없음 () throws Exception {

        // 요청 전 개수 확인
        long beforeOrderCount = orderRepository.count();
        long beforeItemCount = orderItemRepository.count();

        OrderItem orderItem = new OrderItem(100L, 10L, 3);
        AddressInfo addressInfo = null;
        CardInfo cardInfo = new CardInfo(1L,"브랜드", "카드이름", "card token", 12, 2029);
        UserData userData = new UserData(1L, addressInfo, cardInfo);
        OrderDirectRequest request = new OrderDirectRequest(orderItem, userData, PaymentType.TOSS_PAY);

        UserData noAddress = new UserData(1L, addressInfo, cardInfo);
        given(userClient.getUserData(eq(1L), any())).willReturn(noAddress);

        ResultActions resultActions = mvc
                .perform(
                        post("/orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .header("X-USERS-IDX", "1")
                                .content(objectMapper.writeValueAsString(request))
                )
                .andDo(print());

        resultActions.andExpect(status().is4xxClientError())
                .andExpect(handler().handlerType(OrderController.class))
                .andExpect(jsonPath("$.resultCode").value("BAD_REQUEST_VALID"))
                .andExpect(jsonPath("$.data").value("주소 정보는 필수입니다."));

        // 요청 후 변화
        Assertions.assertThat(orderRepository.count()).isEqualTo(beforeOrderCount);
        Assertions.assertThat(orderItemRepository.count()).isEqualTo(beforeItemCount);
    }
}