package co.kr.order.service.impl;

import co.kr.order.client.ProductClient;
import co.kr.order.mapper.CartMapper;
import co.kr.order.model.dto.CartDetails;
import co.kr.order.model.dto.ProductInfo;
import co.kr.order.model.entity.CartEntity;
import co.kr.order.repository.CartJpaRepository;
import co.kr.order.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartJpaRepository cartJpaRepository;
    private final ProductClient productClient;


    public List<CartDetails> getCartList() {
        List<CartEntity> entities = cartJpaRepository.findAll();

        return entities.stream()
                .map(entity -> {
                    ProductInfo productInfo = productClient.getProductById(entity.getProductId());

                    return CartMapper.toDetails(entity, productInfo);
                })
                .toList();
    }

    @Override
    public void deleteCart(Long cartId) {
        cartJpaRepository.deleteById(cartId);
    }
}
