package co.kr.order.controller;

import co.kr.order.model.dto.request.ClientProductReq;
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

    /*
     * @param servletRequest : userIdx
     * @param request : productIdx와 optionIdx
     * 장바구니에 단일상품 추가 (장바구니 페이지에서 상품 + 클릭)
     */
    @PostMapping("/add")
    public ResponseEntity<BaseResponse<CartItemRes>> addCartItem(
            HttpServletRequest servletRequest,
            @Valid @RequestBody ClientProductReq request
    ) {

        String headerValue = servletRequest.getHeader("X-USERS-IDX");
        Long userIdx = (headerValue != null) ? Long.parseLong(headerValue) : null;

        CartItemRes info = cartService.addCartItem(userIdx, request);
        BaseResponse<CartItemRes> body = new BaseResponse<>("ok", info);

        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    /*
     * @param servletRequest : userIdx
     * @param request : productIdx와 optionIdx
     * 장바구니 페이지에서 상품 - 클릭
     */
    @PostMapping("/subtract")
    public ResponseEntity<BaseResponse<CartItemRes>> subtractCartItem(
            HttpServletRequest servletRequest,
            @Valid @RequestBody ClientProductReq request
    ) {

        String headerValue = servletRequest.getHeader("X-USERS-IDX");
        Long userIdx = (headerValue != null) ? Long.parseLong(headerValue) : null;

        CartItemRes info = cartService.subtractCartItem(userIdx, request);
        BaseResponse<CartItemRes> body = new BaseResponse<>("ok", info);

        return ResponseEntity.ok(body);
    }

    /*
     * @param servletRequest : userIdx
     * 장바군니에 담겨 있는 모든 상품 정보 조회
     */
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

    /*
     * @param servletRequest : userIdx
     * @param request : productIdx와 optionIdx
     * 장바구니에 담겨 있는 단일 상품 정보 조회
     */
    @PostMapping("/item")
    public ResponseEntity<BaseResponse<CartItemRes>> getCartItem(
            HttpServletRequest servletRequest,
            @Valid @RequestBody ClientProductReq request
    ) {

        String headerValue = servletRequest.getHeader("X-USERS-IDX");
        Long userIdx = (headerValue != null) ? Long.parseLong(headerValue) : null;

        CartItemRes info = cartService.getCartItem(userIdx, request);
        BaseResponse<CartItemRes> body = new BaseResponse<>("ok", info);

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
            @Valid @RequestBody ClientProductReq request
    ) {

        String headerValue = servletRequest.getHeader("X-USERS-IDX");
        Long userIdx = (headerValue != null) ? Long.parseLong(headerValue) : null;

        cartService.deleteCartItem(userIdx, request);
        BaseResponse<Void> body = new BaseResponse<>("ok", null);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(body);
    }
}
