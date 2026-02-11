package co.kr.order.service.impl;

import co.kr.order.client.PaymentClient;
import co.kr.order.client.ProductClient;
import co.kr.order.exception.ErrorCode;
import co.kr.order.exception.OrderNotFoundException;
import co.kr.order.model.dto.ItemInfo;
import co.kr.order.model.dto.ProductInfo;
import co.kr.order.model.dto.event.StockUpdateEvent;
import co.kr.order.model.dto.event.StockUpdateMsg;
import co.kr.order.model.dto.request.ClientPaymentReq;
import co.kr.order.model.dto.request.ClientProductReq;
import co.kr.order.model.dto.request.ClientRefundReq;
import co.kr.order.model.dto.request.OrderReq;
import co.kr.order.model.dto.response.ClientPaymentRes;
import co.kr.order.model.dto.response.ClientProductRes;
import co.kr.order.model.dto.response.OrderItemRes;
import co.kr.order.model.dto.response.OrderRes;
import co.kr.order.model.entity.OrderEntity;
import co.kr.order.model.entity.OrderItemEntity;
import co.kr.order.model.vo.OrderStatus;
import co.kr.order.model.vo.OrderType;
import co.kr.order.repository.OrderItemJpaRepository;
import co.kr.order.repository.OrderJpaRepository;
import co.kr.order.service.CartService;
import co.kr.order.service.DeductStockService;
import co.kr.order.service.OrderService;
import co.kr.order.service.SettlementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderJpaRepository orderRepository;
    private final OrderItemJpaRepository orderItemRepository;

    private final CartService cartService;
    private final DeductStockService deductStockService;
    private final SettlementService settlementService;
    private final ApplicationEventPublisher eventPublisher;

    private final PaymentClient paymentClient;
    private final ProductClient productClient;

    /*
     * 주문 생성
     * @param userIdx: 유저 인덱스
     * @param request: 주문 요청 정보
     */
    @Transactional
    @Override
    public OrderRes createOrder(Long userIdx, OrderReq request) {

        String orderCode = UUID.randomUUID().toString();  // 주문 코드 (UUID)

        List<OrderItemEntity> tempOrderItems = new ArrayList<>(); // 임시 주문한 상품 저장 리스트
        List<ProductInfo> stocksInfos = new ArrayList<>();  // 재고 차감 요청 정보 리스트
        Map<Long, BigDecimal> sellerSettlementMap = new HashMap<>();  // 판매자별 정산금 집계 Map

        // =================================================================
        // 주문 상품 데이터 준비
        // =================================================================
        switch (request.orderType()) {
            case OrderType.DIRECT:  // 직접 주문했을 때 (단건)

                // 제품 정보 가져옴 (FeignClient)
                ClientProductRes product = productClient.getProduct(
                        request.productInfo().productCode(),
                        request.productInfo().optionCode()
                );

                // 주문한 상품 엔티티 생성 및 추가
                tempOrderItems.add(createOrderItemEntity(product, request.productInfo().quantity()));

                // 재고 차감할 데이터 세팅
                stocksInfos.add(
                        new ProductInfo(
                            request.productInfo().productCode(),
                            request.productInfo().optionCode(),
                            request.productInfo().quantity()
                        )
                );

                // 정산 데이터 수집 (판매자 별 정산금 계산)
                // Map [Key: SellerIdx, Value: 정산금] -> 기존 값에 현재 상품 금액 누적 (merge 활용)
                BigDecimal directAmount = product.price().multiply(BigDecimal.valueOf(request.productInfo().quantity()));
                sellerSettlementMap.merge(product.sellerIdx(), directAmount, BigDecimal::add);

                break;

            case OrderType.CART:  // 장바구니로 주문했을 때 (다건)

                // 장바구니 상품 옵션별 수량 조회 (Map<OptionIdx, Quantity>)
                Map<Long, Integer> quantityMap = cartService.getCartItemQuantities(userIdx);

                // 장바구니 상품 정보 조회를 위한 요청 세팅
                List<ClientProductReq> productRequest = cartService.getProductByCart(userIdx);
                // FeignClient로 장바구니에 담긴 상품들의 상세 정보 조회
                List<ClientProductRes> productsResponse = productClient.getProductList(productRequest);

                // 가져온 상품 정보 순회
                for (ClientProductRes productRes : productsResponse) {

                    // optionIdx를 기준으로 주문 수량 조회 (없으면 0)
                    Integer quantity = quantityMap.getOrDefault(productRes.optionIdx(), 0);
                    if (quantity > 0) {
                        // 주문한 상품 엔티티 생성 및 추가
                        tempOrderItems.add(createOrderItemEntity(productRes, quantity));

                        // 재고 차감할 데이터 세팅
                        stocksInfos.add(
                                new ProductInfo(
                                        productRes.productCode(),
                                        productRes.optionCode(),
                                        quantity
                                )
                        );

                        // 정산 데이터 수집 (판매자 별 정산금 누적 계산)
                        BigDecimal itemAmount = productRes.price().multiply(BigDecimal.valueOf(quantity));
                        sellerSettlementMap.merge(productRes.sellerIdx(), itemAmount, BigDecimal::add);
                    }
                }

                break;

            default:
                // 주문 상태가 DIRECT도 CART도 아닌 경우
                throw new RuntimeException("올바르지 않는 주문타입");
        }

        // Redis 재고 선차감 (Atomic)
        deductStockService.decreaseStocks(stocksInfos);

        // 모든 상품의 주문 금액 계산
        BigDecimal totalAmount = tempOrderItems.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 주문 엔티티 생성 및 추가
        OrderEntity orderEntity = OrderEntity.builder()
                .userIdx(userIdx)
                .orderCode(orderCode)
                .recipient(request.addressInfo().recipient())
                .address(request.addressInfo().address())
                .addressDetail(request.addressInfo().addressDetail())
                .phone(request.addressInfo().phone())
                .itemsAmount(totalAmount)
                .totalAmount(totalAmount)
                .build();

        try {
            // 엔티티 저장
            orderRepository.save(orderEntity);

            // 주문 상품 엔티티에 부모(주문 엔티티) 지정
            for (OrderItemEntity item : tempOrderItems) {
                item.setOrder(orderEntity);
            }
            orderItemRepository.saveAll(tempOrderItems);  // 엔티티 저장
        } catch (Exception e) {
            // 저장 실패했을 경우
            log.error("주문 DB 저장 실패. 재고 롤백 수행. orderCode={}", orderCode);

            // 까인 재고 롤백
            deductStockService.rollBackStocks(stocksInfos);
            throw e; // 예외 다시 던져서 트랜잭션 롤백
        }


        // =================================================================
        // 결제 요청 -> 5. 재고 차감/정산 -> (실패시) 환불
        // =================================================================
        ClientPaymentRes paymentRes;
        try {
            // 결제 진행
            paymentRes = paymentClient.processPayment(
                    new ClientPaymentReq(
                            userIdx,
                            orderCode,
                            orderEntity.getId(),
                            request.paymentType(),
                            totalAmount,
                            request.tossKey()
                    )
            );
            orderEntity.setStatus(OrderStatus.PAID);  // 주문 상태 PAID로 변경

        } catch (Exception e) {
            // 결제 실패 시
            log.error("결제 실패. 재고 롤백 수행. orderCode={}", orderCode);

            // 까인 재고 롤백
            deductStockService.rollBackStocks(stocksInfos);

            // 트랜잭션이 알아서 RollBack 해줌 (실제로는 반영되지 않는 코드)
            orderEntity.setStatus(OrderStatus.REFUNDED);

            throw new RuntimeException("결제 실패", e);
        }

        try {
            // 정산 데이터 저장
            settlementService.createSettlement(paymentRes.paymentIdx(), sellerSettlementMap);

            // 장바구니 비우기
            if (request.orderType() == OrderType.CART) {
                cartService.deleteCartAll(userIdx);
            }

            // Kafka로 Product Service에게 DB 갱신하라는 이벤트 발행
            for (ProductInfo info : stocksInfos) {
                // 메시지 전송
                StockUpdateMsg msg = new StockUpdateMsg(
                        info.productCode(),
                        info.optionCode(),
                        (long) info.quantity()
                );

                // 이벤트를 발행 (아직 Kafka로 안 날아감 - 메모리에만 존재)
                // -> DB 트랜잭션이 커밋되어야 Listener 처리함
                eventPublisher.publishEvent(new StockUpdateEvent(msg));
            }

        } catch (Exception e) {
            // 주문은 완료 되었는데 문재가 생겨서 롤백 해야되는 상황
            log.error("주문 후처리 실패. 결제 취소 및 재고 롤백 수행. orderCode={}", orderCode, e);
            deductStockService.rollBackStocks(stocksInfos);

            try {
                // 결제된 상품 환불 처리
                paymentClient.refundPayment(new ClientRefundReq(userIdx, orderCode));
            } catch (Exception refundEx) {
                log.error("CRITICAL: 결제 취소(환불) 요청 실패! 관리자 개입 필요. orderCode={}", orderCode, refundEx);
            }

            // 주문 상태 REFUNDED로 변경
            orderEntity.setStatus(OrderStatus.REFUNDED);
            throw new RuntimeException("주문 마무리 중 오류 발생 (환불 완료)", e);
        }

        // 주문이 완료된 상품정보 return
        List<OrderItemRes> responseItems = tempOrderItems.stream()
                .map(item -> new OrderItemRes(
                        new ItemInfo(
                                item.getProductCode(),
                                item.getOptionCode(),
                                item.getProductName(),
                                item.getOptionName(),
                                item.getPrice()),
                        item.getQuantity(),
                        item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()))
                ))
                .collect(Collectors.toList());

        return new OrderRes(orderCode, responseItems, totalAmount);
    }

    // Helper Method : createOrderItemEntity 생성
    private OrderItemEntity createOrderItemEntity(ClientProductRes product, Integer quantity) {
        return OrderItemEntity.builder()
                .productCode(product.productCode())
                .optionCode(product.optionCode())
                .productName(product.productName())
                .optionName(product.optionContent())
                .price(product.price())
                .quantity(quantity)
                .del(false)
                .build();
    }

    /*
     * 주문 정보 조회
     * @param userIdx: 유저 인덱스
     * @param pageable: 페이징 정보
     */
    @Transactional(readOnly = true)
    @Override
    public Page<OrderRes> findOrderList(Long userIdx, Pageable pageable) {

        List<OrderRes> resultList = new ArrayList<>();  // 주문 정보 리스트 (응답 객체)

        // userIdx의 OrderEntity 가져오기
        Page<OrderEntity> orderPage = orderRepository.findAllByUserIdx(userIdx, pageable);
        // 현재 페이지에 포함된 OrderEntity 리스트를 반환
        List<OrderEntity> entities = orderPage.getContent();

        // 가져온 주문 엔티티 순회
        for (OrderEntity orderEntity : entities) {

            List<OrderItemRes> responseItemList = new ArrayList<>();  // 주문 상품 정보 (응답 객체)
            BigDecimal itemsAmount = BigDecimal.ZERO;  // 주문 가격

            // 주문된 상품 엔티티 순회
            for (OrderItemEntity itemEntity : orderEntity.getOrderItems()) {
                // 주문한 개별 상품 가격
                BigDecimal amount = itemEntity.getPrice().multiply(BigDecimal.valueOf(itemEntity.getQuantity()));
                itemsAmount = itemsAmount.add(amount);

                // 주문한 상품 리스트 세팅
                responseItemList.add(
                        new OrderItemRes(
                                new ItemInfo(
                                        itemEntity.getProductCode(),
                                        itemEntity.getOptionCode(),
                                        itemEntity.getProductName(),
                                        itemEntity.getOptionName(),
                                        itemEntity.getPrice()
                                ),
                                itemEntity.getQuantity(),
                                amount
                        )
                );
            }

            // 최종 응답 객체 세팅
            OrderRes orderRes = new OrderRes(
                    orderEntity.getOrderCode(),
                    responseItemList,
                    itemsAmount
            );
            resultList.add(orderRes);
        }

        // 페이징으로 return
        return new PageImpl<>(resultList, pageable, orderPage.getTotalElements());
    }

    /*  [이전 N+1 발생 코드]
    List<OrderEntity> orderEntities = orderRepository.findAllByUserIdx(userIdx);
    for(OrderEntity orderEntity : orderEntities) {

        List<OrderItemResponse> responseItemList = new ArrayList<>();

        BigDecimal itemsAmount = BigDecimal.ZERO;
        List<OrderItemEntity> itemEntities = orderItemRepository.findAllByOrder(orderEntity);
        for(OrderItemEntity itemEntity : itemEntities) {

            BigDecimal amount = itemEntity.getPrice().multiply(BigDecimal.valueOf(itemEntity.getQuantity()));
            itemsAmount = itemsAmount.add(amount);
            responseItemList.add(
                    new OrderItemResponse(
                            new ItemInfo(
                                    itemEntity.getProductIdx(),
                                    itemEntity.getOptionIdx(),
                                    itemEntity.getProductName(),
                                    itemEntity.getOptionName(),
                                    itemEntity.getPrice()
                            ),
                            itemEntity.getQuantity(),
                            amount
                    )
            );
        }

        responseOrderList.add(
                new OrderResponse(
                        orderEntity.getId(),
                        responseItemList,
                        orderEntity.getOrderCode(),
                        itemsAmount
                )
        );
    }
    */


    /*
     * 단일 주문 조회
     * @param userIdx: 유저 인덱스
     * @param orderCode: 주문 코드
     */
    @Transactional(readOnly = true)
    @Override
    public OrderRes findOrder(Long userIdx, String orderCode) {

        List<OrderItemRes> responseItemList = new ArrayList<>();  // 주문한 상품 리스트 (응답 객체)
        BigDecimal itemsAmount = BigDecimal.ZERO;  // 주문한 상품 가격

        // UserIdx/OrderCode가 같은 주문 정보 찾기 (없으면 OrderNotFoundException)
        OrderEntity orderEntity = orderRepository.findByUserIdxAndOrderCode(userIdx, orderCode)
                .orElseThrow(() -> new OrderNotFoundException(ErrorCode.ORDER_NOT_FOUND));

        // orderEntity로 주문한 상품 엔티티 찾기
        List<OrderItemEntity> itemEntities = orderItemRepository.findAllByOrder(orderEntity);

        // 주문한 상품 순회
        for(OrderItemEntity entity : itemEntities) {

            BigDecimal amount = entity.getPrice().multiply(BigDecimal.valueOf(entity.getQuantity()));  // 주문한 가격
            itemsAmount = itemsAmount.add(amount);  // 가격 계산

            // 주문한 상품 리스트 세팅
            responseItemList.add(
                    new OrderItemRes(
                            new ItemInfo(
                                    entity.getProductCode(),
                                    entity.getOptionCode(),
                                    entity.getProductName(),
                                    entity.getOptionName(),
                                    entity.getPrice()
                            ),
                            entity.getQuantity(),
                            amount
                    )
            );
        }

        // 상세 주문 조회 데이터 return
        return new OrderRes(
                orderEntity.getOrderCode(),
                responseItemList,
                itemsAmount
        );
    }

    /*
     * 주문 상태 변경
     * @param orderCode: 주문 코드
     * @param status: 주문 상태
     */
    @Transactional
    @Override
    public void updateOrderStatus(String orderCode, String status) {

        // orderCode로 주문 엔티티 조회
        OrderEntity order = orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new OrderNotFoundException(ErrorCode.ORDER_NOT_FOUND));

        order.setStatus(OrderStatus.valueOf(status));  // 상태 변경
        orderRepository.save(order);  // 엔티티 저장
    }

    /*
     * OrderCode로 OrderIdx 찾기
     * @param orderCode: 주문 코드
     */
    @Transactional(readOnly = true)
    @Override
    public Long findOrderIdx(String orderCode) {

        // orderCode로 주문 엔티티 조회
        OrderEntity order = orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new OrderNotFoundException(ErrorCode.ORDER_NOT_FOUND));

        // orderIdx return
        return order.getUserIdx();
    }
}
