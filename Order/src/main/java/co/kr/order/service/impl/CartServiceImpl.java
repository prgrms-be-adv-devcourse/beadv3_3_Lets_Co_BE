package co.kr.order.service.impl;

import co.kr.order.client.ProductClient;
import co.kr.order.client.UserClient;
import co.kr.order.mapper.CartMapper;
import co.kr.order.model.dto.CartInfo;
import co.kr.order.model.dto.ProductInfo;
import co.kr.order.model.entity.CartEntity;
import co.kr.order.repository.CartJpaRepository;
import co.kr.order.service.CartService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartJpaRepository cartJpaRepository;
    private final ProductClient productClient;
    private final UserClient userClient;

    @Override
    @Transactional
    public CartInfo addCart(String token, Long productIdx, Long optionIdx) {
        Long userId = userClient.getUserId(token);

        ProductInfo productInfo = productClient.getProductById(productIdx);

        Optional<CartEntity> existingCart = cartJpaRepository.findByUserIdxAndProductIdxAndOptionIdx(userId, productIdx, optionIdx);

        if (existingCart.isPresent()) {
            CartEntity entity = existingCart.get();
            entity.addProductToCart();
            entity.addPrice(productInfo.price());
            cartJpaRepository.save(entity);
        } else {
            CartEntity newCart = new CartEntity();
            newCart.setUserIdx(userId);
            newCart.setProductIdx(productIdx);
            newCart.setOptionIdx(optionIdx);
            newCart.setCount(1);
            newCart.setPrice(productInfo.price());
            newCart.setDel(false);
            cartJpaRepository.save(newCart);
        }

        return getCart(token);
    }

    @Override
    public CartInfo getCart(String token) {
        Long userId = userClient.getUserId(token);
        List<CartEntity> cartList = cartJpaRepository.findAllByUserIdx(userId);

        List<ProductInfo> productInfos = cartList.stream()
                .map(cart -> productClient.getProductById(cart.getProductIdx()))
                .toList();

        return CartMapper.toInfo(cartList, productInfos);
    }
}
