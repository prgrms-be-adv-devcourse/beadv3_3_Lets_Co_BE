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

    /*
     * 장바구니에 제품 추가 요청 (POST)
     * @param productInfo: 장바구니 제품 추가 요청 정보
     */
    @PostMapping("/add")
    public ResponseEntity<BaseResponse<CartItemRes>> addCartItem(
            HttpServletRequest servletRequest,
            @Valid @RequestBody ProductInfo productInfo
    ) {

        // 해더의 Key가 "X-USERS-IDX"인 Value 가져오기
        String headerValue = servletRequest.getHeader("X-USERS-IDX");
        Long userIdx = (headerValue != null) ? Long.parseLong(headerValue) : null;

        // 장바구니 추가
        CartItemRes info = cartService.addCartItem(userIdx, productInfo);
        BaseResponse<CartItemRes> body = new BaseResponse<>("ok", info);

        // 장바구니 정보 응답 (CREATED)
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    /*
     * 장바구니 상품 개수 +1 요청 (POST)
     * @param optionCode: 상품 옵션 코드
     */
    @PostMapping("/plus/{optionCode}")
    public ResponseEntity<BaseResponse<CartItemRes>> plusCartItem(
            HttpServletRequest servletRequest,
            @PathVariable("optionCode") String optionCode
    ) {

        // 해더의 Key가 "X-USERS-IDX"인 Value 가져오기
        String headerValue = servletRequest.getHeader("X-USERS-IDX");
        Long userIdx = (headerValue != null) ? Long.parseLong(headerValue) : null;

        // 장바구니 상품 개수 +1
        CartItemRes info = cartService.plusCartItem(userIdx, optionCode);
        BaseResponse<CartItemRes> body = new BaseResponse<>("ok", info);

        // 장바구니 정보 응답 (OK)
        return ResponseEntity.ok(body);
    }

    /*
     * 장바구니 상품 개수 -1 (POST)
     * @param optionCode: 상품 옵션 코드
     */
    @PostMapping("/minus/{optionCode}")
    public ResponseEntity<BaseResponse<CartItemRes>> minusCartItem(
            HttpServletRequest servletRequest,
            @PathVariable("optionCode") String optionCode
    ) {

        // 해더의 Key가 "X-USERS-IDX"인 Value 가져오기
        String headerValue = servletRequest.getHeader("X-USERS-IDX");
        Long userIdx = (headerValue != null) ? Long.parseLong(headerValue) : null;

        // 장바구니 상품 개수 -1
        CartItemRes info = cartService.minusCartItem(userIdx, optionCode);
        BaseResponse<CartItemRes> body = new BaseResponse<>("ok", info);

        // 장바구니 정보 응답 (OK)
        return ResponseEntity.ok(body);
    }

    /*
     * 장바구니에 추가한 상품 리스트 요청 (GET)
     */
    @GetMapping
    public ResponseEntity<BaseResponse<List<CartItemRes>>> getCartList(
            HttpServletRequest servletRequest
    ) {

        // 해더의 Key가 "X-USERS-IDX"인 Value 가져오기
        String headerValue = servletRequest.getHeader("X-USERS-IDX");
        Long userIdx = (headerValue != null) ? Long.parseLong(headerValue) : null;

        // 장바구니 상품 리스트 가져오기
        List<CartItemRes> info = cartService.getCartList(userIdx);
        BaseResponse<List<CartItemRes>> body = new BaseResponse<>("ok", info);

        // 장바구니 정보 응답 (OK)
        return ResponseEntity.ok(body);
    }

    /*
     * 장바구니 제품 삭제 요청 (DELETE)
     * @param optionCode: 제품 옵션 코드
     */
    @DeleteMapping("/{optionCode}")
    public ResponseEntity<BaseResponse<Void>> deleteCartItem(
            HttpServletRequest servletRequest,
            @PathVariable("optionCode") String optionCode
    ) {

        // 해더의 Key가 "X-USERS-IDX"인 Value 가져오기
        String headerValue = servletRequest.getHeader("X-USERS-IDX");
        Long userIdx = (headerValue != null) ? Long.parseLong(headerValue) : null;

        // 장바구니 제품 삭제
        cartService.deleteCartItem(userIdx, optionCode);
        BaseResponse<Void> body = new BaseResponse<>("ok", null);

        // 응답 (NO_CONTENT)
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(body);
    }
}
