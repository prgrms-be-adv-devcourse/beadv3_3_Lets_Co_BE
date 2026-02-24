package co.kr.order.model.dto;

/*
 * return 받을 판매자 정보
 * @param sellerIdx: 판매자 인덱스
 * @param businessLicense: 사업자 등록번호
 * @param bankBrand: 은행 브래드
 * @param bankName: 은행명
 * @param bankToken: 은행 토큰키 (혹은 계좌)
 */
public record SellerInfo (
        Long sellerIdx,
        String businessLicense,
        String bankBrand,
        String bankName,
        String bankToken
) {}
