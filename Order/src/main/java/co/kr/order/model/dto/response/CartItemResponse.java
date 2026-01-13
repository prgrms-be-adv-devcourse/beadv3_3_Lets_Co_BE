package co.kr.order.model.dto.response;

import co.kr.order.model.dto.ProductInfo;

import java.math.BigDecimal;

/*
 * @param product : 제품 정보 (ProductInfo)
 * @param quantity : 카트에 담긴 제품의 개수
 * @param amount : 가격 * 개수
 */
public record CartItemResponse(
        ProductInfo product,
        Integer quantity,
        BigDecimal amount
) {}
