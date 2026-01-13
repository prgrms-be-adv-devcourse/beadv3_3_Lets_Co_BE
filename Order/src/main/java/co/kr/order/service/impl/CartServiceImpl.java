package co.kr.order.service.impl;

import co.kr.order.client.ProductClient;
import co.kr.order.client.UserClient;
import co.kr.order.exception.CartNotFoundException;
import co.kr.order.exception.ErrorCode;
import co.kr.order.mapper.CartMapper;
import co.kr.order.model.dto.ProductInfo;
import co.kr.order.model.dto.request.CartRequest;
import co.kr.order.model.dto.response.CartItemResponse;
import co.kr.order.model.dto.response.CartResponse;
import co.kr.order.model.entity.CartEntity;
import co.kr.order.repository.CartJpaRepository;
import co.kr.order.service.CartService;
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

    private final CartJpaRepository cartRepository;
    private final ProductClient productClient;
    private final UserClient userClient;

    @Override
    @Transactional
    public CartItemResponse addCartItem(String token, CartRequest cartRequest) {
        Long userIdx = userClient.getUserIdx(token);

        ProductInfo productInfo = productClient.getProduct(cartRequest.productIdx(), cartRequest.optionIdx());
        Optional<CartEntity> existingCart = cartRepository.findByUserIdxAndProductIdxAndOptionIdx(userIdx, cartRequest.productIdx(), cartRequest.optionIdx());

        if (existingCart.isPresent()) {
            // 장바구니에서 상품을 + 했을 경우
            CartEntity entity = existingCart.get();
            entity.plusQuantity();
            entity.addPrice(productInfo.price());

            cartRepository.save(entity);
        }
        else {
            // 상품에서 직접 카트 담기 눌렀을 경우
            CartEntity newCart = new CartEntity();

            newCart.setUserIdx(userIdx);
            newCart.setProductIdx(cartRequest.productIdx());
            newCart.setOptionIdx(cartRequest.optionIdx());
            newCart.setQuantity(1);
            newCart.setPrice(productInfo.price());
            newCart.setDel(false);

            cartRepository.save(newCart);
        }

        return getCartItem(token, cartRequest);
    }

    @Override
    @Transactional
    public CartItemResponse subtractCartItem(String token, CartRequest cartRequest) {
        Long userId = userClient.getUserIdx(token);

        ProductInfo productInfo = productClient.getProduct(cartRequest.productIdx(), cartRequest.optionIdx());
        Optional<CartEntity> existingCart = cartRepository.findByUserIdxAndProductIdxAndOptionIdx(userId, cartRequest.productIdx(), cartRequest.optionIdx());

        if (existingCart.isPresent()) {
            CartEntity entity = existingCart.get();

            if(entity.getQuantity() > 1) {
                entity.minusQuantity();
                entity.subtractPrice(productInfo.price());
                cartRepository.save(entity);
            }
            else {
                // 어차피 front에서 1 이하로 안내려가게 처리할거지만 혹시모르니 삭제 처리
                cartRepository.delete(entity);
            }
        }
        else {
            throw new CartNotFoundException(ErrorCode.CART_NOT_FOUND);
        }

        return getCartItem(token, cartRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public CartResponse getCartList(String token) {
        Long userIdx = userClient.getUserIdx(token);
        List<CartEntity> entities = cartRepository.findAllByUserIdx(userIdx);
        List<CartItemResponse> cartList = new ArrayList<>();

        // todo N+1 문제 발생. 이거 나중에 안되도록 코드 수정해야함 (Order 기능 개발하고)
        for (CartEntity entity : entities) {
            ProductInfo productInfo = productClient.getProduct(entity.getProductIdx(), entity.getOptionIdx());

            // 상품 가격 = 단가 * 수량
            int quantity = entity.getQuantity();
            BigDecimal totalPrice = productInfo.price().multiply(BigDecimal.valueOf(quantity));

            CartItemResponse cartItem = new CartItemResponse(
                    productInfo,
                    quantity,
                    totalPrice
            );
            cartList.add(cartItem);
        }

        return CartMapper.toCartInfo(cartList);
    }

    @Override
    public CartItemResponse getCartItem(String token, CartRequest request) {

        Long userIdx = userClient.getUserIdx(token);
        CartEntity entity = cartRepository.findByUserIdxAndProductIdxAndOptionIdx(userIdx, request.productIdx(), request.optionIdx()).orElseThrow(() -> new CartNotFoundException(ErrorCode.CART_NOT_FOUND));

        ProductInfo productInfo = productClient.getProduct(entity.getProductIdx(), entity.getOptionIdx());

        // 상품 가격 = 단가 * 수량
        int quantity = entity.getQuantity();
        BigDecimal totalPrice = productInfo.price().multiply(BigDecimal.valueOf(quantity));

        return new CartItemResponse(
                productInfo,
                quantity,
                totalPrice
        );
    }

    @Override
    @Transactional
    public void deleteCartItem(String token, CartRequest cartRequest) {
        Long userId = userClient.getUserIdx(token);

        Optional<CartEntity> existingCart = cartRepository.findByUserIdxAndProductIdxAndOptionIdx(userId, cartRequest.productIdx(), cartRequest.optionIdx());

        if (existingCart.isPresent()) {
            CartEntity entity = existingCart.get();
            cartRepository.deleteById(entity.getId());
        }
        else {
            throw new CartNotFoundException(ErrorCode.CART_NOT_FOUND);
        }
    }
}
