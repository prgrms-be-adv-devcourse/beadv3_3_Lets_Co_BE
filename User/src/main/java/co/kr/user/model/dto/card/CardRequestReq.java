package co.kr.user.model.dto.card;

import lombok.Data;

@Data
public class CardRequestReq {
    private String cardCode;
    private boolean defaultCard;
    private String cardBrand;
    private String cardName;
    private String cardToken;
    private int expMonth;
    private int expYear;
}