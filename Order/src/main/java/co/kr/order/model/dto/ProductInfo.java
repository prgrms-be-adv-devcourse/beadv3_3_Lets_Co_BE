package co.kr.order.model.dto;

import java.math.BigDecimal;

/**
 * @param productIdx : 제품 id
 * @param productName : 제품 이름
 * @param optionContent : 제품 옵션 내용
 * @param price : 제품 가격
 * @param stock : 제품 개수
 * Product-service에 동기통신 후 받는 제품 정보
 */
public record ProductInfo(
        Long productIdx,
        Long optionIdx,
        String productName,
//        String imageUrl,
        String optionContent,
        BigDecimal price,
        Integer stock
) {}
