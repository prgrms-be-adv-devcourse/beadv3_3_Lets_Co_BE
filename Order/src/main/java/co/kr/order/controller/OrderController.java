package co.kr.order.controller;

import co.kr.order.model.dto.request.OrderDirectRequest;
import co.kr.order.model.dto.UserData;
import co.kr.order.model.dto.response.BaseResponse;
import co.kr.order.model.dto.response.OrderListResponse;
import co.kr.order.model.dto.response.OrderResponse;
import co.kr.order.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<BaseResponse<OrderListResponse>> cartOrder (
            HttpServletRequest servletRequest,
            @Valid @RequestBody UserData request
    ) {

        String headerValue = servletRequest.getHeader("X-USERS-IDX");
        Long userIdx = (headerValue != null) ? Long.parseLong(headerValue) : null;

        OrderListResponse info = orderService.cartOrder(userIdx, request);
        BaseResponse<OrderListResponse> body = new BaseResponse<>("ok", info);

        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @GetMapping
    public ResponseEntity<BaseResponse<OrderListResponse>> getOrderList (
            HttpServletRequest servletRequest
    ) {

        String headerValue = servletRequest.getHeader("X-USERS-IDX");
        Long userIdx = (headerValue != null) ? Long.parseLong(headerValue) : null;

        OrderListResponse info = orderService.findOrderList(userIdx);

        BaseResponse<OrderListResponse> body = new BaseResponse<>("ok", info);
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
}
