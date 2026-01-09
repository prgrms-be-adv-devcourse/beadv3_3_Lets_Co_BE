package co.kr.order.controller;

import co.kr.order.model.dto.AddCartRequest;
import co.kr.order.model.dto.BaseResponse;
import co.kr.order.model.dto.CartInfo;
import co.kr.order.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;

    @PostMapping
    public ResponseEntity<BaseResponse<CartInfo>> addCart(
            @RequestHeader("Authorization") String token,
            @RequestBody AddCartRequest request
    ) {

        CartInfo info = cartService.addCart(token, request.productIdx(), request.optionIdx());
        BaseResponse<CartInfo> res = new BaseResponse<>("ok", info);

        return ResponseEntity.ok(res);
    }

    @GetMapping
    public ResponseEntity<BaseResponse<CartInfo>> getCart(@RequestHeader("Authorization") String token) {

        CartInfo info = cartService.getCart(token);
        BaseResponse<CartInfo> res = new BaseResponse<>("ok", info);

        return ResponseEntity.ok(res);
    }
}
