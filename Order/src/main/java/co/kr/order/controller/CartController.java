package co.kr.order.controller;

import co.kr.order.model.dto.request.ProductRequest;
import co.kr.order.model.dto.response.BaseResponse;
import co.kr.order.model.dto.response.CartItemResponse;
import co.kr.order.model.dto.response.CartResponse;
import co.kr.order.service.CartService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/carts")
public class CartController {

    private final CartService cartService;

    /*
     * @param servletRequest : userIdx
     * @param request : productIdx와 optionIdx
     * 장바구니에 단일상품 추가 (장바구니 페이지에서 상품 + 클릭)
     */
    @PostMapping("/add")
    public ResponseEntity<BaseResponse<CartItemResponse>> addCartItem(
            HttpServletRequest servletRequest,
            @RequestBody ProductRequest request
    ) {

        String headerValue = servletRequest.getHeader("X-USERS-IDX");
        Long userIdx = (headerValue != null) ? Long.parseLong(headerValue) : null;

        CartItemResponse info = cartService.addCartItem(userIdx, request);
        BaseResponse<CartItemResponse> body = new BaseResponse<>("ok", info);

        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    /*
     * @param servletRequest : userIdx
     * @param request : productIdx와 optionIdx
     * 장바구니 페이지에서 상품 - 클릭
     */
    @PostMapping("/subtract")
    public ResponseEntity<BaseResponse<CartItemResponse>> subtractCartItem(
            HttpServletRequest servletRequest,
            @RequestBody ProductRequest request
    ) {

        String headerValue = servletRequest.getHeader("X-USERS-IDX");
        Long userIdx = (headerValue != null) ? Long.parseLong(headerValue) : null;

        CartItemResponse info = cartService.subtractCartItem(userIdx, request);
        BaseResponse<CartItemResponse> body = new BaseResponse<>("ok", info);

        return ResponseEntity.ok(body);
    }

    /*
     * @param servletRequest : userIdx
     * 장바군니에 담겨 있는 모든 상품 정보 조회
     */
    @GetMapping
    public ResponseEntity<BaseResponse<CartResponse>> getCartList(
            HttpServletRequest servletRequest
    ) {

        String headerValue = servletRequest.getHeader("X-USERS-IDX");
        Long userIdx = (headerValue != null) ? Long.parseLong(headerValue) : null;

        CartResponse info = cartService.getCartList(userIdx);
        BaseResponse<CartResponse> body = new BaseResponse<>("ok", info);

        return ResponseEntity.ok(body);
    }

    /*
     * @param servletRequest : userIdx
     * @param request : productIdx와 optionIdx
     * 장바구니에 담겨 있는 단일 상품 정보 조회
     */
    @GetMapping("/item")
    public ResponseEntity<BaseResponse<CartItemResponse>> getCartItem(
            HttpServletRequest servletRequest,
            @RequestBody ProductRequest request
    ) {

        String headerValue = servletRequest.getHeader("X-USERS-IDX");
        Long userIdx = (headerValue != null) ? Long.parseLong(headerValue) : null;

        CartItemResponse info = cartService.getCartItem(userIdx, request);
        BaseResponse<CartItemResponse> body = new BaseResponse<>("ok", info);

        return ResponseEntity.ok(body);
    }

    /*
     * @param servletRequest : userIdx
     * @param request : productIdx와 optionIdx
     * 장바구니 상품 삭제 (단일 제품삭제임, 전체 삭제 아님)
     */
    @DeleteMapping
    public ResponseEntity<BaseResponse<Void>> deleteCartItem(
            HttpServletRequest servletRequest,
            @RequestBody ProductRequest request
    ) {

        String headerValue = servletRequest.getHeader("X-USERS-IDX");
        Long userIdx = (headerValue != null) ? Long.parseLong(headerValue) : null;

        cartService.deleteCartItem(userIdx, request);
        BaseResponse<Void> body = new BaseResponse<>("ok", null);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(body);
    }
}
