package co.kr.user.model.dto.balance;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class BalanceReq {
    private String Status;
    private BigDecimal balance;
}
