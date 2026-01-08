package co.kr.order.service.impl;

import co.kr.order.repository.OrderJpaRepository;
import co.kr.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private OrderJpaRepository orderJpaRepository;

    // 코드 작성

}
