package co.kr.order.service;

import co.kr.order.model.dto.ProductInfo;
import co.kr.order.model.dto.response.CartItemRes;

import java.util.List;

public interface CartService {

    CartItemRes addCartItem(Long userIdx, ProductInfo cartReq);
    CartItemRes plusCartItem(Long userIdx, String optionCode);
    CartItemRes minusCartItem(Long userIdx, String optionCode);
    void deleteCartItem(Long userIdx, String optionCode);
    void deleteCartAll(Long userIdx);
    List<CartItemRes> getCartList(Long userIdx);
}
