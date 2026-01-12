package co.kr.order.controller;

import co.kr.order.model.dto.CartRequest;
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

    @PostMapping("/add")
    public ResponseEntity<BaseResponse<CartInfo>> addCartItem(
            @RequestHeader("Authorization") String token,
            @RequestBody CartRequest request
    ) {

        CartInfo info = cartService.addCartItem(token, request.productIdx(), request.optionIdx());
        BaseResponse<CartInfo> res = new BaseResponse<>("ok", info);

        return ResponseEntity.ok(res);
    }

    @PostMapping("/subtract")
    public ResponseEntity<BaseResponse<CartInfo>> subtractCartItem(
            @RequestHeader("Authorization") String token,
            @RequestBody CartRequest request
    ) {

        CartInfo info = cartService.subtractCartItem(token, request.productIdx(), request.optionIdx());
        BaseResponse<CartInfo> res = new BaseResponse<>("ok", info);

        return ResponseEntity.ok(res);
    }

    @GetMapping
    public ResponseEntity<BaseResponse<CartInfo>> getCart(@RequestHeader("Authorization") String token) {

        CartInfo info = cartService.getCart(token);
        BaseResponse<CartInfo> res = new BaseResponse<>("ok", info);

        return ResponseEntity.ok(res);
    }

    @DeleteMapping
    public ResponseEntity<BaseResponse<Void>> deleteCartItem(
            @RequestHeader("Authorization") String token,
            @RequestBody CartRequest request
    ) {

        cartService.deleteCartItem(token, request.productIdx(), request.optionIdx());
        BaseResponse<Void> res = new BaseResponse<>("ok", null);

        return ResponseEntity.ok(res);
    }
}
