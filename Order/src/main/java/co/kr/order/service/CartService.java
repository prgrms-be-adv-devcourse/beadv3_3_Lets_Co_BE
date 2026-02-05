package co.kr.order.service;

import co.kr.order.model.dto.request.ClientProductReq;
import co.kr.order.model.dto.response.CartItemRes;
import co.kr.order.model.dto.response.CartRes;

public interface CartService {

    CartItemRes addCartItem(Long userIdx, ClientProductReq clientProductReq);
    CartItemRes subtractCartItem(Long userIdx, ClientProductReq clientProductReq);
    CartRes getCartList(Long userIdx);
    CartItemRes getCartItem(Long userIdx, ClientProductReq clientProductReq);
    void deleteCartItem(Long userIdx, ClientProductReq clientProductReq);
}
