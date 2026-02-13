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

    /*
     * 주문 생성
     * @param userIdx: 유저 인덱스
     * @param request: 주문 요청 정보
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
                // 상품 정보 조회
                ClientProductRes product = productClient.getProduct(
                        request.productInfo().productCode(),
                        request.productInfo().optionCode()
                );

                // 엔티티 생성
                tempOrderItems.add(createOrderItemEntity(product, request.productInfo().quantity()));

                BigDecimal directAmount = product.price().multiply(BigDecimal.valueOf(request.productInfo().quantity()));
                sellerSettlementMap.merge(product.sellerIdx(), directAmount, BigDecimal::add);

                // 재고 차감 정보
                stocksInfos.add(new ProductInfo(
                        request.productInfo().productCode(),
                        request.productInfo().optionCode(),
                        request.productInfo().quantity()
                ));
                break;

            case OrderType.CART:
                Map<Long, Integer> quantityMap = cartService.getCartItemQuantities(userIdx);
                List<ClientProductReq> productRequest = cartService.getProductByCart(userIdx);
                List<ClientProductRes> productsResponse = productClient.getProductList(productRequest);

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

        // Redis 재고 선차감
        deductStockService.decreaseStocks(stocksInfos);

        // 총 주문 금액 계산
        BigDecimal totalAmount = tempOrderItems.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 주문 엔티티 생성
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

            /* ======================
             * 정산 잘 적용되는지 로그
             =======================*/
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

        // 응답 생성
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

    // Helper Method 주문한 상품 엔티티
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
     * 결제 성공 시 호출 (장바구니 삭제, 재고 동기화 이벤트)
     */
    @Transactional
    @Override
    public void orderSuccess(String orderCode, Long paymentIdx, UserInfo userInfo) {

        // 주문 엔티티 조회
        OrderEntity orderEntity = orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new OrderNotFoundException(ErrorCode.ORDER_NOT_FOUND));

        if (orderEntity.getStatus() == OrderStatus.PAID) {
            return;
        }

        // 상태 변경
        orderEntity.setUserData(userInfo);
        orderEntity.setStatus(OrderStatus.PAID);

        // 주문 상품 리스트 조회 (재고 이벤트를 위해 필요)
        List<OrderItemEntity> itemEntities = orderItemRepository.findAllByOrder(orderEntity);

        processSettlement(orderCode, paymentIdx, itemEntities);

        // Kafka 이벤트 발행 (Product Service DB 재고 차감용)
        for (OrderItemEntity item : itemEntities) {
            StockUpdateMsg msg = new StockUpdateMsg(
                    UUID.randomUUID().toString(),
                    item.getProductCode(),
                    item.getOptionCode(),
                    (long) item.getQuantity()
            );
            eventPublisher.publishEvent(new StockUpdateEvent(msg));
        }
    }

    /*
     * 결제 실패 시 호출 (재고 롤백)
     */
    @Transactional
    @Override
    public void orderFail(String orderCode) {

        OrderEntity orderEntity = orderRepository.findByOrderCode(orderCode)
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

    private void processSettlement(String orderCode, Long paymentIdx, List<OrderItemEntity> items) {
        Map<Long, BigDecimal> settlementMap = null;
        String key = "settlement:" + orderCode;

        // Redis에서 조회 시도
        try {
            Object redisValue = redisTemplate.opsForValue().get(key);
            if (redisValue != null) {
                // Redis에 저장된 JSON 문자열을 Map으로 복원
                // RedisTemplate<String, Object>라서 String으로 캐스팅 필요할 수 있음
                String jsonStr = String.valueOf(redisValue);
                settlementMap = objectMapper.readValue(jsonStr, new TypeReference<Map<Long, BigDecimal>>() {});
            }
        } catch (Exception e) {
            log.warn("Redis 정산 정보 조회/파싱 실패 (재계산 수행 예정): {}", orderCode, e);
        }

        // Redis에 없으면 DB/Feign으로 재계산 (Fallback)
        if (settlementMap == null || settlementMap.isEmpty()) {
            log.info("정산 정보 재계산 수행: {}", orderCode);
            settlementMap = recalculateSettlementMap(items);
        }

        // 정산 서비스 호출
        if (!settlementMap.isEmpty()) {
            settlementService.createSettlement(paymentIdx, settlementMap);
            // 처리 후 Redis 키 삭제
            redisTemplate.delete(key);
        }
    }

    // Fallback: 상품 서비스를 통해 sellerIdx를 다시 조회하여 정산금 계산
    private Map<Long, BigDecimal> recalculateSettlementMap(List<OrderItemEntity> items) {
        Map<Long, BigDecimal> map = new HashMap<>();

        // 주문 상품들의 코드로 최신 상품 정보(SellerIdx 포함) 조회
        List<ClientProductReq> reqList = items.stream()
                .map(item -> new ClientProductReq(item.getProductCode(), item.getOptionCode()))
                .collect(Collectors.toList());

        // Product Service 호출 (Bulk)
        List<ClientProductRes> productInfos = productClient.getProductList(reqList);

        // 정산금 합산
        for (ClientProductRes p : productInfos) {
            // 해당 옵션 코드의 주문 수량 찾기
            int quantity = items.stream()
                    .filter(i -> i.getOptionCode().equals(p.optionCode()))
                    .mapToInt(OrderItemEntity::getQuantity)
                    .sum();

            if (quantity > 0) {
                // 가격 * 수량
                BigDecimal amount = p.price().multiply(BigDecimal.valueOf(quantity));
                map.merge(p.sellerIdx(), amount, BigDecimal::add);
            }
        }
        return map;
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
        return order.getId();
    }

}
