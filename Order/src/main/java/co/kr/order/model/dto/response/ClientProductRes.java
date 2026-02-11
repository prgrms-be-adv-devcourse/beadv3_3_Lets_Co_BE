package co.kr.order.model.dto.response;

import java.math.BigDecimal;

/*
 * return 받을 상품 정보
 * @param productIdx: 상품 인덱스
 * @param optionIdx: 옵션 인덱스
 * @param productCode: 상품 코드
 * @param optionCode: 옵션 코드
 * @param sellerIdx: 판매자 인덱스
 * @param productName: 상품명
 * @param optionContent: 상품 옵션 내용
 * @param price: 상품 가격
 * @param stock: 재고 수
 */
public record ClientProductRes(
        Long productIdx,
        Long optionIdx,
        String productCode,
        String optionCode,
        Long sellerIdx,
        String productName,
//        String imageUrl,
        String optionContent,
        BigDecimal price,
        Integer stock
) {}
