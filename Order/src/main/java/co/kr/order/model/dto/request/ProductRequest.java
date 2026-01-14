package co.kr.order.model.dto.request;

// 제품 정보를 가져오기 위해 사용될 제품 id, 옵션 id
public record ProductRequest(
        Long productIdx,
        Long optionIdx
) { }
