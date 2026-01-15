package co.kr.order.controller;

import co.kr.order.model.dto.request.OrderDirectRequest;
import co.kr.order.model.dto.request.UserDataRequest;
import co.kr.order.model.dto.response.BaseResponse;
import co.kr.order.model.dto.response.OrderCartResponse;
import co.kr.order.model.dto.response.OrderDirectResponse;
import co.kr.order.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/order")
public class OrderController {

    private final OrderService orderService;

    /*
     * @param servletRequest : userIdx
     * @param request : productIdx, optionIdx, quantity
     */
    @PostMapping
    public ResponseEntity<BaseResponse<OrderDirectResponse>> directOrder(
            HttpServletRequest servletRequest,
            @RequestBody OrderDirectRequest request
            ) {

        String headerValue = servletRequest.getHeader("X-USERS-IDX");
        Long userIdx = (headerValue != null) ? Long.parseLong(headerValue) : null;

        OrderDirectResponse info = orderService.directOrder(userIdx, request);
        BaseResponse<OrderDirectResponse> body = new BaseResponse<>("ok", info);

        return ResponseEntity.ok(body);
    }

    /*
     * @param servletRequest : userIdx
     * @param request : productIdx, optionIdx, quantity
     */
    @PostMapping("/cart")
    public ResponseEntity<BaseResponse<OrderCartResponse>> cartOrder(
            HttpServletRequest servletRequest,
            @RequestBody UserDataRequest request
    ) {

        String headerValue = servletRequest.getHeader("X-USERS-IDX");
        Long userIdx = (headerValue != null) ? Long.parseLong(headerValue) : null;

        OrderCartResponse info = orderService.cartOrder(userIdx, request);
        BaseResponse<OrderCartResponse> body = new BaseResponse<>("ok", info);
        return ResponseEntity.ok(body);
    }
}
