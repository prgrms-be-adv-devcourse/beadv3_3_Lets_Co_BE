package co.kr.product.product.dto.vo;

public enum ProductStatus {
    ON_SALE,    // 보임 (판매 중)
    SOLD_OUT,   // 품절
    STOPPED,    // 숨김 (판매 x - 판매자)
    BLOCKED,
    STOP_SELLING, 
    HIDE;// 블락 처리(판매 중지 - 관리자)
}
