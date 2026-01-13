package co.kr.order.controller;

import co.kr.order.model.dto.request.CartOrderRequest;
import co.kr.order.model.dto.request.OrderRequest;
import co.kr.order.model.dto.response.BaseResponse;
import co.kr.order.model.dto.response.OrderCartResponse;
import co.kr.order.model.dto.response.OrderDirectResponse;
import co.kr.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/order")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<BaseResponse<OrderDirectResponse>> directOrder(
            @RequestHeader("Authorization") String token,
            @RequestBody OrderRequest orderRequest
            ) {

        OrderDirectResponse info = orderService.directOrder(token, orderRequest);
        BaseResponse<OrderDirectResponse> body = new BaseResponse<>("ok", info);

        return ResponseEntity.ok(body);
    }

    @PostMapping("/cart")
    public ResponseEntity<BaseResponse<OrderCartResponse>> cartOrder(
            @RequestHeader("Authorization") String token,
            @RequestBody CartOrderRequest cartOrderRequest
    ) {

        OrderCartResponse info = orderService.cartOrder(token, cartOrderRequest);
        BaseResponse<OrderCartResponse> body = new BaseResponse<>("ok", info);
        return ResponseEntity.ok(body);
    }
}
