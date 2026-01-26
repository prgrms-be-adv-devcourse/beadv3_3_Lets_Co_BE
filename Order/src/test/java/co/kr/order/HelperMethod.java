package co.kr.order;

import co.kr.order.client.ProductClient;
import co.kr.order.model.dto.ProductInfo;
import co.kr.order.model.entity.*;
import co.kr.order.model.vo.OrderStatus;
import co.kr.order.model.vo.PaymentStatus;
import co.kr.order.model.vo.PaymentType;
import co.kr.order.model.vo.SettlementType;
import co.kr.order.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles
public class HelperMethod {

    @Autowired protected MockMvc mvc;
    @Autowired protected ObjectMapper om;
    @PersistenceContext protected EntityManager em;

    @MockitoBean protected ProductClient productClient;

    @Autowired protected OrderJpaRepository orderRepository;
    @Autowired protected OrderItemJpaRepository orderItemRepository;
    @Autowired protected PaymentJpaRepository paymentRepository;
    @Autowired protected SettlementRepository settlementRepository;
    @Autowired protected CartJpaRepository cartRepository;


    protected OrderEntity createOrder(Long userIdx, Long addressIdx, Long cardIdx, int itemAmount, int totalAmount) {
        OrderEntity orderEntity = new OrderEntity(
                userIdx,
                addressIdx,
                cardIdx,
                UUID.randomUUID().toString(),
                OrderStatus.CREATED,
                new BigDecimal(itemAmount),
                new BigDecimal(totalAmount),
                false
        );

        return orderRepository.save(orderEntity);
    }

    protected OrderItemEntity createItem(OrderEntity order, Long productIdx, Long optionIdx, String productName, String optionName, BigDecimal price, int quantity) {
        OrderItemEntity itemEntity = OrderItemEntity.builder()
                .order(order)
                .productIdx(productIdx)
                .optionIdx(optionIdx)
                .productName(productName)
                .optionName(optionName)
                .price(price)
                .quantity(quantity)
                .del(false)
                .build();

        return orderItemRepository.save(itemEntity);
    }

    protected PaymentEntity createPayment(Long userIdx, Long orderIdx, Long cardIdx, PaymentType type, BigDecimal amount, String tossKey) {
        PaymentEntity payment = PaymentEntity.builder()
                .usersIdx(userIdx)
                .ordersIdx(orderIdx)
                .cardIdx(cardIdx)
                .status(PaymentStatus.PAYMENT)
                .type(type)
                .amount(amount)
                .paymentKey(tossKey)
                .build();

        return paymentRepository.save(payment);
    }

    protected SettlementHistoryEntity createSettlement(Long sellerIdx, Long paymentIdx, BigDecimal amount) {
        SettlementHistoryEntity settlement =  SettlementHistoryEntity.builder()
                .sellerIdx(sellerIdx)
                .paymentIdx(paymentIdx)
                .type(SettlementType.SETTLE_PAYOUT)
                .amount(amount)
                .build();

        return settlementRepository.save(settlement);
    }

    protected CartEntity createCart (Long userIdx, Long productIdx, Long optionIdx, BigDecimal price, int quantity) {
        CartEntity cart = CartEntity.builder()
                .userIdx(userIdx)
                .productIdx(productIdx)
                .optionIdx(optionIdx)
                .price(price)
                .quantity(quantity)
                .del(false)
                .build();

        return cartRepository.save(cart);
    }

    protected ProductInfo createProductInfo(Long productIdx, Long optionIdx, Long sellerIdx, String name, String option, int price, int stock) {
        return new ProductInfo(
                productIdx,
                optionIdx,
                sellerIdx,
                name,
                option,
                new BigDecimal(price),
                stock
        );
    }
}
