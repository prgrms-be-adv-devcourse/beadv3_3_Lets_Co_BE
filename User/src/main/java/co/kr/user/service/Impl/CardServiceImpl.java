package co.kr.user.service.Impl;

import co.kr.user.dao.UserCardRepository;
import co.kr.user.model.dto.card.CardListDTO;
import co.kr.user.model.dto.card.CardRequestReq;
import co.kr.user.model.entity.UserCard;
import co.kr.user.model.entity.UsersInformation;
import co.kr.user.model.vo.UserDel;
import co.kr.user.service.CardService;
import co.kr.user.service.UserQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CardServiceImpl implements CardService {
    private final UserCardRepository userCardRepository;
    private final UserQueryService userQueryService;

    @Override
    public List<CardListDTO> cardList(Long userIdx) {
        UsersInformation usersInformation = userQueryService.findActiveUserInfo(userIdx);
        Long defaultCardIdx = usersInformation.getDefaultCard();

        List<UserCard> userCardList = userCardRepository.findAllByUsersIdxAndDel(userIdx, UserDel.ACTIVE);

        if (userCardList.isEmpty()) {
            throw new IllegalArgumentException("카드를 추가해 주세요");
        }

        return userCardList.stream()
                .sorted((a, b) -> {
                    int aVal = a.getCardIdx().equals(defaultCardIdx) ? 1 : 0;
                    int bVal = b.getCardIdx().equals(defaultCardIdx) ? 1 : 0;
                    return Integer.compare(bVal, aVal);
                })
                .map(card -> {
                    CardListDTO dto = new CardListDTO();
                    int isDefault = card.getCardIdx().equals(defaultCardIdx) ? 1 : 0;
                    dto.setDefaultCard(isDefault);
                    dto.setCardCode(card.getCardCode());
                    dto.setCardBrand(card.getCardBrand());
                    dto.setCardName(card.getCardName());
                    dto.setCardToken(card.getCardToken());
                    dto.setExpMonth(card.getExpMonth());
                    dto.setExpYear(card.getExpYear());
                    return dto;
                })
                .toList();
    }

    @Override
    @Transactional
    public String addCard(Long userIdx, CardRequestReq req) {
        userQueryService.findActiveUser(userIdx);

        UserCard userCard = UserCard.builder()
                .usersIdx(userIdx)
                .cardCode(UUID.randomUUID().toString())
                .cardBrand(req.getCardBrand())
                .cardName(req.getCardName())
                .cardToken(req.getCardToken())
                .expMonth(req.getExpMonth())
                .expYear(req.getExpYear())
                .build();

        userCardRepository.save(userCard);

        if (req.isDefaultCard()) {
            UsersInformation usersInformation = userQueryService.findActiveUserInfo(userIdx);
            usersInformation.updateDefaultCard(userCard.getCardIdx());
        }

        return "카드가 성공적으로 추가되었습니다.";
    }

    @Override
    @Transactional
    public String updateCard(Long userIdx, CardRequestReq req) {
        userQueryService.findActiveUser(userIdx);
        UserCard userCard = userCardRepository.findByCardCodeAndUsersIdxAndDel(req.getCardCode(), userIdx, UserDel.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("요청하신 카드 정보가 없습니다."));

        userCard.updateCard(req.getCardBrand(), req.getCardName(), req.getCardToken(), req.getExpMonth(), req.getExpYear());

        if (req.isDefaultCard()) {
            UsersInformation usersInformation = userQueryService.findActiveUserInfo(userIdx);
            usersInformation.updateDefaultCard(userCard.getCardIdx());
        }

        return "카드 정보가 수정되었습니다.";
    }

    @Override
    @Transactional
    public String deleteCard(Long userIdx, String cardCode) {
        userQueryService.findActiveUser(userIdx);
        UserCard userCard = userCardRepository.findByCardCodeAndUsersIdxAndDel(cardCode, userIdx, UserDel.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("요청하신 카드 정보가 없습니다."));

        userCard.deleteCard();
        return "카드가 삭제되었습니다.";
    }
}