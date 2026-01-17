package co.kr.order.service.impl;

import co.kr.order.client.ProductClient;
import co.kr.order.exception.CartNotFoundException;
import co.kr.order.exception.ErrorCode;
import co.kr.order.exception.ProductNotFoundException;
import co.kr.order.mapper.CartMapper;
import co.kr.order.model.dto.ItemInfo;
import co.kr.order.model.dto.ProductInfo;
import co.kr.order.model.dto.request.ProductRequest;
import co.kr.order.model.dto.response.CartItemResponse;
import co.kr.order.model.dto.response.CartResponse;
import co.kr.order.model.entity.CartEntity;
import co.kr.order.repository.CartJpaRepository;
import co.kr.order.service.CartService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartJpaRepository cartRepository;
    private final ProductClient productClient;

    /*
     * @param request : productIdx와 optionIdx
     * 장바구니에 단일상품 추가 (장바구니 페이지에서 상품 + 클릭)
     */
    @Override
    @Transactional
    public CartItemResponse addCartItem(Long userIdx, ProductRequest request) {

        // Product 서비스에 feignClient(동기통신) 으로 제품 정보 가져옴
        ProductInfo productInfo;
        try {
            productInfo = productClient.getProduct(request.productIdx(), request.optionIdx());
        } catch (FeignException.NotFound e) {
            throw new ProductNotFoundException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        // userIdx, productIdx, optionIdx가 같은 컬럼 찾기
        Optional<CartEntity> existCart = cartRepository.findByUserIdxAndProductIdxAndOptionIdx(userIdx, request.productIdx(), request.optionIdx());

        if (existCart.isPresent()) {
            // 장바구니에서 상품을 + 했을 경우 (existCart가 존재할 경우)
            // entity의 개수(quantity) 증가
            CartEntity entity = existCart.get();
            entity.plusQuantity();
            entity.addPrice(productInfo.price());

            cartRepository.save(entity);
        }
        else {
            // 상품에서 직접 카트 담기 눌렀을 경우 (existCart가 없을 경우)
            // 새로운 entity 생성 후 데이터 추가
            CartEntity newCart = CartEntity.builder()
                    .userIdx(userIdx)
                    .productIdx(request.productIdx())
                    .optionIdx(request.optionIdx())
                    .quantity(1)
                    .price(productInfo.price())
                    .del(false)
                    .build();

            cartRepository.save(newCart);
        }

        // 단일상품 조회
        return getCartItem(userIdx, request);
    }

    /*
     * @param request : productIdx와 optionIdx
     * 장바구니 페이지에서 상품 - 클릭
     */
    @Override
    @Transactional
    public CartItemResponse subtractCartItem(Long userIdx, ProductRequest request) {

        // Product 서비스에 feignClient(동기통신) 으로 제품 정보 가져옴
        ProductInfo productInfo;
        try {
            productInfo = productClient.getProduct(request.productIdx(), request.optionIdx());
        } catch (FeignException.NotFound e) {
            throw new ProductNotFoundException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        Optional<CartEntity> existingCart = cartRepository.findByUserIdxAndProductIdxAndOptionIdx(userIdx, request.productIdx(), request.optionIdx());

        if (existingCart.isPresent()) {
            // 장바구니에서 상품을 - 했을 경우 (existCart가 존재할 경우)
            // entity의 개수(quantity) 감소
            CartEntity entity = existingCart.get();

            // 개수(quantity)가 1 이상이면 동작하고
            if(entity.getQuantity() > 1) {
                entity.minusQuantity();
                entity.subtractPrice(productInfo.price());
                cartRepository.save(entity);
            }
            else {
                // 1 이하이면
                // 어차피 front에서 처리할거지만 혹시모르니 삭제 처리
                cartRepository.delete(entity);
            }
        }
        else {
            // existCart가 없는데 - 를 누를 수 없으니 CartNotFoundException 으로 처리
            throw new CartNotFoundException(ErrorCode.CART_NOT_FOUND);
        }

        // 단일상품 조회
        return getCartItem(userIdx, request);
    }

    /*
     * 카트 전체 리스트 조회
     */
    @Override
    @Transactional(readOnly = true)
    public CartResponse getCartList(Long userIdx) {

        // useridx가 같은 entity 가져오기
        List<CartEntity> entities = cartRepository.findAllByUserIdx(userIdx);
        List<CartItemResponse> cartList = new ArrayList<>();

        // 장바구니가 비어있을 경우 바로 빈리스트 return
        if (entities.isEmpty()) {
            return CartMapper.toCartInfo(new ArrayList<>());
        }

        List<ProductRequest> productList = new ArrayList<>();
        for (CartEntity entity : entities) {
            ProductRequest product = new ProductRequest(entity.getProductIdx(), entity.getOptionIdx());
            productList.add(product);
        }

        // ProductInfo(상품 정보)를 가져오기 위한 Product-Service간의 동기 통신
        List<ProductInfo> productInfos;
        try {
            productInfos = productClient.getProductList(productList);
        } catch (FeignException.NotFound e) {
            throw new ProductNotFoundException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        // 중복 방지를 위한 Map의 Key를 String(복합키)로
        Map<String, ProductInfo> productMap = new HashMap<>();
        for (ProductInfo info : productInfos) {
            // key 생성 예: "10-1" (상품ID-옵션 내용)
            String key = info.productIdx() + "-" + info.optionIdx();
            productMap.put(key, info);
        }

        for (CartEntity entity : entities) {

            String key = entity.getProductIdx() + "-" + entity.getOptionIdx();
            ProductInfo product = productMap.get(key);

            if (product == null) {
                continue;
            }

            // 상품 가격 = 단가 * 수량
            int quantity = entity.getQuantity();
            BigDecimal totalPrice = product.price().multiply(BigDecimal.valueOf(quantity));

            // 장바구니 아이템 Dto 생성
            CartItemResponse cartItem = new CartItemResponse(
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

            // 단일상품 리스트에 추가
            cartList.add(cartItem);
        }

        // 응답 dto return
        return CartMapper.toCartInfo(cartList);
    }

    /*
     * @param request : 조회할 상품의 productIdx와 optionIdx
     * 장바구니에 담긴 단일 상품 상세 조회 (수량 변경 후 응답값 반환용)
     */
    @Override
    @Transactional(readOnly = true)
    public CartItemResponse getCartItem(Long userIdx, ProductRequest request) {

        // 해당 유저의 장바구니에서 특정 상품(옵션 포함) 찾기, 없으면 예외 발생
        CartEntity entity = cartRepository.findByUserIdxAndProductIdxAndOptionIdx(userIdx, request.productIdx(), request.optionIdx()).orElseThrow(() -> new CartNotFoundException(ErrorCode.CART_NOT_FOUND));

        // Product 서비스에 feignClient(동기통신) 으로 제품 정보 가져옴
        ProductInfo product;
        try {
            product = productClient.getProduct(request.productIdx(), request.optionIdx());
        } catch (FeignException.NotFound e) {
            throw new ProductNotFoundException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        // 상품 가격 = 단가 * 수량
        int quantity = entity.getQuantity();
        BigDecimal totalPrice = product.price().multiply(BigDecimal.valueOf(quantity));

        // 응답 DTO 반환
        return new CartItemResponse(
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
    }

    /*
     * @param productRequest : 삭제할 상품의 productIdx와 optionIdx
     * 장바구니 상품 아예 삭제 (X 클릭 또는 수량 0 미만 처리)
     */
    @Override
    @Transactional
    public void deleteCartItem(Long userIdx, ProductRequest productRequest) {

        // 삭제할 장바구니 아이템 조회
        Optional<CartEntity> existingCart = cartRepository.findByUserIdxAndProductIdxAndOptionIdx(userIdx, productRequest.productIdx(), productRequest.optionIdx());

        if (existingCart.isPresent()) {
            // 상품이 존재하면 DB 데이터 삭제
            CartEntity entity = existingCart.get();
            cartRepository.deleteById(entity.getId());
        }
        else {
            // 이미 삭제되었거나 없는 상품에 대한 삭제 요청 시 예외 처리
            throw new CartNotFoundException(ErrorCode.CART_NOT_FOUND);
        }
    }
}
