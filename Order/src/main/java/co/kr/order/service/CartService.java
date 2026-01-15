package co.kr.order.service;

import co.kr.order.model.dto.request.ProductRequest;
import co.kr.order.model.dto.response.CartItemResponse;
import co.kr.order.model.dto.response.CartResponse;

public interface CartService {

    CartItemResponse addCartItem(Long userIdx, ProductRequest productRequest);
    CartItemResponse subtractCartItem(Long userIdx, ProductRequest productRequest);
    CartResponse getCartList(Long userIdx);
    CartItemResponse getCartItem(Long userIdx, ProductRequest productRequest);
    void deleteCartItem(Long userIdx, ProductRequest productRequest);
}
