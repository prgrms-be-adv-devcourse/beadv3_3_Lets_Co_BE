package co.kr.order.controller;

import co.kr.order.model.dto.request.OrderCartRequest;
import co.kr.order.model.dto.request.OrderDirectRequest;
import co.kr.order.model.dto.response.BaseResponse;
import co.kr.order.model.dto.response.OrderResponse;
import co.kr.order.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    /*
     * @param servletRequest : userIdx
     * @param request : productIdx, optionIdx, quantity
     */
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

    /*
     * @param servletRequest : userIdx
     * @param request : productIdx, optionIdx, quantity
     */
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
    public ResponseEntity<BaseResponse<String>> refund(
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
    public ResponseEntity<BaseResponse<List<OrderResponse>>> getOrderList (
            HttpServletRequest servletRequest
    ) {

        String headerValue = servletRequest.getHeader("X-USERS-IDX");
        Long userIdx = (headerValue != null) ? Long.parseLong(headerValue) : null;

        List<OrderResponse> info = orderService.findOrderList(userIdx);

        BaseResponse<List<OrderResponse>> body = new BaseResponse<>("ok", info);
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

    /**
     * 주문 완료 처리 (배송 완료 후 호출)
     * - 결제 완료(PAID) 상태의 주문만 완료 처리 가능
     * - 주문 상태를 COMPLETED로 변경하고 정산 생성
     *
     * @param orderId 주문 ID
     */
    @PostMapping("/{orderId}/complete")
    public ResponseEntity<BaseResponse<Void>> completeOrder(@PathVariable Long orderId) {
        orderService.completeOrder(orderId);
        BaseResponse<Void> body = new BaseResponse<>("ok", null);
        return ResponseEntity.ok(body);
    }
}
