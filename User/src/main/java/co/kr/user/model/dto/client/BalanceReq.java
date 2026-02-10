package co.kr.user.model.dto.client;

import co.kr.user.model.vo.OrderStatus;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BalanceReq {
    private OrderStatus status;
    private BigDecimal balance;
}
