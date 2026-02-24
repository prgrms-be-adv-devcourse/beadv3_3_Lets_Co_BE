package co.kr.order.service.impl;

import co.kr.order.client.ProductClient;
import co.kr.order.exception.ErrorCode;
import co.kr.order.exception.OrderNotFoundException;
import co.kr.order.model.dto.ItemInfo;
import co.kr.order.model.dto.ProductInfo;
import co.kr.order.model.dto.UserInfo;
import co.kr.order.model.dto.event.StockUpdateEvent;
import co.kr.order.model.dto.event.StockUpdateMsg;
import co.kr.order.model.dto.request.ClientProductReq;
import co.kr.order.model.dto.request.OrderReq;
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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderJpaRepository orderRepository;
    private final OrderItemJpaRepository orderItemRepository;
    private final SettlementService settlementService;

    private final CartService cartService;
    private final DeductStockService deductStockService;
    private final ApplicationEventPublisher eventPublisher;

    private final ProductClient productClient;

    // 정산금 처리를 위한 Redis
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    // 외부 통신과 트랜잭션을 분리하기 위해 TransactionTemplate 주입
    private final TransactionTemplate transactionTemplate;

    /*
     * 주문 생성
     */
    @Transactional
    @Override
    public OrderRes createOrder(Long userIdx, OrderReq request) {

        String orderCode = UUID.randomUUID().toString();
        List<OrderItemEntity> tempOrderItems = new ArrayList<>();
        List<ProductInfo> stocksInfos = new ArrayList<>();
        Map<Long, BigDecimal> sellerSettlementMap = new HashMap<>();

        switch (request.orderType()) {
            case OrderType.DIRECT:
                ClientProductRes product;
                try {
                    product = productClient.getProduct(
                            request.productInfo().productCode(),
                            request.productInfo().optionCode()
                    );
                } catch (Exception e) {
                    log.error("상품조회 실패", e);
                    throw e;
                }

                tempOrderItems.add(createOrderItemEntity(product, request.productInfo().quantity()));
                BigDecimal directAmount = product.price().multiply(BigDecimal.valueOf(request.productInfo().quantity()));
                sellerSettlementMap.merge(product.sellerIdx(), directAmount, BigDecimal::add);

                stocksInfos.add(new ProductInfo(
                        request.productInfo().productCode(),
                        request.productInfo().optionCode(),
                        request.productInfo().quantity()
                ));
                break;

            case OrderType.CART:
                Map<Long, Integer> quantityMap = cartService.getCartItemQuantities(userIdx);
                List<ClientProductReq> productRequest = cartService.getProductByCart(userIdx);

                List<ClientProductRes> productsResponse;
                try {
                    productsResponse = productClient.getProductList(productRequest);
                } catch (Exception e) {
                    log.error("상품조회 실패", e);
                    throw e;
                }

                for (ClientProductRes productRes : productsResponse) {
                    Integer quantity = quantityMap.getOrDefault(productRes.optionIdx(), 0);
                    if (quantity > 0) {
                        tempOrderItems.add(createOrderItemEntity(productRes, quantity));

                        BigDecimal itemAmount = productRes.price().multiply(BigDecimal.valueOf(quantity));
                        sellerSettlementMap.merge(productRes.sellerIdx(), itemAmount, BigDecimal::add);

                        stocksInfos.add(new ProductInfo(
                                productRes.productCode(),
                                productRes.optionCode(),
                                quantity
                        ));
                    }
                }
                break;

            default:
                throw new RuntimeException("올바르지 않는 주문타입");
        }

        deductStockService.decreaseStocks(stocksInfos);

        BigDecimal totalAmount = tempOrderItems.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        OrderEntity orderEntity = OrderEntity.builder()
                .userIdx(userIdx)
                .orderCode(orderCode)
                .itemsAmount(totalAmount)
                .totalAmount(totalAmount)
                .build();

        try {
            orderRepository.save(orderEntity);
            for (OrderItemEntity item : tempOrderItems) {
                item.setOrder(orderEntity);
            }
            orderItemRepository.saveAll(tempOrderItems);

            log.info("========== [정산 로그] 주문 생성 (Redis 저장 전) ==========");
            log.info("OrderCode: {}", orderCode);
            sellerSettlementMap.forEach((sellerIdx, amount) ->
                    log.info(" - 판매자(SellerIdx): {}, 정산금: {}", sellerIdx, amount)
            );
            log.info("==========================================================");

            saveSettlementInfoToRedis(orderCode, sellerSettlementMap);
        } catch (Exception e) {
            log.error("주문 저장 실패. 재고 롤백. orderCode={}", orderCode);
            deductStockService.rollBackStocks(stocksInfos);
            throw e;
        }

        if(request.orderType().equals(OrderType.CART)) {
            cartService.deleteCartAll(orderEntity.getUserIdx());
        }

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

        return new OrderRes(orderCode, responseItems, totalAmount, orderEntity.getCreatedAt());
    }

    private OrderItemEntity createOrderItemEntity(ClientProductRes product, Integer quantity) {
        return OrderItemEntity.builder()
                .productCode(product.productCode())
                .optionCode(product.optionCode())
                .productName(product.productName())
                .optionName(product.optionContent())
                .price(product.price())
                .quantity(quantity)
                .build();
    }

    /*
     * 결제 성공 처리 (트랜잭션과 외부 통신 분리)
     */
    @Override
    public void orderSuccess(String orderCode, Long paymentIdx, UserInfo userInfo) {
        // 1. DB 락(Lock) 없이 데이터 단순 조회
        OrderEntity tempOrder = orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new OrderNotFoundException(ErrorCode.ORDER_NOT_FOUND));

        if (tempOrder.getStatus() == OrderStatus.PAID) {
            return;
        }

        List<OrderItemEntity> itemEntities = orderItemRepository.findAllByOrder(tempOrder);
        Map<Long, BigDecimal> settlementMap = getOrCalculateSettlementMap(orderCode, itemEntities);

        //  DB 업데이트 수행
        transactionTemplate.executeWithoutResult(status -> {

            // 비관적 락(Pessimistic Lock) 획득
            OrderEntity lockedOrder = orderRepository.findByOrderCodeWithLock(orderCode)
                    .orElseThrow(() -> new OrderNotFoundException(ErrorCode.ORDER_NOT_FOUND));

            // 그 사이 다른 스레드가 처리했는지 멱등성 재검증
            if (lockedOrder.getStatus() == OrderStatus.PAID) {
                return;
            }

            // DB 상태 업데이트 (JPA Dirty Checking으로 자동 UPDATE)
            lockedOrder.setUserData(userInfo);
            lockedOrder.setStatus(OrderStatus.PAID);

            // 미리 만들어둔 Map을 이용해 정산 데이터 DB 저장 (외부 통신 대기 없음)
            if (!settlementMap.isEmpty()) {
                settlementService.createSettlement(paymentIdx, settlementMap);
                redisTemplate.delete("settlement:" + orderCode);
            }

            // Kafka 이벤트 발행
            for (OrderItemEntity item : itemEntities) {
                StockUpdateMsg msg = new StockUpdateMsg(
                        UUID.randomUUID().toString(),
                        item.getProductCode(),
                        item.getOptionCode(),
                        (long) item.getQuantity()
                );
                eventPublisher.publishEvent(new StockUpdateEvent(msg));
            }
        });
    }

    /*
     * 결제 실패 시 호출 (재고 롤백)
     */
    @Transactional
    @Override
    public void orderFail(String orderCode) {

        OrderEntity orderEntity = orderRepository.findByOrderCodeWithLock(orderCode)
                .orElseThrow(() -> new OrderNotFoundException(ErrorCode.ORDER_NOT_FOUND));

        orderEntity.setStatus(OrderStatus.PAYMENT_FAILED);

        List<OrderItemEntity> itemEntities = orderItemRepository.findAllByOrder(orderEntity);

        List<ProductInfo> stocksInfos = itemEntities.stream()
                .map(item -> new ProductInfo(
                        item.getProductCode(),
                        item.getOptionCode(),
                        item.getQuantity()
                ))
                .collect(Collectors.toList());

        try {
            deductStockService.rollBackStocks(stocksInfos);
            log.info("결제 실패로 인한 재고 롤백 완료. orderCode={}", orderCode);
        } catch (Exception e) {
            log.error("재고 롤백 중 에러 발생! 수동 확인 필요. orderCode={}", orderCode, e);
        }
    }


    // ==========================================
    // Helper Methods Settlement (Redis)
    // ==========================================
    private void saveSettlementInfoToRedis(String orderCode, Map<Long, BigDecimal> map) {
        try {
            String key = "settlement:" + orderCode;
            String jsonValue = objectMapper.writeValueAsString(map);
            redisTemplate.opsForValue().set(key, jsonValue, 30, TimeUnit.MINUTES);
            log.info("Redis 정산 정보 저장 완료: {}", orderCode);
        } catch (JsonProcessingException e) {
            log.error("정산 정보 Redis 직렬화 실패: {}", orderCode, e);
        }
    }

    // 정산 데이터를 가져오거나(Redis) 재계산(Feign API)하여 Map 형태로만 반환하는 메서드로 변경
    private Map<Long, BigDecimal> getOrCalculateSettlementMap(String orderCode, List<OrderItemEntity> items) {
        Map<Long, BigDecimal> settlementMap = null;
        String key = "settlement:" + orderCode;

        try {
            Object redisValue = redisTemplate.opsForValue().get(key);
            if (redisValue != null) {
                String jsonStr = String.valueOf(redisValue);
                settlementMap = objectMapper.readValue(jsonStr, new TypeReference<Map<Long, BigDecimal>>() {});
            }
        } catch (Exception e) {
            log.warn("Redis 정산 정보 조회/파싱 실패 (재계산 수행 예정): {}", orderCode, e);
        }

        if (settlementMap == null || settlementMap.isEmpty()) {
            log.info("정산 정보 재계산 수행: {}", orderCode);
            settlementMap = recalculateSettlementMap(items);
        }

        return settlementMap != null ? settlementMap : new HashMap<>();
    }

    private Map<Long, BigDecimal> recalculateSettlementMap(List<OrderItemEntity> items) {
        Map<Long, BigDecimal> map = new HashMap<>();

        List<ClientProductReq> reqList = items.stream()
                .map(item -> new ClientProductReq(item.getProductCode(), item.getOptionCode()))
                .collect(Collectors.toList());

        List<ClientProductRes> productInfos = productClient.getProductList(reqList);

        for (ClientProductRes p : productInfos) {
            int quantity = items.stream()
                    .filter(i -> i.getOptionCode().equals(p.optionCode()))
                    .mapToInt(OrderItemEntity::getQuantity)
                    .sum();

            if (quantity > 0) {
                BigDecimal amount = p.price().multiply(BigDecimal.valueOf(quantity));
                map.merge(p.sellerIdx(), amount, BigDecimal::add);
            }
        }
        return map;
    }

    /*
     * 주문 정보 조회
     */
    @Transactional(readOnly = true)
    @Override
    public Page<OrderRes> findOrderList(Long userIdx, Pageable pageable) {

        List<OrderRes> resultList = new ArrayList<>();
        Page<OrderEntity> orderPage = orderRepository.findAllByUserIdx(userIdx, pageable);
        List<OrderEntity> entities = orderPage.getContent();

        for (OrderEntity orderEntity : entities) {
            List<OrderItemRes> responseItemList = new ArrayList<>();
            BigDecimal itemsAmount = BigDecimal.ZERO;

            for (OrderItemEntity itemEntity : orderEntity.getOrderItems()) {
                BigDecimal amount = itemEntity.getPrice().multiply(BigDecimal.valueOf(itemEntity.getQuantity()));
                itemsAmount = itemsAmount.add(amount);

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

            OrderRes orderRes = new OrderRes(
                    orderEntity.getOrderCode(),
                    responseItemList,
                    itemsAmount,
                    orderEntity.getCreatedAt()
            );
            resultList.add(orderRes);
        }
        return new PageImpl<>(resultList, pageable, orderPage.getTotalElements());
    }

    /*
     * 단일 주문 조회
     */
    @Transactional(readOnly = true)
    @Override
    public OrderRes findOrder(Long userIdx, String orderCode) {

        List<OrderItemRes> responseItemList = new ArrayList<>();
        BigDecimal itemsAmount = BigDecimal.ZERO;

        OrderEntity orderEntity = orderRepository.findByUserIdxAndOrderCode(userIdx, orderCode)
                .orElseThrow(() -> new OrderNotFoundException(ErrorCode.ORDER_NOT_FOUND));

        List<OrderItemEntity> itemEntities = orderItemRepository.findAllByOrder(orderEntity);

        for(OrderItemEntity entity : itemEntities) {
            BigDecimal amount = entity.getPrice().multiply(BigDecimal.valueOf(entity.getQuantity()));
            itemsAmount = itemsAmount.add(amount);

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

        return new OrderRes(
                orderEntity.getOrderCode(),
                responseItemList,
                itemsAmount,
                orderEntity.getCreatedAt()
        );
    }

    /*
     * 환불 시 호출
     */
    @Transactional
    @Override
    public void orderRefund(String orderCode, Long paymentIdx) {
        OrderEntity orderEntity = orderRepository.findByOrderCodeWithLock(orderCode)
                .orElseThrow(() -> new OrderNotFoundException(ErrorCode.ORDER_NOT_FOUND));

        orderEntity.setStatus(OrderStatus.REFUNDED);
        settlementService.refundSettlement(orderEntity.getId(), paymentIdx);
    }

    /*
     * 주문 상태 변경
     */
    @Transactional
    @Override
    public void updateOrderStatus(String orderCode, String status) {
        OrderEntity order = orderRepository.findByOrderCodeWithLock(orderCode)
                .orElseThrow(() -> new OrderNotFoundException(ErrorCode.ORDER_NOT_FOUND));
        order.setStatus(OrderStatus.valueOf(status));
    }

    /*
     * OrderCode로 OrderIdx 찾기
     */
    @Transactional(readOnly = true)
    @Override
    public Long findOrderIdx(String orderCode) {
        // 단순 조회를 하는 곳에서는 일반(락 없는) 조회를 사용해야 시스템 성능이 저하되지 않습니다.
        OrderEntity order = orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new OrderNotFoundException(ErrorCode.ORDER_NOT_FOUND));
        return order.getId();
    }
}