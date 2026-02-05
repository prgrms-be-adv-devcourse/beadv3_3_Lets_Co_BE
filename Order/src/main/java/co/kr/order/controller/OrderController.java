package co.kr.order.controller;

import co.kr.order.model.dto.request.OrderCartReq;
import co.kr.order.model.dto.request.OrderDirectReq;
import co.kr.order.common.BaseResponse;
import co.kr.order.model.dto.response.OrderRes;
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

@CrossOrigin(origins = "http://localhost:8081", allowedHeaders = "*")
@RestController
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<BaseResponse<OrderRes>> directOrder (
            HttpServletRequest servletRequest,
            @Valid @RequestBody OrderDirectReq request
            ) {

        String headerValue = servletRequest.getHeader("X-USERS-IDX");
        Long userIdx = (headerValue != null) ? Long.parseLong(headerValue) : null;

        OrderRes info = orderService.directOrder(userIdx, request);
        BaseResponse<OrderRes> body = new BaseResponse<>("ok", info);

        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @PostMapping("/cart")
    public ResponseEntity<BaseResponse<OrderRes>> cartOrder (
            HttpServletRequest servletRequest,
            @Valid @RequestBody OrderCartReq request
    ) {

        String headerValue = servletRequest.getHeader("X-USERS-IDX");
        Long userIdx = (headerValue != null) ? Long.parseLong(headerValue) : null;

        OrderRes info = orderService.cartOrder(userIdx, request);
        BaseResponse<OrderRes> body = new BaseResponse<>("ok", info);

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
    public ResponseEntity<BaseResponse<Page<OrderRes>>> getOrderList (
            HttpServletRequest servletRequest,
            @PageableDefault(
                    size = 10,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC) Pageable pageable
    ) {

        String headerValue = servletRequest.getHeader("X-USERS-IDX");
        Long userIdx = (headerValue != null) ? Long.parseLong(headerValue) : null;

        Page<OrderRes> info = orderService.findOrderList(userIdx, pageable);

        BaseResponse<Page<OrderRes>> body = new BaseResponse<>("ok", info);
        return ResponseEntity.ok(body);
    }

    @GetMapping("/{orderCode}")
    public ResponseEntity<BaseResponse<OrderRes>> getOrderDetails (
            @PathVariable("orderCode") String orderCode,
            HttpServletRequest servletRequest
    ) {

        String headerValue = servletRequest.getHeader("X-USERS-IDX");
        Long userIdx = (headerValue != null) ? Long.parseLong(headerValue) : null;

        OrderRes info = orderService.findOrder(userIdx, orderCode);

        BaseResponse<OrderRes> body = new BaseResponse<>("ok", info);
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
    @PostMapping("/{orderCode}/status")
    public ResponseEntity<BaseResponse<Void>> updateOrderStatus(
            @PathVariable String orderCode,
            @RequestParam String status
    ) {
        orderService.updateOrderStatus(orderCode, status);
        BaseResponse<Void> body = new BaseResponse<>("ok", null);
        return ResponseEntity.ok(body);
    }
}
