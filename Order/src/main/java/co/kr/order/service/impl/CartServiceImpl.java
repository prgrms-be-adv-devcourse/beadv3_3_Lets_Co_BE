package co.kr.order.service.impl;

import co.kr.order.client.ProductClient;
import co.kr.order.exception.CartNotFoundException;
import co.kr.order.exception.ErrorCode;
import co.kr.order.exception.ProductNotFoundException;
import co.kr.order.model.dto.ItemInfo;
import co.kr.order.model.dto.ProductInfo;
import co.kr.order.model.dto.response.CartItemRes;
import co.kr.order.model.dto.response.ClientProductRes;
import co.kr.order.model.redis.Cart;
import co.kr.order.service.CartService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ProductClient productClient;

    private final static String KEY = "cart:user:";

    @Override
    public CartItemRes addCartItem(Long userIdx, ProductInfo request) {

        ClientProductRes productResponse;
        try {
            productResponse = productClient.getProduct(request.productCode(), request.optionCode());
        } catch (FeignException.NotFound e) {
            throw new ProductNotFoundException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        String cartKey = KEY + userIdx;
        String field = request.optionCode();

        Cart cartItem = Cart.builder()
                .userIdx(userIdx)
                .productIdx(productResponse.productIdx())
                .optionIdx(productResponse.optionIdx())
                .productCode(request.productCode())
                .optionCode(request.optionCode())
                .productName(productResponse.productName())
                .optionContent(productResponse.optionContent())
                .price(productResponse.price())
                .quantity(request.quantity())
                .build();

        redisTemplate.opsForHash().put(cartKey, field, cartItem);
        redisTemplate.expire(cartKey, 30, TimeUnit.DAYS);

        return new CartItemRes(
                new ItemInfo(
                        request.productCode(),
                        request.optionCode(),
                        productResponse.productName(),
                        productResponse.optionContent(),
                        productResponse.price()
                ),
                cartItem.getQuantity(),
                productResponse.price()
                        .multiply(BigDecimal.valueOf(cartItem.getQuantity()))
        );
    }

    @Override
    public CartItemRes plusCartItem(Long userIdx, String optionCode) {

        String cartKey = KEY + userIdx;

        Cart cartItem = getCartItem(cartKey, optionCode);
        if (cartItem == null) {
            throw new CartNotFoundException(ErrorCode.CART_NOT_FOUND);
        }

        if (cartItem.getQuantity() < 100) {
            cartItem.plusQuantity();
            redisTemplate.opsForHash().put(cartKey, optionCode, cartItem);
            redisTemplate.expire(cartKey, 30, TimeUnit.DAYS);
        }

        return new CartItemRes(
                new ItemInfo(
                        cartItem.getProductCode(),
                        cartItem.getOptionCode(),
                        cartItem.getProductName(),
                        cartItem.getOptionContent(),
                        cartItem.getPrice()
                ),
                cartItem.getQuantity(),
                cartItem.getPrice()
                        .multiply(BigDecimal.valueOf(cartItem.getQuantity()))
        );
    }

    @Override
    public CartItemRes minusCartItem(Long userIdx, String optionCode) {

        String cartKey = KEY + userIdx;

        Cart cartItem = getCartItem(cartKey, optionCode);
        if (cartItem == null) {
            throw new CartNotFoundException(ErrorCode.CART_NOT_FOUND);
        }

        if (cartItem.getQuantity() > 1) {
            cartItem.minusQuantity();
            redisTemplate.opsForHash().put(cartKey, optionCode, cartItem);
            redisTemplate.expire(cartKey, 30, TimeUnit.DAYS);
        }

        return new CartItemRes(
                new ItemInfo(
                        cartItem.getProductCode(),
                        cartItem.getOptionCode(),
                        cartItem.getProductName(),
                        cartItem.getOptionContent(),
                        cartItem.getPrice()
                ),
                cartItem.getQuantity(),
                cartItem.getPrice()
                        .multiply(BigDecimal.valueOf(cartItem.getQuantity()))
        );
    }

    @Override
    public void deleteCartItem(Long userIdx, String optionCode) {

        String cartKey = KEY + userIdx;

        redisTemplate.opsForHash().delete(cartKey, optionCode);
        redisTemplate.expire(cartKey, 30, TimeUnit.DAYS);
    }

    @Override
    public void deleteCartAll(Long userIdx) {

        String cartKey = KEY + userIdx;

        redisTemplate.delete(cartKey);
    }

    @Override
    public List<CartItemRes> getCartList(Long userIdx) {

        String cartKey = KEY + userIdx;
        List<Object> values = redisTemplate.opsForHash().values(cartKey);

        if (values.isEmpty()) {
            return List.of();
        }

        List<CartItemRes> cartList = new ArrayList<>();
        for (Object value : values) {
            Cart cartItem = (Cart) value;

            CartItemRes cartItemResponse = new CartItemRes(
                    new ItemInfo(
                            cartItem.getProductCode(),
                            cartItem.getOptionCode(),
                            cartItem.getProductName(),
                            cartItem.getOptionContent(),
                            cartItem.getPrice()
                    ),
                    cartItem.getQuantity(),
                    cartItem.getPrice()
                            .multiply(BigDecimal.valueOf(cartItem.getQuantity()))
            );
            cartList.add(cartItemResponse);
        }

        return cartList;
    }

    // Helper Method
    private Cart getCartItem(String cartKey, String optionCode) {
        Object cartItem = redisTemplate.opsForHash().get(cartKey, optionCode);
        return (Cart) cartItem;
    }
}
