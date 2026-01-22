package co.kr.order.model.dto;

public record SellerInfo (
        Long sellerIdx,
        String businessLicense,
        String bankBrand,
        String bankName,
        String bankToken
) {}
