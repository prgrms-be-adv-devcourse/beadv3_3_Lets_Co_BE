package co.kr.order.controller;

import co.kr.order.model.dto.request.CartReq;
import co.kr.order.model.dto.response.BaseResponse;
import co.kr.order.model.dto.response.CartItemRes;
import co.kr.order.model.dto.response.CartRes;
import co.kr.order.service.CartService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/carts")
public class CartController {

    private final CartService cartService;

    @PostMapping("/add")
    public ResponseEntity<BaseResponse<CartItemRes>> addCartItem(
            HttpServletRequest servletRequest,
            @Valid @RequestBody CartReq cartRequest
    ) {

        String headerValue = servletRequest.getHeader("X-USERS-IDX");
        Long userIdx = (headerValue != null) ? Long.parseLong(headerValue) : null;

        CartItemRes info = cartService.addCartItem(userIdx, cartRequest);
        BaseResponse<CartItemRes> body = new BaseResponse<>("ok", info);

        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @PostMapping("/subtract")
    public ResponseEntity<BaseResponse<CartItemRes>> subtractCartItem(
            HttpServletRequest servletRequest,
            @Valid @RequestBody CartReq cartRequest
    ) {

        String headerValue = servletRequest.getHeader("X-USERS-IDX");
        Long userIdx = (headerValue != null) ? Long.parseLong(headerValue) : null;

        CartItemRes info = cartService.subtractCartItem(userIdx, cartRequest);
        BaseResponse<CartItemRes> body = new BaseResponse<>("ok", info);

        return ResponseEntity.ok(body);
    }

    @GetMapping
    public ResponseEntity<BaseResponse<CartRes>> getCartList(
            HttpServletRequest servletRequest
    ) {

        String headerValue = servletRequest.getHeader("X-USERS-IDX");
        Long userIdx = (headerValue != null) ? Long.parseLong(headerValue) : null;

        CartRes info = cartService.getCartList(userIdx);
        BaseResponse<CartRes> body = new BaseResponse<>("ok", info);

        return ResponseEntity.ok(body);
    }

    @DeleteMapping
    public ResponseEntity<BaseResponse<Void>> deleteCartItem(
            HttpServletRequest servletRequest,
            @Valid @RequestBody CartReq cartRequest
    ) {

        String headerValue = servletRequest.getHeader("X-USERS-IDX");
        Long userIdx = (headerValue != null) ? Long.parseLong(headerValue) : null;

        cartService.deleteCartItem(userIdx, cartRequest);
        BaseResponse<Void> body = new BaseResponse<>("ok", null);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(body);
    }
}
