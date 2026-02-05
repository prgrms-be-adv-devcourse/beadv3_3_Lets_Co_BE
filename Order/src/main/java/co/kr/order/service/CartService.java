package co.kr.order.service;

import co.kr.order.model.dto.request.CartReq;
import co.kr.order.model.dto.response.CartItemRes;
import co.kr.order.model.dto.response.CartRes;

public interface CartService {

    CartItemRes addCartItem(Long userIdx, CartReq cartReq);
    CartItemRes subtractCartItem(Long userIdx, CartReq cartReq);
    CartRes getCartList(Long userIdx);
    CartItemRes getCartItem(Long userIdx, CartReq cartReq);
    void deleteCartItem(Long userIdx, CartReq cartReq);
}
