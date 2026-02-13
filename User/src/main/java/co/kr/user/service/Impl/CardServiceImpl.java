package co.kr.user.service.Impl;

import co.kr.user.dao.UserCardRepository;
import co.kr.user.dao.UserInformationRepository;
import co.kr.user.model.dto.card.CardListDTO;
import co.kr.user.model.dto.card.CardRequestReq;
import co.kr.user.model.entity.UserCard;
import co.kr.user.model.entity.Users;
import co.kr.user.model.entity.UsersInformation;
import co.kr.user.service.CardService;
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
    private final UserInformationRepository userInformationRepository;

    private final UserQueryServiceImpl userQueryServiceImpl;

    @Override
    public List<CardListDTO> cardList(Long userIdx) {
        Users users = userQueryServiceImpl.findActiveUser(userIdx);

        UsersInformation usersInformation = userInformationRepository.findByUsersIdxAndDel(users.getUsersIdx(), 0)
                .orElseThrow(() -> new IllegalArgumentException("사용자 상세 정보가 없습니다."));

        Long defaultCardIdx = usersInformation.getDefaultCard();

        List<UserCard> userCardList = userCardRepository.findAllByUsersIdxAndDel(users.getUsersIdx(), 0);

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
    public String addCard(Long userIdx, CardRequestReq cardRequestReq) {
        Users users = userQueryServiceImpl.findActiveUser(userIdx);

        UserCard userCard = UserCard.builder()
                .usersIdx(users.getUsersIdx())
                .cardCode(UUID.randomUUID().toString())
                .cardBrand(cardRequestReq.getCardBrand())
                .cardName(cardRequestReq.getCardName())
                .cardToken(cardRequestReq.getCardToken())
                .expMonth(cardRequestReq.getExpMonth())
                .expYear(cardRequestReq.getExpYear())
                .build();

        userCardRepository.save(userCard);

        if (cardRequestReq.isDefaultCard()) {
            UsersInformation usersInformation = userInformationRepository.findByUsersIdxAndDel(users.getUsersIdx(), 0)
                    .orElseThrow(() -> new IllegalArgumentException("요청하신 주소 코드(" + userCard.getCardCode() + ")에 해당하는 정보가 없습니다."));
            usersInformation.updateDefaultCard(userCard.getCardIdx());
        }

        userCardRepository.save(userCard);

        return "카드가 성공적으로 추가되었습니다.";
    }

    @Override
    @Transactional
    public String updateCard(Long userIdx, CardRequestReq req) {
        Users users = userQueryServiceImpl.findActiveUser(userIdx);

        UserCard userCard = userCardRepository.findByCardCodeAndUsersIdxAndDel(req.getCardCode(), users.getUsersIdx(), 0)
                .orElseThrow(() -> new IllegalArgumentException("요청하신 카드 코드(" + req.getCardCode() + ")에 해당하는 정보가 없습니다."));

        userCard.updateCard(
                req.getCardBrand() != null ? req.getCardBrand() : userCard.getCardBrand(),
                req.getCardName() != null ? req.getCardName() : userCard.getCardName(),
                req.getCardToken() != null ? req.getCardToken() : userCard.getCardToken(),
                req.getExpMonth() != 0 ? req.getExpMonth() : userCard.getExpMonth(),
                req.getExpYear() != 0 ? req.getExpYear() : userCard.getExpYear()
        );



        if (req.isDefaultCard()) {
            UsersInformation usersInformation = userInformationRepository.findByUsersIdxAndDel(users.getUsersIdx(), 0)
                    .orElseThrow(() -> new IllegalArgumentException("사용자 상세 정보를 찾을 수 없습니다."));
            usersInformation.updateDefaultCard(userCard.getCardIdx());
        }

        return "카드 정보가 수정되었습니다.";
    }

    @Override
    @Transactional
    public String deleteCard(Long userIdx, String cardCode) {
        Users users = userQueryServiceImpl.findActiveUser(userIdx);

        UserCard userCard = userCardRepository.findByCardCodeAndUsersIdxAndDel(cardCode, users.getUsersIdx(), 0)
                .orElseThrow(() -> new IllegalArgumentException("요청하신 카드 코드(" + cardCode + ")에 해당하는 정보가 없습니다."));

        if (!userCard.getUsersIdx().equals(userIdx)) {
            throw new IllegalStateException("해당 카드에 대한 수정/삭제 권한이 없습니다.");
        }

        userCard.deleteCard();

        return "카드가 삭제되었습니다.";
    }
}