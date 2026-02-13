package co.kr.payment.service.impl;

import co.kr.payment.client.OrderClient;
import co.kr.payment.client.UserClient;
import co.kr.payment.exception.ErrorCode;
import co.kr.payment.exception.PaymentFailedException;
import co.kr.payment.mapper.PaymentMapper;
import co.kr.payment.model.dto.request.BalanceClientReq;
import co.kr.payment.model.dto.request.ChargeReq;
import co.kr.payment.model.dto.request.PaymentReq;
import co.kr.payment.model.dto.request.RefundReq;
import co.kr.payment.model.dto.response.PaymentResponse;
import co.kr.payment.model.entity.PaymentEntity;
import co.kr.payment.model.vo.PaymentStatus;
import co.kr.payment.model.vo.PaymentType;
import co.kr.payment.repository.PaymentJpaRepository;
import co.kr.payment.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
    private final OrderClient orderClient;
    private final UserClient userClient;
    private final ObjectMapper om;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Value("${custom.payments.toss.secrets}")
    private String tossPaymentSecrets;

    @Value("${custom.payments.toss.confirm-secrets}")
    private String tossPaymentConfirmSecrets;

    @Value("${custom.payments.toss.confirm-url}")
    private String tossPaymentConfirmUrl;

    @Value("${custom.payments.toss.cancel-url}")
    private String tossPaymentCancelUrl;

    /**
     * 결제 수단별 결제 처리 (통합)
     * - CARD: 카드 결제 (연동없이 기록만 저장)
     * - DEPOSIT: 예치금 결제 (User 서비스 연동)
     * - TOSS_PAY: 토스페이먼츠 결제 confirm 으로 진입
     */
    @Override
    @Transactional
    public PaymentResponse process(PaymentReq request) {
        // 중복 결제 방지
        if (paymentRepository.findByOrdersIdxAndStatus(request.ordersIdx(), PaymentStatus.PAYMENT).isPresent()) {
            throw new PaymentFailedException(ErrorCode.ALREADY_PAID);
        }

        return switch (request.paymentType()) {
            case CARD -> handleCardPayment(request.userIdx(), request.orderCode(), request.ordersIdx(), request.amount());
            case DEPOSIT -> handleDepositPayment(request.userIdx(), request.orderCode(), request.ordersIdx(), request.amount());
            case TOSS_PAY -> {
                if (request.paymentKey() == null || request.paymentKey().isBlank()) {
                    throw new PaymentFailedException(ErrorCode.PAYMENT_KEY_NOT_FOUND);
                }
                yield handleTossPayPayment(request.userIdx(), request.orderCode(), request.ordersIdx(), request.paymentKey(), request.amount());
            }
        };
    }

    /**
     * 결제 환불 처리
     * - 결제 수단(DEPOSIT, TOSS_PAY, CARD)에 따라 환불 처리 수행
     * - 환불 내역을 별도의 Payment 엔티티로 저장 (REFUND 상태)
     * - Order 상태를 REFUNDED로 변경 (OrderClient 콜백)
     */
    @Override
    @Transactional
    public PaymentResponse refund(RefundReq request) {
        // 1. orderCode → ordersIdx 변환 (Order 서비스 통신)
        Long ordersIdx = orderClient.getOrderIdx(request.orderCode());

        // 2. ordersIdx + PAYMENT 상태로 결제 내역 조회
        PaymentEntity payment = paymentRepository.findByOrdersIdxAndStatus(ordersIdx, PaymentStatus.PAYMENT)
                .orElseThrow(() -> new PaymentFailedException(ErrorCode.PAYMENT_NOT_FOUND));

        // 3. 유저 유효성 검증
        if (!payment.getUsersIdx().equals(request.userIdx())) {
            throw new PaymentFailedException(ErrorCode.USER_MISMATCH);
        }

        BigDecimal refundAmount = payment.getAmount();

        switch (payment.getType()) {
            case DEPOSIT -> {
                try {
                    userClient.updateBalance(request.userIdx(), new BalanceClientReq(PaymentStatus.REFUND, refundAmount));
                    log.info("Balance 환불 성공: userIdx={}, amount={}", request.userIdx(), refundAmount);
                } catch (Exception e) {
                    log.error("Balance 환불 실패: userIdx={}, amount={}", request.userIdx(), refundAmount, e);
                    throw new PaymentFailedException(ErrorCode.PAYMENT_CANCEL_FAILED);
                }
            }
            case TOSS_PAY -> {
                if (payment.getPaymentKey() == null) {
                    throw new PaymentFailedException(ErrorCode.PAYMENT_KEY_NOT_FOUND);
                }
                try {
                    sendTossCancel(payment.getPaymentKey());
                    log.info("토스페이 환불 성공: paymentKey={}, amount={}", payment.getPaymentKey(), refundAmount);
                } catch (Exception e) {
                    log.error("토스페이 환불 실패: paymentKey={}, amount={}", payment.getPaymentKey(), refundAmount, e);
                    throw new PaymentFailedException(ErrorCode.PAYMENT_CANCEL_FAILED);
                }
            }
            case CARD -> log.warn("카드 환불 요청 - 추후 실제 PG 로직 필요: userIdx={}, ordersIdx={}, amount={}",
                    request.userIdx(), payment.getOrdersIdx(), refundAmount);
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

        return PaymentMapper.toResponse(refundPayment);
    }

    /**
     * 카드 결제 처리
     * - PG 연동 없이 결제 내역만 저장
     */
    private PaymentResponse handleCardPayment(Long userIdx, String orderCode, Long ordersIdx, BigDecimal amount) {
        PaymentEntity payment = PaymentEntity.builder()
                .usersIdx(userIdx)
                .ordersIdx(ordersIdx)
                .status(PaymentStatus.PAYMENT)
                .type(PaymentType.CARD)
                .amount(amount)
                .build();

        PaymentEntity saved = paymentRepository.save(payment);

        return PaymentMapper.toResponse(saved);
    }

    /**
     * 예치금(Balance) 결제 처리
     * - User 서비스에 Balance 차감 요청
     * - 차감 성공 시 결제 내역 저장 및 주문 상태 PAID로 변경
     */
    private PaymentResponse handleDepositPayment(Long userIdx, String orderCode, Long ordersIdx, BigDecimal amount) {
        try {
            userClient.updateBalance(userIdx, new BalanceClientReq(PaymentStatus.PAYMENT, amount));
        } catch (Exception e) {
            log.error("Balance(예치금) 결제 실패: userIdx={}, amount={}", userIdx, amount, e);
            throw new PaymentFailedException(ErrorCode.PAYMENT_FAILED);
        }

        PaymentEntity payment = PaymentEntity.builder()
                .usersIdx(userIdx)
                .ordersIdx(ordersIdx)
                .status(PaymentStatus.PAYMENT)
                .type(PaymentType.DEPOSIT)
                .amount(amount)
                .build();

        PaymentEntity saved = paymentRepository.save(payment);

        return PaymentMapper.toResponse(saved);
    }

    /**
     * 토스페이먼츠 결제 처리
     * - 토스 API에 결제 승인 요청 후 결제 내역 저장
     */
    private PaymentResponse handleTossPayPayment(
            Long userIdx,
            String orderCode,
            Long ordersIdx,
            String paymentKey,
            BigDecimal amount
    ) {
        BigDecimal resolvedAmount = amount != null ? amount : BigDecimal.ZERO;
        sendTossConfirm(orderCode, paymentKey, resolvedAmount);

        PaymentEntity payment = PaymentEntity.builder()
                .usersIdx(userIdx)
                .ordersIdx(ordersIdx)
                .status(PaymentStatus.PAYMENT)
                .type(PaymentType.TOSS_PAY)
                .amount(resolvedAmount)
                .paymentKey(paymentKey)
                .build();

        PaymentEntity saved = paymentRepository.save(payment);

        return PaymentMapper.toResponse(saved);
    }

    /**
     * 토스페이먼츠 결제 승인 API 호출
     */
    private void sendTossConfirm(String orderCode, String paymentKey, BigDecimal amount) {
        try {
            String authorization = "Basic " + Base64.getEncoder()
                    .encodeToString((tossPaymentConfirmSecrets + ":").getBytes(StandardCharsets.UTF_8));
            Map<String, Object> payload = Map.of(
                    "paymentKey", paymentKey,
                    "orderId", orderCode,
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
        } catch (InterruptedException e) {
            log.error("토스 결제 승인 요청 중 인터럽트 발생: orderCode={}, paymentKey={}", orderCode, paymentKey, e);
            Thread.currentThread().interrupt();
            throw new PaymentFailedException(ErrorCode.PAYMENT_FAILED);
        } catch (Exception e) {
            log.error("토스 결제 승인 요청 실패: orderCode={}, paymentKey={}", orderCode, paymentKey, e);
            throw new PaymentFailedException(ErrorCode.PAYMENT_FAILED);
        }
    }

    /**
     * 토스페이먼츠 결제 취소 API 호출
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
                throw new PaymentFailedException(ErrorCode.PAYMENT_CANCEL_FAILED);
            }

            log.info("토스페이 취소 API 응답: {}", response.body());
        } catch (PaymentFailedException e) {
            throw e;
        } catch (InterruptedException e) {
            log.error("토스 취소 요청 중 인터럽트 발생: paymentKey={}", paymentKey, e);
            Thread.currentThread().interrupt();
            throw new PaymentFailedException(ErrorCode.PAYMENT_CANCEL_FAILED);
        } catch (Exception e) {
            log.error("토스 취소 요청 실패: paymentKey={}", paymentKey, e);
            throw new PaymentFailedException(ErrorCode.PAYMENT_CANCEL_FAILED);
        }
    }

    /**
     * 예치금 충전
     */
    @Override
    @Transactional
    public PaymentResponse charge(Long userIdx, ChargeReq request) {
        if (request.paymentType() == PaymentType.DEPOSIT) {
            throw new PaymentFailedException(ErrorCode.INVALID_INPUT_VALUE);
        }

        try {
            userClient.updateBalance(userIdx, new BalanceClientReq(PaymentStatus.CHARGE, request.amount()));
        } catch (Exception e) {
            log.error("Balance(예치금) 충전 실패: userIdx={}, amount={}", userIdx, request.amount(), e);
            throw new PaymentFailedException(ErrorCode.CHARGE_FAILED);
        }

        PaymentEntity payment = PaymentEntity.builder()
                .usersIdx(userIdx)
                .status(PaymentStatus.CHARGE)
                .type(request.paymentType())
                .amount(request.amount())
                .paymentKey(java.util.UUID.randomUUID().toString())
                .build();
        PaymentEntity saved = paymentRepository.save(payment);

        return PaymentMapper.toResponse(saved);
    }

}
