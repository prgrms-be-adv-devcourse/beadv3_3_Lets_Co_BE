package co.kr.user.controller;

import co.kr.user.model.DTO.Payment.PaymentDateOptionReq;
import co.kr.user.model.DTO.Payment.PaymentListDTO;
import co.kr.user.service.BalanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 사용자 잔액(Balance) 관리 컨트롤러
 * * <p>사용자의 현재 잔액 조회, 전체 사용 내역 조회,
 * 그리고 조건별(기간 등) 사용 내역 조회를 담당하는 API 엔드포인트를 제공합니다.</p>
 */
@Validated // 데이터 유효성 검증(Validation) 기능을 활성화합니다.
@RestController // @Controller + @ResponseBody, JSON 형태의 응답을 반환함을 명시합니다.
@RequiredArgsConstructor // final 필드에 대한 생성자를 자동으로 생성하여 의존성을 주입(DI)받습니다.
@RequestMapping("/users") // 이 컨트롤러의 모든 API 경로는 "/users"로 시작합니다.
public class BalanceController {
    // 잔액 관련 비즈니스 로직을 처리하는 서비스 계층
    private final BalanceService balanceService;

    /**
     * 사용자 현재 잔액 조회 API
     * * <p>HTTP Method: POST</p>
     * <p>Path: /users/balance</p>
     * * @param userIdx HTTP 헤더(X-USERS-IDX)에 포함된 사용자 고유 식별자 (PK)
     * @return 사용자의 현재 잔액(BigDecimal)을 ResponseEntity로 감싸서 반환 (200 OK)
     */
    @PostMapping("/balance")
    public ResponseEntity<BigDecimal> balance(@RequestHeader("X-USERS-IDX") Long userIdx) {
        // 서비스 계층을 통해 해당 유저의 현재 잔액을 조회하여 반환합니다.
        return ResponseEntity.ok(balanceService.balance(userIdx));
    }

    /**
     * 사용자 전체 잔액 변동 내역(결제/충전 내역) 조회 API
     * * <p>HTTP Method: POST</p>
     * <p>Path: /users/balance/history</p>
     * * @param userIdx HTTP 헤더(X-USERS-IDX)에 포함된 사용자 고유 식별자
     * @return 사용자의 모든 결제/충전 리스트(PaymentListDTO)를 반환 (200 OK)
     */
    @PostMapping("/balance/history")
    public ResponseEntity<List<PaymentListDTO>> balanceHistory(@RequestHeader("X-USERS-IDX") Long userIdx) {
        // 서비스 계층을 통해 해당 유저의 전체 이력을 조회합니다.
        return ResponseEntity.ok(balanceService.balanceHistory(userIdx));
    }

    /**
     * 조건별(옵션) 잔액 변동 내역 조회 API
     * * <p>특정 기간 조회 등 검색 조건을 요청 본문(Body)으로 받아 내역을 필터링하여 조회합니다.</p>
     * <p>HTTP Method: POST</p>
     * <p>Path: /users/balance/history/option</p>
     * * @param userIdx HTTP 헤더(X-USERS-IDX)에 포함된 사용자 고유 식별자
     * @param paymentDateOptionReq 조회할 조건(날짜 범위 등)이 담긴 요청 DTO (@RequestBody로 JSON 파싱)
     * @return 조건에 맞는 결제/충전 리스트(PaymentListDTO)를 반환 (200 OK)
     */
    @PostMapping("/balance/history/option")
    public ResponseEntity<List<PaymentListDTO>> balanceHistoryOption(@RequestHeader("X-USERS-IDX") Long userIdx,
                                                                     @RequestBody PaymentDateOptionReq paymentDateOptionReq) {
        // 유저 식별자와 검색 조건을 서비스 계층으로 전달하여 필터링된 이력을 조회합니다.
        return ResponseEntity.ok(balanceService.balanceHistoryOption(userIdx, paymentDateOptionReq));
    }
}