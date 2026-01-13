package co.kr.order.controller;

import co.kr.order.model.dto.request.CartRequest;
import co.kr.order.model.dto.response.BaseResponse;
import co.kr.order.model.dto.response.CartItemResponse;
import co.kr.order.model.dto.response.CartResponse;
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
    public ResponseEntity<BaseResponse<CartItemResponse>> addCartItem(
            @RequestHeader("Authorization") String token,
            @RequestBody CartRequest request
    ) {

        CartItemResponse info = cartService.addCartItem(token, request);
        BaseResponse<CartItemResponse> body = new BaseResponse<>("ok", info);

        return ResponseEntity.ok(body);
    }

    @PostMapping("/subtract")
    public ResponseEntity<BaseResponse<CartItemResponse>> subtractCartItem(
            @RequestHeader("Authorization") String token,
            @RequestBody CartRequest request
    ) {

        CartItemResponse info = cartService.subtractCartItem(token, request);
        BaseResponse<CartItemResponse> body = new BaseResponse<>("ok", info);

        return ResponseEntity.ok(body);
    }

    @GetMapping
    public ResponseEntity<BaseResponse<CartResponse>> getCartList(
            @RequestHeader("Authorization") String token
    ) {

        CartResponse info = cartService.getCartList(token);
        BaseResponse<CartResponse> body = new BaseResponse<>("ok", info);

        return ResponseEntity.ok(body);
    }

    @GetMapping
    public ResponseEntity<BaseResponse<CartItemResponse>> getCartItem(
            @RequestHeader("Authorization") String token,
            @RequestBody CartRequest request
    ) {

        CartItemResponse info = cartService.getCartItem(token, request);
        BaseResponse<CartItemResponse> body = new BaseResponse<>("ok", info);

        return ResponseEntity.ok(body);
    }

    @DeleteMapping
    public ResponseEntity<BaseResponse<Void>> deleteCartItem(
            @RequestHeader("Authorization") String token,
            @RequestBody CartRequest request
    ) {

        cartService.deleteCartItem(token, request);
        BaseResponse<Void> body = new BaseResponse<>("ok", null);

        return ResponseEntity.ok(body);
    }
}
