package co.kr.order.service;

import co.kr.order.model.dto.request.ProductRequest;
import co.kr.order.model.dto.response.CartItemResponse;
import co.kr.order.model.dto.response.CartResponse;

public interface CartService {

    CartItemResponse addCartItem(String token, ProductRequest productRequest);
    CartItemResponse subtractCartItem(String token, ProductRequest productRequest);
    CartResponse getCartList(String token);
    CartItemResponse getCartItem(String token, ProductRequest productRequest);
    void deleteCartItem(String token, ProductRequest productRequest);
}
