package co.kr.order.controller;

import co.kr.order.model.dto.ProductInfo;
import co.kr.order.model.dto.response.BaseResponse;
import co.kr.order.model.dto.response.CartItemRes;
import co.kr.order.service.CartService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/carts")
public class CartController {

    private final CartService cartService;

    @PostMapping("/add")
    public ResponseEntity<BaseResponse<CartItemRes>> addCartItem(
            HttpServletRequest servletRequest,
            @Valid @RequestBody ProductInfo productInfo
    ) {

        String headerValue = servletRequest.getHeader("X-USERS-IDX");
        Long userIdx = (headerValue != null) ? Long.parseLong(headerValue) : null;

        CartItemRes info = cartService.addCartItem(userIdx, productInfo);
        BaseResponse<CartItemRes> body = new BaseResponse<>("ok", info);

        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @PostMapping("/plus/{optionCode}")
    public ResponseEntity<BaseResponse<CartItemRes>> plusCartItem(
            HttpServletRequest servletRequest,
            @PathVariable("optionCode") String optionCode
    ) {

        String headerValue = servletRequest.getHeader("X-USERS-IDX");
        Long userIdx = (headerValue != null) ? Long.parseLong(headerValue) : null;

        CartItemRes info = cartService.plusCartItem(userIdx, optionCode);
        BaseResponse<CartItemRes> body = new BaseResponse<>("ok", info);

        return ResponseEntity.ok(body);
    }

    @PostMapping("/minus/{optionCode}")
    public ResponseEntity<BaseResponse<CartItemRes>> minusCartItem(
            HttpServletRequest servletRequest,
            @PathVariable("optionCode") String optionCode
    ) {

        String headerValue = servletRequest.getHeader("X-USERS-IDX");
        Long userIdx = (headerValue != null) ? Long.parseLong(headerValue) : null;

        CartItemRes info = cartService.minusCartItem(userIdx, optionCode);
        BaseResponse<CartItemRes> body = new BaseResponse<>("ok", info);

        return ResponseEntity.ok(body);
    }

    @GetMapping
    public ResponseEntity<BaseResponse<List<CartItemRes>>> getCartList(
            HttpServletRequest servletRequest
    ) {

        String headerValue = servletRequest.getHeader("X-USERS-IDX");
        Long userIdx = (headerValue != null) ? Long.parseLong(headerValue) : null;

        List<CartItemRes> info = cartService.getCartList(userIdx);
        BaseResponse<List<CartItemRes>> body = new BaseResponse<>("ok", info);

        return ResponseEntity.ok(body);
    }

    @DeleteMapping("/{optionCode}")
    public ResponseEntity<BaseResponse<Void>> deleteCartItem(
            HttpServletRequest servletRequest,
            @PathVariable("optionCode") String optionCode
    ) {

        String headerValue = servletRequest.getHeader("X-USERS-IDX");
        Long userIdx = (headerValue != null) ? Long.parseLong(headerValue) : null;

        cartService.deleteCartItem(userIdx, optionCode);
        BaseResponse<Void> body = new BaseResponse<>("ok", null);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(body);
    }
}
