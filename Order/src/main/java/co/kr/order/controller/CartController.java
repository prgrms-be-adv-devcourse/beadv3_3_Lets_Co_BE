package co.kr.order.controller;

import co.kr.order.model.dto.request.ProductRequest;
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

    /*
     * @param token : jwt Access 토큰
     * @param request : productIdx와 optionIdx
     * 장바구니에 단일상품 추가 (장바구니 페이지에서 상품 + 클릭)
     */
    @PostMapping("/add")
    public ResponseEntity<BaseResponse<CartItemResponse>> addCartItem(
            @RequestHeader("Authorization") String token,
            @RequestBody ProductRequest request
    ) {

        CartItemResponse info = cartService.addCartItem(token, request);
        BaseResponse<CartItemResponse> body = new BaseResponse<>("ok", info);

        // ok(200)으로 리턴
        return ResponseEntity.ok(body);
    }

    /*
     * @param token : jwt Access 토큰
     * @param request : productIdx와 optionIdx
     * 장바구니 페이지에서 상품 - 클릭
     */
    @PostMapping("/subtract")
    public ResponseEntity<BaseResponse<CartItemResponse>> subtractCartItem(
            @RequestHeader("Authorization") String token,
            @RequestBody ProductRequest request
    ) {

        CartItemResponse info = cartService.subtractCartItem(token, request);
        BaseResponse<CartItemResponse> body = new BaseResponse<>("ok", info);

        // ok(200)으로 리턴
        return ResponseEntity.ok(body);
    }

    /*
     * @param token : jwt Access 토큰
     * 장바군니에 담겨 있는 모든 상품 정보 조회
     */
    @GetMapping
    public ResponseEntity<BaseResponse<CartResponse>> getCartList(
            @RequestHeader("Authorization") String token
    ) {

        CartResponse info = cartService.getCartList(token);
        BaseResponse<CartResponse> body = new BaseResponse<>("ok", info);

        // ok(200)으로 리턴
        return ResponseEntity.ok(body);
    }

    /*
     * @param token : jwt Access 토큰
     * @param request : productIdx와 optionIdx
     * 장바구니에 담겨 있는 단일 상품 정보 조회
     */
    @GetMapping("/item")
    public ResponseEntity<BaseResponse<CartItemResponse>> getCartItem(
            @RequestHeader("Authorization") String token,
            @RequestBody ProductRequest request
    ) {

        CartItemResponse info = cartService.getCartItem(token, request);
        BaseResponse<CartItemResponse> body = new BaseResponse<>("ok", info);

        // ok(200)으로 리턴
        return ResponseEntity.ok(body);
    }

    /*
     * @param token : jwt Access 토큰
     * @param request : productIdx와 optionIdx
     * 장바구니 상품 삭제 (단일 제품삭제임, 전체 삭제 아님)
     */
    @DeleteMapping
    public ResponseEntity<BaseResponse<Void>> deleteCartItem(
            @RequestHeader("Authorization") String token,
            @RequestBody ProductRequest request
    ) {

        cartService.deleteCartItem(token, request);
        BaseResponse<Void> body = new BaseResponse<>("ok", null);

        // ok(200)으로 리턴
        return ResponseEntity.ok(body);
    }
}
