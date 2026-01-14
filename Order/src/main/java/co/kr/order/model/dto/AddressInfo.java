package co.kr.order.model.dto;

public record AddressInfo(
        String recipient,
        String address,
        String addressDetail,
        String phoneNum
) {}