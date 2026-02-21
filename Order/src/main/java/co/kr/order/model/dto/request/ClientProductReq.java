package co.kr.order.model.dto.request;

/*
 * 단일 상품 요청 정보
 * @param productCode: 제품 코드
 * @param optionCode: 제품 옵션 코드
 */
public record ClientProductReq(
        String productCode,
        String optionCode
) {}
