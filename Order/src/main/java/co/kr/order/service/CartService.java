package co.kr.order.service;

import co.kr.order.model.dto.request.CartRequest;
import co.kr.order.model.dto.response.CartItemResponse;
import co.kr.order.model.dto.response.CartResponse;

public interface CartService {

    CartItemResponse addCartItem(String token, CartRequest cartRequest);
    CartItemResponse subtractCartItem(String token, CartRequest cartRequest);
    CartResponse getCartList(String token);
    CartItemResponse getCartItem(String token, CartRequest cartRequest);
    void deleteCartItem(String token, CartRequest cartRequest);
}
