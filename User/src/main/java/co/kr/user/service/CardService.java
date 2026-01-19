package co.kr.user.service;

import co.kr.user.DAO.UserCardRepository;
import co.kr.user.DAO.UserRepository;
import co.kr.user.model.DTO.card.CardListDTO;
import co.kr.user.model.DTO.card.CardRequestReq;
import co.kr.user.model.entity.UserCard;
import co.kr.user.model.entity.Users;
import co.kr.user.util.AESUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CardService implements CardServiceImpl{
    private final UserRepository userRepository;
    private final UserCardRepository userCardRepository;

    private final AESUtil aesUtil;

    @Override
    public Long defaultCard(Long userIdx) {
        Users users = userRepository.findById(userIdx)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이디입니다."));


        if (users.getDel() == 1) {
            throw new IllegalStateException("탈퇴한 회원입니다.");
        }
        else if (users.getDel() == 2) {
            throw new IllegalStateException("인증을 먼저 시도해 주세요.");
        }

        UserCard userCard = userCardRepository.findFirstByUsersIdxAndDefaultCardAndDelOrderByCardIdxDesc(users.getUsersIdx(), 1, 0)
                .orElseThrow(() -> new IllegalArgumentException("Default 카드가 없습니다."));

        YearMonth cardExpiry = YearMonth.of(userCard.getExpYear(), userCard.getExpMonth());
        YearMonth currentMonth = YearMonth.now();

        if (cardExpiry.isBefore(currentMonth)) {
            throw new IllegalStateException("만료된 카드입니다. 카드를 다시 등록해 주세요.");
        }

        return userCard.getCardIdx();
    }

    @Override
    public Long searchCard(Long userIdx, String cardCode) {
        Users users = userRepository.findById(userIdx)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이디입니다."));

        if (users.getDel() == 1) {
            throw new IllegalStateException("탈퇴한 회원입니다.");
        }
        else if (users.getDel() == 2) {
            throw new IllegalStateException("인증을 먼저 시도해 주세요.");
        }

        UserCard userCard = userCardRepository.findFirstByUsersIdxAndCardCodeAndDelOrderByCardIdxDesc(users.getUsersIdx(), cardCode, 0)
                .orElseThrow(() -> new IllegalArgumentException("해당 카드 정보가 없습니다."));

        YearMonth cardExpiry = YearMonth.of(userCard.getExpYear(), userCard.getExpMonth());
        YearMonth currentMonth = YearMonth.now();

        if (cardExpiry.isBefore(currentMonth)) {
            throw new IllegalStateException("만료된 카드입니다. 카드를 다시 등록해 주세요.");
        }

        return userCard.getCardIdx();
    }

    @Override
    public List<CardListDTO> cardList(Long userIdx) {
        Users users = userRepository.findById(userIdx)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이디입니다."));

        if (users.getDel() == 1) {
            throw new IllegalStateException("탈퇴한 회원입니다.");
        }
        else if (users.getDel() == 2) {
            throw new IllegalStateException("인증을 먼저 시도해 주세요.");
        }

        List<UserCard> userCardList = userCardRepository.findAllByUsersIdxAndDel(users.getUsersIdx(), 0);

        if (userCardList.isEmpty()) {
            throw new IllegalArgumentException("카드를 추가해 주세요");
        }

        return userCardList.stream()
                .map(card -> {
                    CardListDTO dto = new CardListDTO();
                    dto.setCardCode(card.getCardCode());
                    dto.setDefaultCard(card.getDefaultCard());
                    dto.setCardBrand(aesUtil.decrypt(card.getCardBrand()));
                    dto.setCardName(aesUtil.decrypt(card.getCardName()));
                    dto.setCardToken(aesUtil.decrypt(card.getCardToken()));
                    dto.setExpMonth(card.getExpMonth());
                    dto.setExpYear(card.getExpYear());
                    return dto;
                })
                .toList();
    }

    @Override
    @Transactional
    public String addCard(Long userIdx, CardRequestReq cardRequestReq) {
        Users users = userRepository.findById(userIdx)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이디입니다."));

        if (users.getDel() == 1) {
            throw new IllegalStateException("탈퇴한 회원입니다.");
        }
        else if (users.getDel() == 2) {
            throw new IllegalStateException("인증을 먼저 시도해 주세요.");
        }

        UserCard userCard = UserCard.builder()
                .usersIdx(users.getUsersIdx())
                .cardCode(UUID.randomUUID().toString())
                .defaultCard(cardRequestReq.getDefaultCard())
                .cardBrand(aesUtil.encrypt(cardRequestReq.getCardBrand()))
                .cardName(aesUtil.encrypt(cardRequestReq.getCardName()))
                .cardToken(aesUtil.encrypt(cardRequestReq.getCardToken()))
                .expMonth(cardRequestReq.getExpMonth())
                .expYear(cardRequestReq.getExpYear())
                .build();

        userCardRepository.save(userCard);

        return "카드가 성공적으로 추가되었습니다.";
    }

    @Override
    @Transactional
    public String updateCard(Long userIdx, CardRequestReq cardRequestReq) {
        Users users = userRepository.findById(userIdx)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이디입니다."));

        if (users.getDel() == 1) {
            throw new IllegalStateException("탈퇴한 회원입니다.");
        }
        else if (users.getDel() == 2) {
            throw new IllegalStateException("인증을 먼저 시도해 주세요.");
        }

        UserCard userCard = userCardRepository.findByCardCode(cardRequestReq.getCardCode())
                .orElseThrow(() -> new IllegalArgumentException("해당 카드 정보를 찾을 수 없습니다."));

        if (!userCard.getUsersIdx().equals(userIdx)) {
            throw new IllegalStateException("본인의 주소만 수정할 수 있습니다.");
        }

        CardRequestReq dto = new CardRequestReq();
        dto.setCardCode(userCard.getCardCode());
        if (cardRequestReq.getDefaultCard() == 1) {
            dto.setDefaultCard(1);
        } else {
            dto.setDefaultCard(0);
        }
        if (cardRequestReq.getCardBrand() == null || cardRequestReq.getCardBrand().equals("")) {
            dto.setCardBrand(aesUtil.encrypt(userCard.getCardBrand()));
        }
        else {
            dto.setCardBrand(aesUtil.encrypt(cardRequestReq.getCardBrand()));
        }
        if (cardRequestReq.getCardName() == null || cardRequestReq.getCardName().equals("")) {
            dto.setCardName(aesUtil.encrypt(userCard.getCardName()));
        }
        else {
            dto.setCardName(aesUtil.encrypt(cardRequestReq.getCardName()));
        }
        if (cardRequestReq.getCardToken() == null || cardRequestReq.getCardToken().equals("")) {
            dto.setCardToken(aesUtil.encrypt(userCard.getCardToken()));
        }
        else {
            dto.setCardToken(aesUtil.encrypt(cardRequestReq.getCardToken()));
        }
        if (cardRequestReq.getExpMonth() == 0) {
            dto.setExpMonth(userCard.getExpMonth());
        }
        else {
            dto.setExpMonth(cardRequestReq.getExpMonth());
        }
        if (cardRequestReq.getExpYear() == 0) {
            dto.setExpYear(userCard.getExpYear());
        }
        else {
            dto.setExpYear(cardRequestReq.getExpYear());
        }

        userCard.updateCard(
                dto.getDefaultCard(),
                dto.getCardBrand(),
                dto.getCardName(),
                dto.getCardToken(),
                dto.getExpMonth(),
                dto.getExpYear()
        );

        return "카드 정보가 수정되었습니다.";
    }

    @Override
    @Transactional
    public String deleteCard(Long userIdx, String CardCode) {
        Users users = userRepository.findById(userIdx)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이디입니다."));

        if (users.getDel() == 1) {
            throw new IllegalStateException("탈퇴한 회원입니다.");
        }
        else if (users.getDel() == 2) {
            throw new IllegalStateException("인증을 먼저 시도해 주세요.");
        }

        UserCard userCard = userCardRepository.findByCardCode(CardCode)
                .orElseThrow(() -> new IllegalArgumentException("해당 주소 정보를 찾을 수 없습니다."));

        if (!userCard.getUsersIdx().equals(userIdx)) {
            throw new IllegalStateException("본인의 주소만 수정할 수 있습니다.");
        }

        userCard.del();

        return "카드가 삭제되었습니다.";
    }
}