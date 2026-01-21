package co.kr.user.model.DTO.card;

import lombok.Data;

@Data
public class CardRequestReq {
    private String cardCode;
    private int defaultCard;
    private String cardBrand;
    private String cardName;
    private String cardToken;
    private int expMonth;
    private int expYear;
}