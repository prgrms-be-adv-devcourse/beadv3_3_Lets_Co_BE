package co.kr.order.domain.service;

import co.kr.order.domain.model.dto.CartInfo;

public interface CartService {

    CartInfo addCartItem(String token, Long productIdx, Long optionIdx);
    CartInfo subtractCartItem(String token, Long productIdx, Long optionIdx);
    CartInfo getCart(String token);
    void deleteCartItem(String token, Long productIdx, Long optionIdx);
}
