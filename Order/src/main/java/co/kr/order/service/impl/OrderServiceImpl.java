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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderJpaRepository orderRepository;
    private final OrderItemJpaRepository orderItemRepository;

    private final CartService cartService;
    private final DeductStockService deductStockService;
    private final ApplicationEventPublisher eventPublisher;

    private final ProductClient productClient;

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

        switch (request.orderType()) {
            case OrderType.DIRECT:
                // 상품 정보 조회
                ClientProductRes product = productClient.getProduct(
                        request.productInfo().productCode(),
                        request.productInfo().optionCode()
                );

                // 엔티티 생성
                tempOrderItems.add(createOrderItemEntity(product, request.productInfo().quantity()));

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
        } catch (Exception e) {
            log.error("주문 저장 실패. 재고 롤백. orderCode={}", orderCode);
            deductStockService.rollBackStocks(stocksInfos);
            throw e;
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

    // Helper Method
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
     * - 정산 로직 및 상품 재조회 로직 제거됨
     */
    @Transactional
    @Override
    public void orderSuccess(String orderCode, UserInfo userInfo) {

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

        // TODO 상태 받고 CART일때만 삭제
        cartService.deleteCartAll(orderEntity.getUserIdx());

        // 5. Kafka 이벤트 발행 (Product Service DB 재고 차감용)
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
