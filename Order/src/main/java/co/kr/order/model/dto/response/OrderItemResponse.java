package co.kr.order.model.dto.response;

import co.kr.order.model.dto.ProductInfo;

import java.math.BigDecimal;

/**
 * @param product : 제품정보
 * @param quantity : 제품 개수
 * @param amount : 가격 * 개수
 */
public record OrderItemResponse(
        ProductInfo product,
        Integer quantity,
        BigDecimal amount
) {}
