package co.kr.user.service;

import co.kr.user.model.DTO.card.CardListDTO;
import co.kr.user.model.DTO.card.CardRequestReq;

import java.util.List;

public interface CardServiceImpl {
    List<CardListDTO> cardList(Long userIdx);

    String addCard(Long userIdx, CardRequestReq cardRequestReq);

    String updateCard(Long userIdx, CardRequestReq cardRequestReq);

    String deleteCard(Long userIdx, String CardCode);
}
