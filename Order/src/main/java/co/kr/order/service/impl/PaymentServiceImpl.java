package co.kr.order.service.impl;


import co.kr.order.client.UserClient;
import co.kr.order.exception.ErrorCode;
import co.kr.order.exception.PaymentFailedException;
import co.kr.order.model.dto.request.PaymentRequest;
import co.kr.order.model.dto.request.PaymentTossConfirmRequest;
import co.kr.order.model.dto.response.PaymentResponse;
import co.kr.order.model.entity.OrderEntity;
import co.kr.order.model.entity.PaymentEntity;
import co.kr.order.model.vo.OrderStatus;
import co.kr.order.model.vo.PaymentStatus;
import co.kr.order.model.vo.PaymentType;
import co.kr.order.mapper.PaymentMapper;
import co.kr.order.repository.OrderJpaRepository;
import co.kr.order.repository.PaymentJpaRepository;
import co.kr.order.service.PaymentService;
import co.kr.order.service.SettlementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentJpaRepository paymentRepository;
    private final OrderJpaRepository orderRepository;
    private final UserClient userClient;
    private final SettlementService settlementService;
    private final ObjectMapper om;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Value("${custom.payments.toss.secrets}")
    private String tossPaymentSecrets;

    @Value("${custom.payments.toss.confirm-url}")
    private String tossPaymentConfirmUrl;

    @Value("${custom.payments.toss.cancel-url}")
    private String tossPaymentCancelUrl;

    /**
     * 결제 처리 (process와 동일)
     */
    @Override
    @Transactional
    public PaymentResponse process(Long userIdx, PaymentRequest request) {
        return pay(userIdx, request);
    }

    /**
     * 결제 수단별 결제 처리
     * - CARD: 카드 결제 (연동없이 기록만 저장)
     * - DEPOSIT: 예치금 결제 (User 서비스 연동)
     * - TOSS_PAY: 토스페이먼츠는 /payment/toss/confirm 엔드포인트 사용
     */
    @Override
    @Transactional
    public PaymentResponse pay(Long userIdx, PaymentRequest request) {
        return switch (request.paymentType()) {
            case CARD -> handleCardPayment(userIdx, request.orderCode());
            case DEPOSIT -> handleDepositPayment(userIdx, request.orderCode());
            case TOSS_PAY -> throw new IllegalArgumentException(
                    "TOSS_PAY는 별도로 요청합니다."
            );
        };
    }

    /**
     * 결제 환불 처리
     * - 결제 수단(DEPOSIT, TOSS_PAY, CARD)에 따라 환불 처리 수행
     * - 환불 내역을 별도의 Payment 엔티티로 저장 (REFUND 상태)
     * - Order 상태를 REFUNDED로 변경
     */
    @Override
    @Transactional
    public PaymentResponse refund(Long userIdx, String orderCode) {
        OrderEntity order = orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));

        if (!order.getUserIdx().equals(userIdx)) {
            throw new IllegalArgumentException("주문 소유자가 아닙니다.");
        }

        PaymentEntity payment = paymentRepository.findByOrdersIdx(order.getId())
                .orElseThrow(() -> new IllegalArgumentException("결제 내역을 찾을 수 없습니다."));

        if (payment.getStatus() != PaymentStatus.PAYMENT) {
            throw new IllegalStateException("환불할 수 없는 상태입니다.");
        }

        BigDecimal refundAmount = payment.getAmount();

        switch (payment.getType()) {
            case DEPOSIT -> {
                try {
                    userClient.refundBalance(userIdx, refundAmount);
                    log.info("Balance 환불 성공: userIdx={}, amount={}", userIdx, refundAmount);
                } catch (Exception e) {
                    log.error("Balance 환불 실패: userIdx={}, amount={}", userIdx, refundAmount, e);
                    throw new PaymentFailedException(ErrorCode.PAYMENT_FAILED);
                }
            }
            case TOSS_PAY -> {
                if (payment.getPaymentKey() == null) {
                    throw new IllegalStateException("토스페이 PaymentKey가 없습니다.");
                }
                try {
                    sendTossCancel(payment.getPaymentKey());
                    log.info("토스페이 환불 성공: paymentKey={}, amount={}", payment.getPaymentKey(), refundAmount);
                } catch (Exception e) {
                    log.error("토스페이 환불 실패: paymentKey={}, amount={}", payment.getPaymentKey(), refundAmount, e);
                    throw new PaymentFailedException(ErrorCode.PAYMENT_FAILED);
                }
            }
            case CARD -> {
                log.warn("카드 환불 요청 - 관리자 처리 필요: userIdx={}, orderId={}, amount={}",
                        userIdx, order.getId(), refundAmount);
            }
        }

        PaymentEntity refundPayment = PaymentEntity.builder()
                .usersIdx(payment.getUsersIdx())
                .status(PaymentStatus.REFUND)
                .type(payment.getType())
                .amount(refundAmount)
                .ordersIdx(payment.getOrdersIdx())
                .cardIdx(payment.getCardIdx())
                .paymentKey(payment.getPaymentKey())
                .build();
        paymentRepository.save(refundPayment);

        updateOrderStatus(order, OrderStatus.REFUNDED);

        // 환불 정산 생성 (정산에서 차감 처리)
        settlementService.createRefundSettlement(order.getId(), refundPayment.getPaymentIdx());

        return PaymentMapper.toResponse(refundPayment);
    }

    /**
     * 토스페이먼츠 결제 승인 처리
     * - 클라이언트에서 받은 paymentKey와 amount로 토스 API에 승인 요청
     */
    @Override
    @Transactional
    public PaymentResponse confirmTossPayment(Long userIdx, PaymentTossConfirmRequest request) {
        return handleTossPayPayment(
                userIdx,
                request.orderCode(),
                request.paymentKey(),
                request.amount()
        );
    }

    /**
     * 카드 결제 처리
     * - PG 연동 없이 결제 내역만 저장
     */
    private PaymentResponse handleCardPayment(Long userIdx, String orderCode) {
        OrderEntity order = orderRepository.findByOrderCode(orderCode).orElse(null);
        Long cardIdx = order != null ? order.getCardIdx() : null;

        PaymentEntity payment = PaymentEntity.builder()
                .usersIdx(userIdx)
                .status(PaymentStatus.PAYMENT)
                .type(PaymentType.CARD)
                .amount(order != null ? order.getTotalAmount() : null)
                .ordersIdx(order != null ? order.getId() : null)
                .cardIdx(cardIdx)
                .build();

        PaymentEntity saved = paymentRepository.save(payment);
        if (order != null) {
            updateOrderStatus(order, OrderStatus.PAID);
        }

        return PaymentMapper.toResponse(saved);
    }

    /**
     * 예치금(Balance) 결제 처리
     * - User 서비스에 Balance 차감 요청
     * - 차감 성공 시 결제 내역 저장 및 주문 상태 PAID로 변경
     */
    private PaymentResponse handleDepositPayment(Long userIdx, String orderCode) {
        OrderEntity order = orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));

        if (!order.getUserIdx().equals(userIdx)) {
            throw new IllegalArgumentException("주문 소유자가 아닙니다.");
        }

        BigDecimal paymentAmount = order.getTotalAmount();

        try {
            userClient.useBalance(userIdx, paymentAmount);
        } catch (Exception e) {
            log.error("Balance(예치금) 결제 실패: userIdx={}, amount={}", userIdx, paymentAmount, e);
            throw new PaymentFailedException(ErrorCode.PAYMENT_FAILED);
        }

        PaymentEntity payment = PaymentEntity.builder()
                .usersIdx(userIdx)
                .status(PaymentStatus.PAYMENT)
                .type(PaymentType.DEPOSIT)
                .amount(paymentAmount)
                .ordersIdx(order.getId())
                .build();

        PaymentEntity saved = paymentRepository.save(payment);
        updateOrderStatus(order, OrderStatus.PAID);

        return PaymentMapper.toResponse(saved);
    }

    /**
     * 토스페이먼츠 결제 처리
     * - 토스 API에 결제 승인 요청 후 결제 내역 저장
     */
    private PaymentResponse handleTossPayPayment(
            Long userIdx,
            String orderCode,
            String paymentKey,
            BigDecimal amount
    ) {
        OrderEntity order = orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));

        if (!order.getUserIdx().equals(userIdx)) {
            throw new IllegalArgumentException("주문 소유자가 아닙니다.");
        }

        BigDecimal resolvedAmount = amount != null ? amount : order.getTotalAmount();
        sendTossConfirm(order, paymentKey, resolvedAmount);

        PaymentEntity payment = PaymentEntity.builder()
                .usersIdx(userIdx)
                .status(PaymentStatus.PAYMENT)
                .type(PaymentType.TOSS_PAY)
                .amount(resolvedAmount)
                .ordersIdx(order.getId())
                .paymentKey(paymentKey)
                .build();

        PaymentEntity saved = paymentRepository.save(payment);
        updateOrderStatus(order, OrderStatus.PAID);

        return PaymentMapper.toResponse(saved);
    }

    /**
     * 토스페이먼츠 결제 승인 API 호출
     */
    private void sendTossConfirm(OrderEntity order, String paymentKey, BigDecimal amount) {
        try {
            String authorization = "Basic " + Base64.getEncoder()
                    .encodeToString((tossPaymentSecrets + ":").getBytes(StandardCharsets.UTF_8));
            Map<String, Object> payload = Map.of(
                    "paymentKey", paymentKey,
                    "orderId", order.getOrderCode(),
                    "amount", amount
            );

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(tossPaymentConfirmUrl))
                    .header("Authorization", authorization)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofByteArray(om.writeValueAsBytes(payload)))
                    .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error("토스페이 승인 실패 - Status: {}, Body: {}", response.statusCode(), response.body());
                throw new PaymentFailedException(ErrorCode.PAYMENT_FAILED);
            }

            log.info("토스페이 승인 API 응답: {}", response.body());
        } catch (PaymentFailedException e) {
            throw e;
        } catch (Exception e) {
            log.error("토스 결제 승인 요청 실패: orderCode={}, paymentKey={}", order.getOrderCode(), paymentKey, e);
            throw new PaymentFailedException(ErrorCode.PAYMENT_FAILED);
        }
    }

    /**
     * 토스페이먼츠 결제 취소 API 호출
     * 전체 환불 처리이기 때문에 부분 환불을 위한 amount 파라미터는 없음
     */
    private void sendTossCancel(String paymentKey) {
        try {
            String authorization = "Basic " + Base64.getEncoder()
                    .encodeToString((tossPaymentSecrets + ":").getBytes(StandardCharsets.UTF_8));

            Map<String, Object> payload = Map.of(
                    "cancelReason", "고객 요청"
            );
            String cancelUrl = tossPaymentCancelUrl.replace("{paymentKey}", paymentKey);

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(cancelUrl))
                    .header("Authorization", authorization)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofByteArray(om.writeValueAsBytes(payload)))
                    .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error("토스페이 취소 실패 - Status: {}, Body: {}", response.statusCode(), response.body());
                throw new RuntimeException("토스페이 취소 실패: " + response.body());
            }

            log.info("토스페이 취소 API 응답: {}", response.body());
        } catch (Exception e) {
            log.error("토스 취소 요청 실패: paymentKey={}", paymentKey, e);
            throw new RuntimeException("토스페이 환불 요청 실패", e);
        }
    }

    private void updateOrderStatus(OrderEntity order, OrderStatus status) {
        order.setStatus(status);
        orderRepository.save(order);
    }

}
