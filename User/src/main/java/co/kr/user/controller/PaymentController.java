package co.kr.user.controller;

import co.kr.user.model.dto.payment.PaymentReq;
import co.kr.user.model.dto.payment.PaymentListDTO;
import co.kr.user.service.PaymentService;
import co.kr.user.util.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 결제 및 포인트 내역 관련 요청을 처리하는 컨트롤러입니다.
 * 사용자의 충전, 결제, 환불 이력을 조회하는 API를 제공합니다.
 */
@Validated // 요청 데이터의 유효성 검사를 활성화합니다.
@RestController // JSON 응답을 반환하는 컨트롤러입니다.
@RequiredArgsConstructor // final 필드에 대한 생성자 자동 주입을 설정합니다.
@RequestMapping("/users") // 기본 경로를 "/users"로 설정합니다.
public class PaymentController {
    // 결제 내역 조회 로직을 담당하는 서비스 주입
    private final PaymentService paymentService;

    /**
     * 사용자의 결제/이용 내역(History)을 조회합니다.
     * * @param userIdx 요청 헤더(X-USERS-IDX)에서 추출한 사용자 식별자 (인증된 사용자)
     * @param paymentReq 조회할 내역의 조건 (기간, 결제 상태, 유형 등)
     * @return 조건에 맞는 결제 내역 리스트를 담은 응답 객체
     */
    @PostMapping("/payment/history")
    public ResponseEntity<BaseResponse<List<PaymentListDTO>>> paymentHistory(@RequestHeader("X-USERS-IDX") Long userIdx,
                                                                             @RequestBody PaymentReq paymentReq) {
        // 서비스 계층을 호출하여 결제 내역을 조회하고 결과를 반환합니다.
        return ResponseEntity.ok(new BaseResponse<>("SUCCESS", paymentService.balanceHistory(userIdx, paymentReq)));
    }
}