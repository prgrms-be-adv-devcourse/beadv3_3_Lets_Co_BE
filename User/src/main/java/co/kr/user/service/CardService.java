package co.kr.user.service;

import co.kr.user.model.dto.card.CardListDTO;
import co.kr.user.model.dto.card.CardRequestReq;

import java.util.List;

public interface CardService {
    List<CardListDTO> cardList(Long userIdx);

    String addCard(Long userIdx, CardRequestReq cardRequestReq);

    String updateCard(Long userIdx, CardRequestReq cardRequestReq);

    String deleteCard(Long userIdx, String cardCode);
}