package co.kr.order.model.dto;

import java.math.BigDecimal;

/*
 * 주문/장바구니 제품 정보
 * @param productCode: 제품 코드
 * @param optionCode: 제품 옵션 코드
 * @param productName: 상품명
 * @param optionContent: 상품 옵션 내용
 * @param price: 가격
 */
public record ItemInfo (
        String productCode,
        String optionCode,
        String productName,
        String optionContent,
        BigDecimal price
) {}
