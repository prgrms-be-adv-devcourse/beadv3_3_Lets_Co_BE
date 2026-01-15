package co.kr.order.model.dto.request;

/*
 * @param productIdx
 * @param optionIdx
 * @param quantity
 */
public record OrderRequest(
        Long productIdx,
        Long optionIdx,
        Integer quantity
) {}
