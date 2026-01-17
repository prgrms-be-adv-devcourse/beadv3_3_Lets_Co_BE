package co.kr.order.model.dto;

import java.math.BigDecimal;

/*
 * @param productIdx : 제품 id
 * @param productName : 제품 이름
 * @param optionName : 제품 옵션 내용
 * @param price : 제품 가격
 * @param stock : 제품 개수
 */
public record ProductInfo(
        Long productIdx,
        Long optionIdx,
        String productName,
//        String imageUrl,
        String optionName,
        BigDecimal price,
        Integer stock
) {}
