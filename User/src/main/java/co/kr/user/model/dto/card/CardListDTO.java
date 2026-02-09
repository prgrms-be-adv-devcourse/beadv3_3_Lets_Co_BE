package co.kr.user.model.dto.card;

import lombok.Data;

@Data
public class CardListDTO {
    private String cardCode;
    private int defaultCard;
    private String cardBrand;
    private String cardName;
    private String cardToken;
    private int expMonth;
    private int expYear;
}