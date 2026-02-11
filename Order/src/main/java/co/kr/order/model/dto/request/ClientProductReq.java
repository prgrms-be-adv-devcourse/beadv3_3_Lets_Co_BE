package co.kr.order.model.dto.request;

/*
 * 단일 상품 요청 정보
 * @param productIdx: 제품 인덱스
 * @param optionIdx: 제품 옵션 인덱스
 */
public record ClientProductReq(
        Long productIdx,
        Long optionIdx
) {}
