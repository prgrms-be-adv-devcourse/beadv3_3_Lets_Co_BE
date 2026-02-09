package co.kr.user.controller;

import co.kr.user.model.dto.Payment.PaymentReq;
import co.kr.user.model.dto.Payment.PaymentListDTO;
import co.kr.user.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 사용자 잔액(Balance) 관리 컨트롤러 클래스입니다.
 * 사용자의 현재 보유 잔액 조회, 전체 거래 내역(충전/사용) 조회,
 * 그리고 기간별 조건 검색 기능을 제공하는 API 엔드포인트를 담당합니다.
 */
@Validated // 요청 데이터의 유효성 검증(Validation) 기능을 활성화합니다.
@RestController // RESTful API 컨트롤러임을 명시하며, 응답 데이터를 JSON으로 반환합니다.
@RequiredArgsConstructor // final 필드에 대한 생성자를 자동으로 생성하여 의존성을 주입(DI)받습니다.
@RequestMapping("/users") // 이 클래스 내의 모든 API 경로는 "/users"로 시작합니다.
public class PaymentController {

    // 잔액 및 결제 내역 관련 비즈니스 로직을 처리하는 서비스 객체입니다.
    private final PaymentService paymentService;


    /**
     * 사용자 전체 잔액 변동 내역(결제/충전 내역) 조회 API
     * 사용자의 모든 예치금 충전 및 사용 이력을 조회합니다.
     *
     * @param userIdx HTTP 헤더(X-USERS-IDX)에 포함된 사용자 고유 식별자
     * @return ResponseEntity<List<PaymentListDTO>> 결제 및 충전 내역 리스트(PaymentListDTO)를 포함한 응답 객체 (HTTP 200 OK)
     */
    @PostMapping("/payment/history")
    public ResponseEntity<List<PaymentListDTO>> PaymentHistory(@RequestHeader("X-USERS-IDX") Long userIdx,
                                                               @RequestBody PaymentReq paymentReq) {
        // BalanceService를 호출하여 사용자의 전체 거래 이력을 조회하고 반환합니다.
        return ResponseEntity.ok(paymentService.balanceHistory(userIdx, paymentReq));
    }
}