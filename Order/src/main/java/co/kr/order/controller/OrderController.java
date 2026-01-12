package co.kr.order.controller;

import co.kr.order.model.dto.BaseResponse;
import co.kr.order.model.dto.OrderItemInfo;
import co.kr.order.model.dto.OrderRequest;
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
    public ResponseEntity<BaseResponse<OrderItemInfo>> directOrder(
            @RequestHeader("Authorization") String token,
            @RequestBody OrderRequest orderRequest
            ) {

        OrderItemInfo info = orderService.directOrder(token, orderRequest);
        BaseResponse<OrderItemInfo> res = new BaseResponse<>("ok", info);

        return ResponseEntity.ok(res);
    }
}
