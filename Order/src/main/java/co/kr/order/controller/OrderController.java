package co.kr.order.controller;

import co.kr.order.model.dto.request.ChargeRequest;
import co.kr.order.model.dto.request.OrderCartRequest;
import co.kr.order.model.dto.request.OrderDirectRequest;
import co.kr.order.model.dto.response.BaseResponse;
import co.kr.order.model.dto.response.OrderResponse;
import co.kr.order.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<BaseResponse<OrderResponse>> directOrder (
            HttpServletRequest servletRequest,
            @Valid @RequestBody OrderDirectRequest request
            ) {

        String headerValue = servletRequest.getHeader("X-USERS-IDX");
        Long userIdx = (headerValue != null) ? Long.parseLong(headerValue) : null;

        OrderResponse info = orderService.directOrder(userIdx, request);
        BaseResponse<OrderResponse> body = new BaseResponse<>("ok", info);

        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @PostMapping("/cart")
    public ResponseEntity<BaseResponse<OrderResponse>> cartOrder (
            HttpServletRequest servletRequest,
            @Valid @RequestBody OrderCartRequest request
    ) {

        String headerValue = servletRequest.getHeader("X-USERS-IDX");
        Long userIdx = (headerValue != null) ? Long.parseLong(headerValue) : null;

        OrderResponse info = orderService.cartOrder(userIdx, request);
        BaseResponse<OrderResponse> body = new BaseResponse<>("ok", info);

        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @PostMapping("/refund/{orderCode}")
    public ResponseEntity<BaseResponse<String>> refund (
            @PathVariable("orderCode") String orderCode,
            HttpServletRequest servletRequest
    ) {

        String headerValue = servletRequest.getHeader("X-USERS-IDX");
        Long userIdx = (headerValue != null) ? Long.parseLong(headerValue) : null;

        String info = orderService.refund(userIdx, orderCode);
        BaseResponse<String> body = new BaseResponse<>("ok", info);

        return ResponseEntity.ok(body);
    }

    @GetMapping
    public ResponseEntity<BaseResponse<Page<OrderResponse>>> getOrderList (
            HttpServletRequest servletRequest,
            @PageableDefault(
                    size = 10,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC) Pageable pageable
    ) {

        String headerValue = servletRequest.getHeader("X-USERS-IDX");
        Long userIdx = (headerValue != null) ? Long.parseLong(headerValue) : null;

        Page<OrderResponse> info = orderService.findOrderList(userIdx, pageable);

        BaseResponse<Page<OrderResponse>> body = new BaseResponse<>("ok", info);
        return ResponseEntity.ok(body);
    }

    @GetMapping("/{orderCode}")
    public ResponseEntity<BaseResponse<OrderResponse>> getOrderDetails (
            @PathVariable("orderCode") String orderCode,
            HttpServletRequest servletRequest
    ) {

        String headerValue = servletRequest.getHeader("X-USERS-IDX");
        Long userIdx = (headerValue != null) ? Long.parseLong(headerValue) : null;

        OrderResponse info = orderService.findOrder(userIdx, orderCode);

        BaseResponse<OrderResponse> body = new BaseResponse<>("ok", info);
        return ResponseEntity.ok(body);
    }


    @PostMapping("/deposit")
    public ResponseEntity<BaseResponse<String>> charge (
            HttpServletRequest servletRequest,
            @Valid @RequestBody ChargeRequest request
    ) {
        String headerValue = servletRequest.getHeader("X-USERS-IDX");
        Long userIdx = (headerValue != null) ? Long.parseLong(headerValue) : null;

        String msg = orderService.charge(userIdx, request);
        BaseResponse<String> body = new BaseResponse<>("ok", msg);

        return ResponseEntity.ok(body);
    }


    /**
     * 주문 완료 처리 (배송 완료 후 호출) (일단 보류)
     * - 결제 완료(PAID) 상태의 주문만 완료 처리 가능
     * - 주문 상태를 COMPLETED로 변경
     *
     * @param orderId 주문 ID
     */
    @PostMapping("/{orderId}/complete")
    public ResponseEntity<BaseResponse<Void>> completeOrder(@PathVariable Long orderId) {
        orderService.completeOrder(orderId);
        BaseResponse<Void> body = new BaseResponse<>("ok", null);
        return ResponseEntity.ok(body);
    }

    /**
     * 주문 상태 변경 (Payment에서 호출)
     * - 결제 완료/환불 시 Order 상태를 변경
     */
    @PatchMapping("/{orderCode}/status")
    public ResponseEntity<BaseResponse<Void>> updateOrderStatus(
            @PathVariable String orderCode,
            @RequestParam String status
    ) {
        orderService.updateOrderStatus(orderCode, status);
        BaseResponse<Void> body = new BaseResponse<>("ok", null);
        return ResponseEntity.ok(body);
    }
}
