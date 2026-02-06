package co.kr.order.service;

import co.kr.order.model.dto.request.CartReq;
import co.kr.order.model.dto.response.CartItemRes;

import java.util.List;

public interface CartService {

    CartItemRes addCartItem(Long userIdx, CartReq cartReq);
    CartItemRes subtractCartItem(Long userIdx, CartReq cartReq);
    void deleteCartItem(Long userIdx, CartReq cartReq);
    void deleteCartAll(Long userIdx);
    List<CartItemRes> getCartList(Long userIdx);
}
