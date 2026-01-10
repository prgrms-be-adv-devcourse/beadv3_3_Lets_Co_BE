package co.kr.order.service;

import co.kr.order.model.dto.CartInfo;

public interface CartService {

    CartInfo addCartItem(String token, Long productIdx, Long optionIdx);
    CartInfo subtractCartItem(String token, Long productIdx, Long optionIdx);
    CartInfo getCart(String token);
    void deleteCartItem(String token, Long productIdx, Long optionIdx);
}
