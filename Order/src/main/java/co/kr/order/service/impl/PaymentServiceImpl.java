package co.kr.order.service.impl;


import co.kr.order.client.UserClient;
import co.kr.order.exception.ErrorCode;
import co.kr.order.exception.PaymentFailedException;
import co.kr.order.model.dto.response.PaymentResponse;
import co.kr.order.model.dto.request.PaymentRequest;
import co.kr.order.model.entity.OrderEntity;
import co.kr.order.model.entity.PaymentEntity;
import co.kr.order.model.vo.PaymentStatus;
import co.kr.order.model.vo.PaymentType;
import co.kr.order.repository.OrderJpaRepository;
import co.kr.order.repository.PaymentJpaRepository;
import co.kr.order.service.OrderService;
import co.kr.order.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentJpaRepository paymentRepository;
    private final OrderJpaRepository orderRepository;
    private final OrderService orderService;
    private final UserClient userClient;

    @Override
    @Transactional
    public PaymentResponse process(String token, PaymentRequest request) {
        if (request == null || request.paymentType() == null) {
            throw new PaymentFailedException(ErrorCode.PAYMENT_FAILED);
        }

        return switch (request.paymentType()) {
            case CARD -> processCardPayment(token, request.orderCode());
            default -> throw new PaymentFailedException(ErrorCode.PAYMENT_FAILED);
        };
    }

    private PaymentResponse processCardPayment(String token, String orderCode) {
        Long userIdx = userClient.getUserIdx(token);

        OrderEntity order = orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new PaymentFailedException(ErrorCode.PAYMENT_FAILED));

        // 실제사용자 확인
        if (!userIdx.equals(order.getUserIdx())) {
            throw new PaymentFailedException(ErrorCode.PAYMENT_FAILED);
        }
        // 카드 Null 인지 확인
        Long cardIdx = order.getCardIdx();
        if (cardIdx == null) {
            throw new PaymentFailedException(ErrorCode.PAYMENT_FAILED);
        }

        PaymentEntity payment = PaymentEntity.builder()
                .usersIdx(order.getUserIdx())
                .status(PaymentStatus.PAYMENT)
                .type(PaymentType.CARD)
                .amount(order.getTotalAmount())
                .ordersIdx(order.getId())
                .cardIdx(cardIdx)
                .build();

        PaymentEntity saved = paymentRepository.save(payment);

        return new PaymentResponse(
                saved.getPaymentIdx(),
                saved.getStatus(),
                saved.getType(),
                saved.getAmount(),
                saved.getOrdersIdx(),
                saved.getCardIdx()
        );
    }

}
