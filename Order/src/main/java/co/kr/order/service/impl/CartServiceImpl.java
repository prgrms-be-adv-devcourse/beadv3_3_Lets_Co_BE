package co.kr.order.service.impl;

import co.kr.order.client.ProductClient;
import co.kr.order.client.UserClient;
import co.kr.order.exception.CartNotFoundException;
import co.kr.order.exception.ErrorCode;
import co.kr.order.mapper.CartMapper;
import co.kr.order.model.dto.ProductInfo;
import co.kr.order.model.dto.request.ProductRequest;
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

    /*
     * @param token : jwt 토큰
     * @param request : productIdx와 optionIdx
     * 장바구니에 단일상품 추가 (장바구니 페이지에서 상품 + 클릭)
     */
    @Override
    @Transactional
    public CartItemResponse addCartItem(String token, ProductRequest request) {

        // token으로 userIdx를 가져오기 User-Service 간의 동기통신
        Long userIdx = userClient.getUserIdx(token);

        // ProductInfo(상품 정보)를 가져오기 위한 Product-Service간의 동기 통신
        ProductInfo productInfo = productClient.getProduct(request.productIdx(), request.optionIdx());

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
        return getCartItem(token, request);
    }

    /*
     * @param token : jwt 토큰
     * @param request : productIdx와 optionIdx
     * 장바구니 페이지에서 상품 - 클릭
     */
    @Override
    @Transactional
    public CartItemResponse subtractCartItem(String token, ProductRequest request) {

        // addCartItem와 로직 동일
        Long userId = userClient.getUserIdx(token);

        ProductInfo productInfo = productClient.getProduct(request.productIdx(), request.optionIdx());
        Optional<CartEntity> existingCart = cartRepository.findByUserIdxAndProductIdxAndOptionIdx(userId, request.productIdx(), request.optionIdx());

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
        return getCartItem(token, request);
    }

    /*
     * @param token : jwt 토큰
     * 카트 전체 리스트 조회
     */
    @Override
    @Transactional(readOnly = true)
    public CartResponse getCartList(String token) {
        
        /*
         * 위의 로직들처럼 ProductRequest를 받아서 productIdx, optionIdx를 가져올지
         * token만 받아서 userIdx를 가져오고 findByIdx로 productIdx, optionIdx 찾을지 
         * 위 두 방법중 하나 선택
         */

        // 일단 token만 받아서 findByIdx로 productIdx, optionIdx 데이터를 가져오는 걸로

        // token으로 userIdx를 가져오기 User-Service 간의 동기통신
        Long userIdx = userClient.getUserIdx(token);

        // useridx가 같은 entity 가져오기
        List<CartEntity> entities = cartRepository.findAllByUserIdx(userIdx);
        List<CartItemResponse> cartList = new ArrayList<>();

        // todo N+1 문제 발생. 성능개선은 나중에
        for (CartEntity entity : entities) {
            // ProductInfo(상품 정보)를 가져오기 위한 Product-Service간의 동기 통신
            ProductInfo productInfo = productClient.getProduct(entity.getProductIdx(), entity.getOptionIdx());

            // 상품 가격 = 단가 * 수량
            int quantity = entity.getQuantity();
            BigDecimal totalPrice = productInfo.price().multiply(BigDecimal.valueOf(quantity));

            // 장바구니 아이템 Dto 생성
            CartItemResponse cartItem = new CartItemResponse(
                    productInfo,
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
     * @param token : jwt 토큰
     * @param request : 조회할 상품의 productIdx와 optionIdx
     * 장바구니에 담긴 단일 상품 상세 조회 (수량 변경 후 응답값 반환용)
     */
    @Override
    @Transactional(readOnly = true)
    public CartItemResponse getCartItem(String token, ProductRequest request) {

        // token으로 userIdx를 가져오기 User-Service 간의 동기통신
        Long userIdx = userClient.getUserIdx(token);

        // 해당 유저의 장바구니에서 특정 상품(옵션 포함) 찾기, 없으면 예외 발생
        CartEntity entity = cartRepository.findByUserIdxAndProductIdxAndOptionIdx(userIdx, request.productIdx(), request.optionIdx()).orElseThrow(() -> new CartNotFoundException(ErrorCode.CART_NOT_FOUND));

        // ProductInfo(상품 정보)를 가져오기 위한 Product-Service간의 동기 통신
        ProductInfo productInfo = productClient.getProduct(entity.getProductIdx(), entity.getOptionIdx());

        // 상품 가격 = 단가 * 수량
        int quantity = entity.getQuantity();
        BigDecimal totalPrice = productInfo.price().multiply(BigDecimal.valueOf(quantity));

        // 응답 DTO 반환
        return new CartItemResponse(
                productInfo,
                quantity,
                totalPrice
        );
    }

    /*
     * @param token : jwt 토큰
     * @param productRequest : 삭제할 상품의 productIdx와 optionIdx
     * 장바구니 상품 아예 삭제 (X 클릭 또는 수량 0 미만 처리)
     */
    @Override
    @Transactional
    public void deleteCartItem(String token, ProductRequest productRequest) {

        // token으로 userIdx를 가져오기 User-Service 간의 동기통신
        Long userId = userClient.getUserIdx(token);

        // 삭제할 장바구니 아이템 조회
        Optional<CartEntity> existingCart = cartRepository.findByUserIdxAndProductIdxAndOptionIdx(userId, productRequest.productIdx(), productRequest.optionIdx());

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
