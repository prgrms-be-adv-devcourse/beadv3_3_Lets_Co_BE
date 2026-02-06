package co.kr.order.service.impl;

import co.kr.order.client.ProductClient;
import co.kr.order.exception.CartNotFoundException;
import co.kr.order.exception.ErrorCode;
import co.kr.order.exception.ProductNotFoundException;
import co.kr.order.model.dto.ItemInfo;
import co.kr.order.model.dto.request.CartReq;
import co.kr.order.model.dto.request.ClientProductReq;
import co.kr.order.model.dto.response.CartItemRes;
import co.kr.order.model.dto.response.ClientProductRes;
import co.kr.order.model.redis.Cart;
import co.kr.order.service.CartService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * todo.
 * DB에 Cart 테이블 전부 삭제 예정 (Redis로 처리)
 * 로직 전부 바뀔 예정
 */
@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ProductClient productClient;

    private final static String KEY = "cart:user:";

    @Override
    public CartItemRes addCartItem(Long userIdx, CartReq request) {

        ClientProductRes productResponse;
        try {
            productResponse = productClient.getProduct(request.productCode(), request.optionCode());
        } catch (FeignException.NotFound e) {
            throw new ProductNotFoundException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        String cartKey = KEY + userIdx;
        String field = request.optionCode();

        Cart cartItem = getCartItem(cartKey, field);
        if (cartItem == null) {
            cartItem = Cart.builder()
                .userIdx(userIdx)
                .productIdx(productResponse.productIdx())
                .optionIdx(productResponse.optionIdx())
                .quantity(1)
                .build();
        }
        else {
            cartItem.increaseQuantity();
        }
        redisTemplate.opsForHash().put(cartKey, field, cartItem);
        redisTemplate.expire(cartKey, 30, TimeUnit.DAYS);

        return new CartItemRes(
                new ItemInfo(
                        productResponse.productIdx(),
                        productResponse.optionIdx(),
                        productResponse.productName(),
                        productResponse.optionName(),
                        productResponse.price()
                ),
                cartItem.getQuantity(),
                productResponse.price()
                        .multiply(BigDecimal.valueOf(cartItem.getQuantity()))
        );
    }

    @Override
    public CartItemRes subtractCartItem(Long userIdx, CartReq request) {

        String cartKey = KEY + userIdx;
        String field = request.optionCode();

        Cart cartItem = getCartItem(cartKey, field);
        if (cartItem == null) {
            throw new CartNotFoundException(ErrorCode.CART_NOT_FOUND);
        }

        if (cartItem.getQuantity() > 1) {
            cartItem.decreaseQuantity();
            redisTemplate.opsForHash().put(cartKey, field, cartItem);
            redisTemplate.expire(cartKey, 30, TimeUnit.DAYS);
        }

        ClientProductRes productResponse;
        try {
            productResponse = productClient.getProduct(request.productCode(), request.optionCode());
        } catch (FeignException.NotFound e) {
            throw new ProductNotFoundException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        return new CartItemRes(
                new ItemInfo(
                        productResponse.productIdx(),
                        productResponse.optionIdx(),
                        productResponse.productName(),
                        productResponse.optionName(),
                        productResponse.price()
                ),
                cartItem.getQuantity(),
                productResponse.price()
                        .multiply(BigDecimal.valueOf(cartItem.getQuantity()))
        );
    }

    @Override
    public void deleteCartItem(Long userIdx, CartReq request) {

        String cartKey = KEY + userIdx;
        String field = request.optionCode();

        redisTemplate.opsForHash().delete(cartKey, field);
        redisTemplate.expire(cartKey, 30, TimeUnit.DAYS);
    }

    @Override
    public void deleteCartAll(Long userIdx) {

        String cartKey = KEY + userIdx;

        redisTemplate.delete(cartKey);
    }

    @Override
    public List<CartItemRes> getCartList(Long userIdx) {

        List<CartItemRes> cartIList = new ArrayList<>();

        String cartKey = KEY + userIdx;
        List<Object> values = redisTemplate.opsForHash().values(cartKey);

        if (values.isEmpty()) {
            return List.of();
        }

        List<ClientProductReq> productList = new ArrayList<>();
        for (Object value : values) {
            Cart cartItem = (Cart) value;
            ClientProductReq product = new ClientProductReq(cartItem.getProductIdx(), cartItem.getOptionIdx());
            productList.add(product);
        }

        List<ClientProductRes> clientProductResponse;
        try {
            clientProductResponse = productClient.getProductList(productList);
        } catch (FeignException.NotFound e) {
            throw new ProductNotFoundException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        // 중복 방지를 위한 Map의 Key를 String(복합키)로
        Map<String, ClientProductRes> productMap = new HashMap<>();
        for (ClientProductRes info : clientProductResponse) {
            // key 생성 예: "10-1" (상품ID-옵션 내용)
            String key = info.productIdx() + "-" + info.optionIdx();
            productMap.put(key, info);
        }

        for (Object value : values) {
            Cart cartItem = (Cart) value;

            String key = cartItem.getProductIdx() + "-" + cartItem.getOptionIdx();
            ClientProductRes product = productMap.get(key);

            if (product == null) {
                continue;
            }

            // 상품 가격 = 단가 * 수량
            int quantity = cartItem.getQuantity();
            BigDecimal totalPrice = product.price().multiply(BigDecimal.valueOf(quantity));

            CartItemRes cartItemResponse = new CartItemRes(
                    new ItemInfo(
                            product.productIdx(),
                            product.optionIdx(),
                            product.productName(),
                            product.optionName(),
                            product.price()
                    ),
                    quantity,
                    totalPrice
            );

            cartIList.add((cartItemResponse));
        }

        return cartIList;
    }

    // Helper Method
    private Cart getCartItem(String cartKey, String field) {
        Object cartItem = redisTemplate.opsForHash().get(cartKey, field);
        return (Cart) cartItem;
    }
}
