package co.kr.order.controller;

import co.kr.order.model.dto.request.OrderDirectRequest;
import co.kr.order.model.dto.request.UserDataRequest;
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

    /**
     * @param token : jwt Access 토큰
     * @param request : productIdx, optionIdx, quantity
     */
    @PostMapping
    public ResponseEntity<BaseResponse<OrderDirectResponse>> directOrder(
            @RequestHeader("Authorization") String token,
            @RequestBody OrderDirectRequest request
            ) {

        OrderDirectResponse info = orderService.directOrder(token, request);
        BaseResponse<OrderDirectResponse> body = new BaseResponse<>("ok", info);

        return ResponseEntity.ok(body);
    }

    /**
     * @param token : jwt Access 토큰
     * @param request : productIdx, optionIdx, quantity
     */
    @PostMapping("/cart")
    public ResponseEntity<BaseResponse<OrderCartResponse>> cartOrder(
            @RequestHeader("Authorization") String token,
            @RequestBody UserDataRequest request
    ) {

        OrderCartResponse info = orderService.cartOrder(token, request);
        BaseResponse<OrderCartResponse> body = new BaseResponse<>("ok", info);
        return ResponseEntity.ok(body);
    }
}
