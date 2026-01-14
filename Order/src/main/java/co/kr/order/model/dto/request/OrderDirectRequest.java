package co.kr.order.model.dto.request;

public record OrderDirectRequest(
        OrderRequest orderRequest,
        UserDataRequest userData
) {}
