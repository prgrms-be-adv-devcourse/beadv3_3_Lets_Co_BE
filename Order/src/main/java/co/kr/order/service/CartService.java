package co.kr.order.service;

import co.kr.order.model.dto.CartDetails;

import java.util.List;

public interface CartService {

    List<CartDetails> getCartList();
    void deleteCart(Long CartId);
}
