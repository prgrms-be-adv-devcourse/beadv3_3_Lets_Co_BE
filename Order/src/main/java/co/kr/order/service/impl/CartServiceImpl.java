package co.kr.order.service.impl;

import co.kr.order.client.ProductClient;
import co.kr.order.exception.CartNotFoundException;
import co.kr.order.exception.ErrorCode;
import co.kr.order.exception.ProductNotFoundException;
import co.kr.order.model.dto.ItemInfo;
import co.kr.order.model.dto.ProductInfo;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ProductClient productClient;

    // Redis의 Key Default 값
    private final static String KEY = "cart:user:";

    /*
     * 장바구니에 상품 추가
     * @param userIdx: 유저 인덱스
     * @param request: 상품 정보
     */
    @Override
    public CartItemRes addCartItem(Long userIdx, ProductInfo request) {

        // 상품 정보 가져오기 (FeignClient)
        ClientProductRes productResponse;
        try {
            productResponse = productClient.getProduct(request.productCode(), request.optionCode());
        } catch (FeignException.NotFound e) {
            // 없을 경우 ProductNotFoundException
            throw new ProductNotFoundException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        // 장바구니 Key, Field 세팅
        String cartKey = KEY + userIdx;
        String field = request.optionCode();

        // Redis에 저장 할 장바구니 정보 세팅
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

        // put(Key, Field, Data) - Redis에 장바구니 데이터 저장
        redisTemplate.opsForHash().put(cartKey, field, cartItem);
        // TTL 설정 (30일)
        redisTemplate.expire(cartKey, 30, TimeUnit.DAYS);

        // 장바구니에 저장한 상품 정보 return
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

    /*
     * 장바구니 상품개수 +1
     * @param userIdx: 유저 인덱스
     * @param optionCode: 상품 옵션 코드
     */
    @Override
    public CartItemRes plusCartItem(Long userIdx, String optionCode) {

        // 장바구니 Key 세팅
        String cartKey = KEY + userIdx;

        // Key로 장바구니 조회
        Cart cartItem = getCartItem(cartKey, optionCode);
        if (cartItem == null) {
            // 없을 경우 CartNotFoundException
            throw new CartNotFoundException(ErrorCode.CART_NOT_FOUND);
        }

        // 장바구니 최대 개수 100으로 지정
        if (cartItem.getQuantity() < 100) {
            cartItem.plusQuantity();  // 장바구니 상품 +1
            redisTemplate.opsForHash().put(cartKey, optionCode, cartItem);  // 저장
            redisTemplate.expire(cartKey, 30, TimeUnit.DAYS);  // TTL 설정 (30일)
        }

        // 장바구니 상품 정보 return
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

    /*
     * 장바구니 상품개수 -1
     * @param userIdx: 유저 인덱스
     * @param optionCode: 상품 옵션 코드
     */
    @Override
    public CartItemRes minusCartItem(Long userIdx, String optionCode) {

        // 장바구니 Key 세팅
        String cartKey = KEY + userIdx;

        // Key로 장바구니 조회
        Cart cartItem = getCartItem(cartKey, optionCode);
        if (cartItem == null) {
            // 없을 경우 CartNotFoundException
            throw new CartNotFoundException(ErrorCode.CART_NOT_FOUND);
        }

        // 1개 이하로 감소할 수 없음
        if (cartItem.getQuantity() > 1) {
            cartItem.minusQuantity();  // 장바구니 상품 -1
            redisTemplate.opsForHash().put(cartKey, optionCode, cartItem);  // 저장
            redisTemplate.expire(cartKey, 30, TimeUnit.DAYS);  // TTL 설정 (30일)
        }

        // 장바구니 상품 정보 return
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

    /*
     * 장바구니 아이템 삭제
     * @param userIdx: 유저 인덱스
     * @param optionCode: 상품 옵션 코드
     */
    @Override
    public void deleteCartItem(Long userIdx, String optionCode) {

        // 장바구니 Key 세팅
        String cartKey = KEY + userIdx;

        redisTemplate.opsForHash().delete(cartKey, optionCode);  // 저장
        redisTemplate.expire(cartKey, 30, TimeUnit.DAYS);  // TTL 설정(30일)
    }

    /*
     * 장바구니 상품 전체 삭제 (결제완료)
     * @param userIdx: 유저 인덱스
     */
    @Override
    public void deleteCartAll(Long userIdx) {

        // 장바구니 Key 세팅
        String cartKey = KEY + userIdx;

        redisTemplate.delete(cartKey);  // 삭제
    }

    /*
     * 장바구니 상품 리스트 조회
     * @param userIdx: 유저 인덱스
     */
    @Override
    public List<CartItemRes> getCartList(Long userIdx) {

        // 장바구니 Key 세팅
        String cartKey = KEY + userIdx;
        // 장바구니 정보 가져오기 (전부)
        List<Object> values = redisTemplate.opsForHash().values(cartKey);

        // 비어있으면 빈 list로 return
        if (values.isEmpty()) {
            return List.of();
        }

        // 장바구니 상품 리스트 정보 세팅
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

        // 장바구니 상품 리스트 return
        return cartList;
    }

    // FeignClient를 위한 상품 요청 정보 DTO 세팅
    @Override
    public List<ClientProductReq> getProductByCart(Long userIdx) {

        // 장바구니 Key 세팅
        String cartKey = KEY + userIdx;
        // 장바구니 정보 가져오기 (전부)
        List<Object> values = redisTemplate.opsForHash().values(cartKey);

        // 비어있으면 ProductNotFoundException
        if (values.isEmpty()) {
            throw new ProductNotFoundException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        // 반복문 순회하면서 ClientProductReq의 getProductIdx, getOptionIdx 값 세팅
        List<ClientProductReq> products = new ArrayList<>();
        for (Object value : values) {
            Cart cart = (Cart) value;

            // 실제 상품 데이터를(최신화 된) 가져옴
            ClientProductReq product = new ClientProductReq(
                    cart.getProductCode(),
                    cart.getOptionCode()
            );

            products.add(product);
        }

        // ClientProductReq 리스트 return
        return products;
    }

    /*
     * 장바구니 상품에 맞는 개수 가져오기
     * @param userIdx: 유저 인덱스
     */
    @Override
    public Map<Long, Integer> getCartItemQuantities(Long userIdx) {

        // 장바구니 Key 세팅
        String cartKey = KEY + userIdx;
        // 장바구니 정보 가져오기 (전부)
        List<Object> values = redisTemplate.opsForHash().values(cartKey);


        // 비어있으면 ProductNotFoundException
        if (values.isEmpty()) {
            throw new ProductNotFoundException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        // stream 돌면서 optionIdx와 quantity를 Map으로 세팅하고 return
        // Map(key:optionIdx, value:quantity)
        return values.stream()
                .map(value -> (Cart) value)
                .collect(Collectors.toMap(
                        Cart::getOptionIdx,
                        Cart::getQuantity
                ));
    }

    /*
     * Helper Method
     * 장바구니 특정 상품하나 가져오기
     * @param cartKey: 장바구니 Key
     * @param optionCode: 옵션 코드
     */
    private Cart getCartItem(String cartKey, String optionCode) {
        Object cartItem = redisTemplate.opsForHash().get(cartKey, optionCode);
        return (Cart) cartItem;
    }
}
