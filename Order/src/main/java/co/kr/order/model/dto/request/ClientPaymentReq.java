package co.kr.order.model.dto.request;

import co.kr.order.model.vo.PaymentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/*
 * 결제 요청 정보
 * @param userIdx: 유저 인덱스
 * @param orderCode: 주문 코드
 * @param ordersIdx: 주문 인덱스
 * @param paymentType: 결제 타입 (DEPOSIT, TOSS_PAY, CARD)
 * @param amount: 결제 금액
 * @param paymentKey: 토스 결제 key
 */
public record ClientPaymentReq(

        @NotBlank(message = "주문 코드는 필수입니다.")
        Long userIdx,

        @NotBlank(message = "주문 코드는 필수입니다.")
        String orderCode,

        @NotNull(message = "주문 IDX는 필수입니다.")
        Long ordersIdx,

        @NotNull(message = "결제 타입은 필수입니다.")
        PaymentType paymentType,

        @NotNull(message = "결제 금액은 필수입니다.")
        BigDecimal amount,

        // TOSS_PAY일 때만 사용, CARD/DEPOSIT은 null
        String paymentKey
) {}
