package co.kr.order.controller;

import co.kr.order.HelperMethod;
import co.kr.order.model.dto.OrderItem;
import co.kr.order.model.dto.ProductInfo;
import co.kr.order.model.dto.request.OrderCartRequest;
import co.kr.order.model.dto.request.OrderDirectRequest;
import co.kr.order.model.dto.request.ProductRequest;
import co.kr.order.model.entity.*;
import co.kr.order.model.vo.OrderStatus;
import co.kr.order.model.vo.PaymentStatus;
import co.kr.order.model.vo.PaymentType;
import feign.FeignException;
import feign.Request;
import org.assertj.core.api.Assertions;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OrderControllerTest extends HelperMethod {

    String tossKey = UUID.randomUUID().toString();

    OrderEntity order1;
    OrderItemEntity itemO1I1;
    PaymentEntity payment1;
    SettlementHistoryEntity settlement1;

    OrderEntity order2;
    OrderItemEntity itemO2I1;
    OrderItemEntity itemO2I2;
    PaymentEntity payment2;
    SettlementHistoryEntity settlement2;

    @BeforeEach
    void setUp() {
        // given:
        ProductInfo productInfo1 = createProductInfo(100L, 10L, 1L, "상품1", "옵션A", 1000, 100);
        ProductInfo productInfo2 = createProductInfo(101L, 11L, 2L, "상품2", "옵션B", 2000, 80);
        ProductInfo productInfo3 = createProductInfo(102L, 12L, 2L, "상품2", "옵션C", 2000, 50);
        ProductInfo productInfo4 = createProductInfo(103L, 13L, 3L, "상품3", "옵션D", 15000, 0);

        given(productClient.getProduct(100L, 10L)).willReturn(productInfo1);
        given(productClient.getProduct(101L, 11L)).willReturn(productInfo2);

        // 카트 주문용
        ProductRequest item1 = new ProductRequest(100L, 10L);
        ProductRequest item2 = new ProductRequest(101L, 11L);
        given(productClient.getProductList(List.of(item1, item2))).willReturn(List.of(productInfo1, productInfo2));

        /**
         * 유저 결제 목록
         */
        // 단일 상품 1개 구매
        order1 = createOrder(1L, null, null, 2000, 2000);
        itemO1I1 = createItem(order1, productInfo1.productIdx(), productInfo1.optionIdx(), productInfo1.productName(), productInfo1.optionName(), productInfo1.price(), 2);
        payment1 = createPayment(order1.getUserIdx(), order1.getId(), null, PaymentType.DEPOSIT, order1.getTotalAmount(), null);
        settlement1 = createSettlement(productInfo1.sellerIdx(), payment1.getPaymentIdx(), order1.getTotalAmount());

        // 단일 옵션 종류별 구매
        order2 = createOrder(1L, null, null, 10000, 10000);
        itemO2I1 = createItem(order2, productInfo2.productIdx(), productInfo2.optionIdx(), productInfo2.productName(), productInfo2.optionName(), productInfo2.price(), 2);
        itemO2I2 = createItem(order2, productInfo3.productIdx(), productInfo3.optionIdx(), productInfo3.productName(), productInfo3.optionName(), productInfo3.price(), 3);
        payment2 = createPayment(order2.getUserIdx(), order2.getId(), null, PaymentType.DEPOSIT, order2.getTotalAmount(), null);
        settlement2 = createSettlement(productInfo2.sellerIdx(), payment2.getPaymentIdx(), order2.getTotalAmount());

        em.flush();
        em.clear();
    }



    /*
     * ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ
     * 정상 케이스 테스트
     * - 단일상품 주문 (토스페이먼츠, 예치금, 카드)
     * - 장바구니 주문
     * - 주문 조회
     * - 주문 상세 조회
     * - 주문 확정
     * ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ
     */

    // 토스 주문 테스트는 외부 api 통신이라 확인을 바로 확인 불가능

    @Test
    @Transactional
    void 단일상품_주문_정상_카드 () throws Exception {

        OrderItem orderItem = new OrderItem(100L, 10L, 5);

        // 요청 body
        OrderDirectRequest request = new OrderDirectRequest(
                orderItem,
                PaymentType.CARD,
                null
        );

        // 응답 테스트
        ResultActions resultActions = mvc.perform(
                        post("/orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .header("X-USERS-IDX", "1")
                                .content(om.writeValueAsString(request))
                )
                .andDo(print());

        // api 테스트
        resultActions.andExpect(handler().handlerType(OrderController.class))
                .andExpect(jsonPath("$.resultCode").value("ok"))
                .andExpect(jsonPath("$.data.itemList[0].product.productIdx").value(100L))
                .andExpect(jsonPath("$.data.itemList[0].product.price").value("1000"))
                .andExpect(jsonPath("$.data.itemsAmount").value("5000"));

        // 영속성 컨텍스트
        em.flush();  // DB에 반영(flush)
        em.clear();  // 캐시 비우기

        // JPQL로 방금 생성된 OrderEntity 조회 (가장 최근 ID)
        OrderEntity order = em.createQuery(
                        "SELECT o FROM OrderEntity o ORDER BY o.id DESC", OrderEntity.class)
                .setMaxResults(1)
                .getSingleResult();

        // OrderItemEntity 추출
        OrderItemEntity item = order.getOrderItems().stream()
                .filter(i -> i.getProductIdx() == 100L)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("주문 상품이 저장되지 않았습니다."));

        // Order 테이블
        assertThat(order.getUserIdx()).isEqualTo(1L);
        assertThat(order.getAddressIdx()).isNull();
        assertThat(order.getCardIdx()).isNull();
        assertThat(order.getOrderCode()).isNotNull();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
        assertThat(order.getItemsAmount()).isEqualByComparingTo("5000.00");

        // OrderItem 테이블
        assertThat(item.getProductIdx()).isEqualTo(100L);
        assertThat(item.getOptionIdx()).isEqualTo(10L);
        assertThat(item.getProductName()).isEqualTo("상품1");
        assertThat(item.getOptionName()).isEqualTo("옵션A");
        assertThat(item.getPrice()).isEqualByComparingTo("1000.00");
        assertThat(item.getQuantity()).isEqualTo(5);

        // payment 테이블
        PaymentEntity payment = paymentRepository.findByOrdersIdx(order.getId()).get();
        assertThat(payment.getUsersIdx()).isEqualTo(1L);
        assertThat(payment.getOrdersIdx()).isEqualTo(order.getId());
        assertThat(payment.getCardIdx()).isNull();
        assertThat(payment.getType()).isEqualTo(PaymentType.CARD);
        assertThat(payment.getAmount()).isEqualByComparingTo("5000.00");
        assertThat(payment.getPaymentKey()).isNull();

        // settlement 테이블
        SettlementHistoryEntity settlement = settlementRepository.findBySellerIdxAndPaymentIdx(1L, payment.getPaymentIdx());
        assertThat(settlement.getSellerIdx()).isEqualTo(1L);
        assertThat(settlement.getPaymentIdx()).isEqualTo(payment.getPaymentIdx());
        assertThat(settlement.getAmount()).isEqualByComparingTo("5000.00");
    }


    @Test
    @Transactional
    void 단일상품_주문_정상_예치금 () throws Exception {

        OrderItem orderItem = new OrderItem(100L, 10L, 3);

        // 요청 body
        OrderDirectRequest request = new OrderDirectRequest(
                orderItem,
                PaymentType.DEPOSIT,
                null
        );

        // 응답 테스트
        ResultActions resultActions = mvc.perform(
                        post("/orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .header("X-USERS-IDX", "1")
                                .content(om.writeValueAsString(request))
                )
                .andDo(print());

        // api 테스트
        resultActions.andExpect(handler().handlerType(OrderController.class))
                .andExpect(jsonPath("$.resultCode").value("ok"))
                .andExpect(jsonPath("$.data.itemList[0].product.productIdx").value(100L))
                .andExpect(jsonPath("$.data.itemList[0].product.price").value("1000"))
                .andExpect(jsonPath("$.data.itemsAmount").value("3000"));

        // 영속성 컨텍스트
        em.flush();  // DB에 반영(flush)
        em.clear();  // 캐시 비우기

        // JPQL로 방금 생성된 OrderEntity 조회 (가장 최근 ID)
        OrderEntity order = em.createQuery(
                        "SELECT o FROM OrderEntity o ORDER BY o.id DESC", OrderEntity.class)
                .setMaxResults(1)
                .getSingleResult();

        // OrderItemEntity 추출
        OrderItemEntity item = order.getOrderItems().stream()
                .filter(i -> i.getProductIdx() == 100L)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("주문 상품이 저장되지 않았습니다."));


        // Order 테이블
        assertThat(order.getUserIdx()).isEqualTo(1L);
        assertThat(order.getAddressIdx()).isNull();
        assertThat(order.getCardIdx()).isNull();
        assertThat(order.getOrderCode()).isNotNull();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
        assertThat(order.getItemsAmount()).isEqualByComparingTo("3000.00");

        // OrderItem 테이블
        assertThat(item.getProductIdx()).isEqualTo(100L);
        assertThat(item.getOptionIdx()).isEqualTo(10L);
        assertThat(item.getProductName()).isEqualTo("상품1");
        assertThat(item.getOptionName()).isEqualTo("옵션A");
        assertThat(item.getPrice()).isEqualByComparingTo("1000.00");
        assertThat(item.getQuantity()).isEqualTo(3);

        // payment 테이블
        PaymentEntity payment = paymentRepository.findByOrdersIdx(order.getId()).get();
        assertThat(payment.getUsersIdx()).isEqualTo(1L);
        assertThat(payment.getOrdersIdx()).isEqualTo(order.getId());
        assertThat(payment.getCardIdx()).isNull();
        assertThat(payment.getType()).isEqualTo(PaymentType.DEPOSIT);
        assertThat(payment.getAmount()).isEqualByComparingTo("3000.00");
        assertThat(payment.getPaymentKey()).isNull();

        // settlement 테이블
        SettlementHistoryEntity settlement = settlementRepository.findBySellerIdxAndPaymentIdx(1L, payment.getPaymentIdx());
        assertThat(settlement.getSellerIdx()).isEqualTo(1L);
        assertThat(settlement.getPaymentIdx()).isEqualTo(payment.getPaymentIdx());
        assertThat(settlement.getAmount()).isEqualByComparingTo("3000.00");
    }

    @Test
    @Transactional
    void 장바구니_주문_정상() throws Exception {

        // 카트 Entity 생성
        createCart(1L, 100L, 10L, new BigDecimal(1200), 5);
        createCart(1L, 101L, 11L, new BigDecimal(1000), 2);

        given(productClient.getSellersByProductIds(any())).willReturn(java.util.Map.of(100L, 1L, 101L, 2L));

        OrderCartRequest body = new OrderCartRequest(
                PaymentType.DEPOSIT,
                null
        );

        ResultActions resultActions = mvc
                .perform(
                        post("/orders/cart")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .header("X-USERS-IDX", "1")
                                .content(om.writeValueAsString(body))
                )
                .andDo(print());

        // 실제 상품 데이터로 결제가 진행 (카트 정보로 x)
        // 상품A(1000 * 5) + 상품B(2000 * 2)
        resultActions.andExpect(status().isCreated())
                .andExpect(handler().handlerType(OrderController.class))
                .andExpect(jsonPath("$.resultCode").value("ok"))
                .andExpect(jsonPath("$.data.itemsAmount").value("9000"));

        em.flush();
        em.clear();

        // entity
        OrderEntity order = em.createQuery(
                        "SELECT o FROM OrderEntity o ORDER BY o.id DESC", OrderEntity.class)
                .setMaxResults(1)
                .getSingleResult();

        List<OrderItemEntity> items = order.getOrderItems();


        // Order 테이블
        assertThat(order.getUserIdx()).isEqualTo(1L);
        assertThat(order.getOrderCode()).isNotNull();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
        assertThat(order.getItemsAmount()).isEqualByComparingTo("9000.00");

        OrderItemEntity item1 = items.get(0);
        assertThat(item1.getProductIdx()).isEqualTo(100L);
        assertThat(item1.getOptionIdx()).isEqualTo(10L);
        assertThat(item1.getProductName()).isEqualTo("상품1");
        assertThat(item1.getOptionName()).isEqualTo("옵션A");
        assertThat(item1.getPrice()).isEqualByComparingTo("1000");
        assertThat(item1.getQuantity()).isEqualTo(5);

        OrderItemEntity item2 = items.get(1);
        assertThat(item2.getProductIdx()).isEqualTo(101L);
        assertThat(item2.getOptionIdx()).isEqualTo(11L);
        assertThat(item2.getProductName()).isEqualTo("상품2");
        assertThat(item2.getOptionName()).isEqualTo("옵션B");
        assertThat(item2.getPrice()).isEqualByComparingTo("2000");
        assertThat(item2.getQuantity()).isEqualTo(2);

        // payment 테이블
        PaymentEntity payment = paymentRepository.findByOrdersIdx(order.getId()).get();
        assertThat(payment.getUsersIdx()).isEqualTo(1L);
        assertThat(payment.getOrdersIdx()).isEqualTo(order.getId());
        assertThat(payment.getCardIdx()).isNull();
        assertThat(payment.getType()).isEqualTo(PaymentType.DEPOSIT);
        assertThat(payment.getAmount()).isEqualByComparingTo("9000");
        assertThat(payment.getPaymentKey()).isNull();

        // settlement 테이블
        SettlementHistoryEntity settlement1 = settlementRepository.findBySellerIdxAndPaymentIdx(1L, payment.getPaymentIdx());
        assertThat(settlement1.getSellerIdx()).isEqualTo(1L);
        assertThat(settlement1.getPaymentIdx()).isEqualTo(payment.getPaymentIdx());
        assertThat(settlement1.getAmount()).isEqualByComparingTo("5000");

        SettlementHistoryEntity settlement2 = settlementRepository.findBySellerIdxAndPaymentIdx(2L, payment.getPaymentIdx());
        assertThat(settlement2.getSellerIdx()).isEqualTo(2L);
        assertThat(settlement2.getPaymentIdx()).isEqualTo(payment.getPaymentIdx());
        assertThat(settlement2.getAmount()).isEqualByComparingTo("4000");

        // 장바구니 비워졌는지 확인
        List<CartEntity> cartEntity = cartRepository.findAll();
        assertThat(cartEntity).isEmpty();
    }

    @Test
    @Transactional
    void 환불_처리_정상() throws Exception {

        String refundOrderCode = order1.getOrderCode();

        // 주문1의 환불 진행
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
        assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.REFUNDED);

        List<PaymentEntity> payments = paymentRepository.findAll();
        PaymentEntity refund = payments.stream()
                .filter(p -> p.getOrdersIdx().equals(updatedOrder.getId()))
                .filter(p -> p.getStatus() == PaymentStatus.REFUND)
                .findFirst().get();

        assertThat(refund.getStatus()).isEqualTo(PaymentStatus.REFUND);
    }

    @Test
    @Transactional
    void 주문_목록_조회_정상() throws Exception {

        ResultActions resultActions = mvc
                .perform(
                        get("/orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .header("X-USERS-IDX", "1")
                                .param("page", "0")
                                .param("size", "10")
                                .param("sort", "createdAt,desc")
                )
                .andDo(print());

        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("ok"))
                .andExpect(jsonPath("$.data.content[0].itemList[0].product.productIdx").value(itemO1I1.getProductIdx()))
                .andExpect(jsonPath("$.data.content[0].itemList[0].product.optionIdx").value(itemO1I1.getOptionIdx()))
                .andExpect(jsonPath("$.data.content[0].itemList[0].product.productName").value(itemO1I1.getProductName()))
                .andExpect(jsonPath("$.data.content[0].itemList[0].product.optionName").value(itemO1I1.getOptionName()))
                .andExpect(jsonPath("$.data.content[0].itemList[0].product.price").value(Matchers.comparesEqualTo(itemO1I1.getPrice().doubleValue())))
                .andExpect(jsonPath("$.data.content[0].orderCode").value(order1.getOrderCode()))
                .andExpect(jsonPath("$.data.content[0].itemsAmount").value(order1.getItemsAmount().doubleValue()))

                .andExpect(jsonPath("$.data.content[1].itemList[0].product.productIdx").value(itemO2I1.getProductIdx()))
                .andExpect(jsonPath("$.data.content[1].itemList[0].product.optionIdx").value(itemO2I1.getOptionIdx()))
                .andExpect(jsonPath("$.data.content[1].itemList[0].product.productName").value(itemO2I1.getProductName()))
                .andExpect(jsonPath("$.data.content[1].itemList[0].product.optionName").value(itemO2I1.getOptionName()))
                .andExpect(jsonPath("$.data.content[1].itemList[0].product.price").value(Matchers.comparesEqualTo(itemO2I1.getPrice().doubleValue())))
                .andExpect(jsonPath("$.data.content[1].itemList[1].product.productIdx").value(itemO2I2.getProductIdx()))
                .andExpect(jsonPath("$.data.content[1].itemList[1].product.optionIdx").value(itemO2I2.getOptionIdx()))
                .andExpect(jsonPath("$.data.content[1].itemList[1].product.productName").value(itemO2I2.getProductName()))
                .andExpect(jsonPath("$.data.content[1].itemList[1].product.optionName").value(itemO2I2.getOptionName()))
                .andExpect(jsonPath("$.data.content[1].itemList[1].product.price").value(Matchers.comparesEqualTo(itemO2I2.getPrice().doubleValue())))
                .andExpect(jsonPath("$.data.content[1].orderCode").value(order2.getOrderCode()))
                .andExpect(jsonPath("$.data.content[1].itemsAmount").value(order2.getItemsAmount().doubleValue()));

        Pageable page = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));

        // 주문건수 2개
        Page<OrderEntity> orderEntities = orderRepository.findAllByUserIdx(1L, page);
        Assertions.assertThat(orderEntities).hasSize(2);
        Assertions.assertThat(orderEntities.getTotalElements()).isEqualTo(2);
    }

    @Test
    @Transactional
    void 주문_상세_조회_정상() throws Exception {

        ResultActions resultActions = mvc
                .perform(
                        get("/orders/" + order1.getOrderCode())
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .header("X-USERS-IDX", "1")
                )
                .andDo(print());

        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("ok"))
                .andExpect(jsonPath("$.data.orderCode").value(order1.getOrderCode()));
    }



    /**
     * ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ
     * 실패 테스트 케이스
     * - 재고 없음
     * - 상품 없음
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

        OrderDirectRequest request = new OrderDirectRequest(
                orderItem,
                PaymentType.TOSS_PAY,
                UUID.randomUUID().toString()
        );

        ResultActions resultActions = mvc
                .perform(
                        post("/orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .header("X-USERS-IDX", "1")
                                .content(om.writeValueAsString(request))
                )
                .andDo(print());

        resultActions.andExpect(status().is4xxClientError())
                .andExpect(handler().handlerType(OrderController.class))
                .andExpect(jsonPath("$.resultCode").value("OUT_OF_STOCK"))
                .andExpect(jsonPath("$.data").value("재고가 부족합니다."));

        // 요청 후 변화 없음 확인
        assertThat(orderRepository.count()).isEqualTo(beforeOrderCount);
        assertThat(orderItemRepository.count()).isEqualTo(beforeItemCount);
    }

    @Test
    @Transactional
    void 단일상품_주문_실패_상품없음 () throws Exception {

        // 요청 전 개수 확인
        long beforeOrderCount = orderRepository.count();
        long beforeItemCount = orderItemRepository.count();

        // 103L/10L 으로 찾았을 때 Product Not Found request 할 가짜 응답
        Request rq = Request.create(Request.HttpMethod.GET, "url", Collections.emptyMap(), null, null, null);
        given(productClient.getProduct(103L, 10L)).willThrow(new FeignException.NotFound("Product Not Found", rq, null, null));

        OrderItem orderItem = new OrderItem(103L, 10L, 3);

        OrderDirectRequest request = new OrderDirectRequest(
                orderItem,
                PaymentType.TOSS_PAY,
                UUID.randomUUID().toString()
        );

        ResultActions resultActions = mvc
                .perform(
                        post("/orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .header("X-USERS-IDX", "1")
                                .content(om.writeValueAsString(request))
                )
                .andDo(print());

        resultActions.andExpect(status().is4xxClientError())
                .andExpect(handler().handlerType(OrderController.class))
                .andExpect(jsonPath("$.resultCode").value("PRODUCT_NOT_FOUND"));

        // 요청 후 변화 없음 확인
        assertThat(orderRepository.count()).isEqualTo(beforeOrderCount);
        assertThat(orderItemRepository.count()).isEqualTo(beforeItemCount);
    }
}