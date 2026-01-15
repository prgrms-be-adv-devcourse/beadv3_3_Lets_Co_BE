package co.kr.order.model.dto.request;

/*
 * @param orderRequest : productIdx, optionIdx, quantity
 * @param userData : 주소정보, 카그정보
 */
public record OrderDirectRequest(
        OrderRequest orderRequest,
        UserDataRequest userData
) {}
