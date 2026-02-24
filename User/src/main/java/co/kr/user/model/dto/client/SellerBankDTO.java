package co.kr.user.model.dto.client;

import lombok.Data;

/**
 * 타 서비스(주문, 정산 등)와의 통신 시 판매자의 은행 계좌 정보를 전달하기 위한 DTO 클래스입니다.
 */
@Data
public class SellerBankDTO {

    /**
     * 은행 브랜드 명칭 (예: 신한은행, 국민은행 등)
     */
    private String bankBrand;

    /**
     * 계좌 예금주 성명
     */
    private String bankName;

    /**
     * 암호화 처리된 계좌 번호 또는 인증용 토큰
     */
    private String bankToken;
}