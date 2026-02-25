package co.kr.order.model.dto;

/**
 * User 서비스에서 반환받는 판매자 계좌 정보
 * @param sellerIdx 판매자 인덱스
 * @param bankBrand 은행 브랜드 (예: KB국민은행)
 * @param bankName 예금주명 (예: (주)용희)
 * @param bankToken 계좌 토큰
 */
public record SellerInfo (
        Long sellerIdx,
        String bankBrand,
        String bankName,
        String bankToken
) {}
