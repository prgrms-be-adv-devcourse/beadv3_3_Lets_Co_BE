package co.kr.order.service;

import co.kr.order.model.dto.CartInfo;

public interface CartService {

    CartInfo addCart(String token, Long productId, Long optionIdx);
    CartInfo getCart(String token);
}
