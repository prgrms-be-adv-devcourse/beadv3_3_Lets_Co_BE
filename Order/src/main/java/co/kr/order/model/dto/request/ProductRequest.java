package co.kr.order.model.dto.request;

/*
 * @param productIdx
 * @param optionIdx
 */
public record ProductRequest(
        Long productIdx,
        Long optionIdx
) { }
