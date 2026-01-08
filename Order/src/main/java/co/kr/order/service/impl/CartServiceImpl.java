package co.kr.order.service.impl;

import co.kr.order.mapper.CartMapper;
import co.kr.order.model.dto.CartDetails;
import co.kr.order.model.entity.OrderItemEntity;
import co.kr.order.repository.OrderItemJpaRepository;
import co.kr.order.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final OrderItemJpaRepository orderItemJpaRepository;

    @Override
    public List<CartDetails> getCartList() {

        List<OrderItemEntity> itemEntities = orderItemJpaRepository.findAll();

        return itemEntities.stream()
                .map(CartMapper::toDetails)
                .toList();
    }

    @Override
    public CartDetails add() {
        return null;
    }
}
