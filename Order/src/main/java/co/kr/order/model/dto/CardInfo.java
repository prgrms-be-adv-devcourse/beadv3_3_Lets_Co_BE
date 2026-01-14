package co.kr.order.model.dto;

public record CardInfo (
        String cardBrand,
        String cardName,
        String cardToken,
        Integer exp_month,
        Integer exp_year
) {}