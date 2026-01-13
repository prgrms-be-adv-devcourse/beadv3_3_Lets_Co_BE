package co.kr.order.model.dto.request;

import java.util.List;

public record CartOrderRequest (
    List<OrderRequest> list
) {}
