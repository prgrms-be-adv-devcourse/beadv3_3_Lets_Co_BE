package co.kr.order.model.dto;

import java.util.List;

public record CartOrderRequest (
    List<OrderRequest> list
) {}
