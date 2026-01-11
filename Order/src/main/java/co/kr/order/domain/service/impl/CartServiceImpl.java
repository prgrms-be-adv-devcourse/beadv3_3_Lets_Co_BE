package co.kr.order.domain.service.impl;

import co.kr.order.domain.client.ProductClient;
import co.kr.order.domain.client.UserClient;
import co.kr.order.domain.mapper.CartMapper;
import co.kr.order.domain.model.dto.CartInfo;
import co.kr.order.domain.model.dto.CartItemInfo;
import co.kr.order.domain.model.dto.ProductInfo;
import co.kr.order.domain.model.entity.CartEntity;
import co.kr.order.domain.repository.CartJpaRepository;
import co.kr.order.domain.service.CartService;
import co.kr.order.global.exception.CartNotFoundException;
import co.kr.order.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
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
    public CartInfo addCartItem(String token, Long productIdx, Long optionIdx) {
        Long userId = userClient.getUserIdx(token);

        ProductInfo productInfo = productClient.getProduct(productIdx, optionIdx);
        Optional<CartEntity> existingCart = cartJpaRepository.findByUserIdxAndProductIdxAndOptionIdx(userId, productIdx, optionIdx);


        if (existingCart.isPresent()) {
            // 장바구니에서 상품을 + 했을 경우
            CartEntity entity = existingCart.get();
            entity.plusQuantity();
            entity.addPrice(productInfo.price());

            cartJpaRepository.save(entity);
        }
        else {
            // 상품에서 직접 카트 담기 눌렀을 경우
            CartEntity newCart = new CartEntity();

            newCart.setUserIdx(userId);
            newCart.setProductIdx(productIdx);
            newCart.setOptionIdx(optionIdx);
            newCart.setQuantity(1);
            newCart.setPrice(productInfo.price());
            newCart.setDel(false);

            cartJpaRepository.save(newCart);
        }

        return getCart(token);
    }

    @Override
    @Transactional
    public CartInfo subtractCartItem(String token, Long productIdx, Long optionIdx) {
        Long userId = userClient.getUserIdx(token);

        ProductInfo productInfo = productClient.getProduct(productIdx, optionIdx);
        Optional<CartEntity> existingCart = cartJpaRepository.findByUserIdxAndProductIdxAndOptionIdx(userId, productIdx, optionIdx);

        if (existingCart.isPresent()) {
            CartEntity entity = existingCart.get();

            if(entity.getQuantity() > 1) {
                entity.minusQuantity();
                entity.subtractPrice(productInfo.price());
                cartJpaRepository.save(entity);
            }
            else {
                // 어차피 front에서 1 이하로 안내려가게 처리할거지만 혹시모르니 삭제 처리
                cartJpaRepository.delete(entity);
            }
        }
        else {
            throw new CartNotFoundException(ErrorCode.CART_NOT_FOUND);
        }

        return getCart(token);
    }

    @Override
    @Transactional(readOnly = true)
    public CartInfo getCart(String token) {
        Long userIdx = userClient.getUserIdx(token);
        List<CartEntity> cartList = cartJpaRepository.findAllByUserIdx(userIdx);

        List<CartItemInfo> cartItemInfos = new ArrayList<>();
        for (CartEntity cart : cartList) {
            ProductInfo getInfo = productClient.getProduct(cart.getProductIdx(), cart.getOptionIdx());

            // 상품 가격 = 단가 * 수량
            int quantity = cart.getQuantity();
            BigDecimal totalPrice = getInfo.price().multiply(BigDecimal.valueOf(quantity));

            CartItemInfo cartItemInfo = new CartItemInfo(
                    getInfo.productIdx(),
                    getInfo.productName(),
                    getInfo.optionContent(),
                    getInfo.price(),
                    quantity,
                    totalPrice
            );
            cartItemInfos.add(cartItemInfo);
        }

        return CartMapper.toCartInfo(cartItemInfos);
    }

    // 장바구니에 있는 품목에 x를 눌렀을때 cart 테이블의 내용을 소프트웨어적 삭제를 할 필요가 있음? 
    // 장바구니 기록을 따로 가지고 있을 필요는 없다고 생각함
    @Override
    @Transactional
    public void deleteCartItem(String token, Long productIdx, Long optionIdx) {
        Long userId = userClient.getUserIdx(token);

        Optional<CartEntity> existingCart = cartJpaRepository.findByUserIdxAndProductIdxAndOptionIdx(userId, productIdx, optionIdx);

        if (existingCart.isPresent()) {
            CartEntity entity = existingCart.get();
            cartJpaRepository.deleteById(entity.getId());
        }
        else {
            throw new CartNotFoundException(ErrorCode.CART_NOT_FOUND);
        }
    }
}
