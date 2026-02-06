package co.kr.user.service.Impl;

import co.kr.user.dao.UserCardRepository;
import co.kr.user.dao.UserRepository;
import co.kr.user.model.dto.card.CardListDTO;
import co.kr.user.model.dto.card.CardRequestReq;
import co.kr.user.model.entity.UserCard;
import co.kr.user.model.entity.Users;
import co.kr.user.service.CardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

/**
 * 사용자 결제 수단(카드) 관리 비즈니스 로직을 처리하는 서비스 클래스입니다.
 * 카드 등록, 수정, 삭제(Soft Delete), 목록 조회, 유효기간 검증 기능을 제공합니다.
 * 카드 정보(브랜드, 별칭, 토큰 등)는 보안을 위해 암호화되어 관리됩니다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CardServiceImpl implements CardService {
    private final UserRepository userRepository;
    private final UserCardRepository userCardRepository;

    private final UserQueryServiceImpl userQueryServiceImpl;

    /**
     * 사용자의 기본 결제 카드를 조회하고 유효성을 검증하는 메서드입니다.
     * 기본 카드(DefaultCard=1)를 찾은 후, 유효기간이 지났는지 확인합니다.
     *
     * @param userIdx 로그인한 사용자의 식별자
     * @return 기본 카드의 CardIdx
     */
    @Override
    public Long defaultCard(Long userIdx) {
        Users users = userQueryServiceImpl.findActiveUser(userIdx);

        // 기본 카드 조회
        UserCard userCard = userCardRepository.findFirstByUsersIdxAndDelOrderByCardIdxDesc(users.getUsersIdx(), 0)
                .orElseThrow(() -> new IllegalArgumentException("Default 카드가 없습니다."));

        // 유효기간 검증 (현재 날짜와 비교)
        YearMonth cardExpiry = YearMonth.of(userCard.getExpYear(), userCard.getExpMonth());
        YearMonth currentMonth = YearMonth.now();

        if (cardExpiry.isBefore(currentMonth)) {
            throw new IllegalStateException("만료된 카드입니다. 카드를 다시 등록해 주세요.");
        }

        return userCard.getCardIdx();
    }

    /**
     * 특정 카드 코드(CardCode)로 카드를 조회하고 유효성을 검증하는 메서드입니다.
     * 결제 시 사용자가 선택한 카드가 유효한지 확인할 때 사용됩니다.
     *
     * @param userIdx 로그인한 사용자의 식별자
     * @param cardCode 조회할 카드의 고유 코드
     * @return 해당 카드의 CardIdx
     */
    @Override
    public Long searchCard(Long userIdx, String cardCode) {
        Users users = userQueryServiceImpl.findActiveUser(userIdx);

        // 카드 조회
        UserCard userCard = userCardRepository.findFirstByUsersIdxAndCardCodeAndDelOrderByCardIdxDesc(users.getUsersIdx(), cardCode, 0)
                .orElseThrow(() -> new IllegalArgumentException("해당 카드 정보가 없습니다."));

        // 유효기간 검증
        YearMonth cardExpiry = YearMonth.of(userCard.getExpYear(), userCard.getExpMonth());
        YearMonth currentMonth = YearMonth.now();

        if (cardExpiry.isBefore(currentMonth)) {
            throw new IllegalStateException("만료된 카드입니다. 카드를 다시 등록해 주세요.");
        }

        return userCard.getCardIdx();
    }

    /**
     * 사용자가 등록한 모든 카드 목록을 조회하는 메서드입니다.
     * 암호화된 카드 정보를 복호화하여 DTO 리스트로 반환합니다.
     *
     * @param userIdx 로그인한 사용자의 식별자
     * @return 카드 목록 (CardListDTO 리스트)
     */
    @Override
    public List<CardListDTO> cardList(Long userIdx) {
        Users users = userQueryServiceImpl.findActiveUser(userIdx);

        // 삭제되지 않은 모든 카드 조회
        List<UserCard> userCardList = userCardRepository.findAllByUsersIdxAndDel(users.getUsersIdx(), 0);

        if (userCardList.isEmpty()) {
            throw new IllegalArgumentException("카드를 추가해 주세요");
        }

        // Entity -> DTO 변환 (복호화 포함)
        return userCardList.stream()
                .map(card -> {
                    CardListDTO dto = new CardListDTO();
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

    /**
     * 신규 카드를 등록하는 메서드입니다.
     * 민감 정보(카드 토큰, 이름 등)를 암호화하여 저장합니다.
     *
     * @param userIdx 로그인한 사용자의 식별자
     * @param cardRequestReq 등록할 카드 정보
     * @return 처리 결과 메시지
     */
    @Override
    @Transactional
    public String addCard(Long userIdx, CardRequestReq cardRequestReq) {
        Users users = userQueryServiceImpl.findActiveUser(userIdx);

        // 카드 엔티티 생성 및 암호화 저장
        UserCard userCard = UserCard.builder()
                .usersIdx(users.getUsersIdx())
                .cardCode(UUID.randomUUID().toString()) // 고유 식별 코드 생성
                .cardBrand(cardRequestReq.getCardBrand())
                .cardName(cardRequestReq.getCardName())
                .cardToken(cardRequestReq.getCardToken())
                .expMonth(cardRequestReq.getExpMonth())
                .expYear(cardRequestReq.getExpYear())
                .build();

        userCardRepository.save(userCard);

        return "카드가 성공적으로 추가되었습니다.";
    }

    /**
     * 카드 정보를 수정하는 메서드입니다.
     * 유효기간 연장, 별칭 변경 등의 작업을 수행하며, 입력된 필드에 한해 업데이트 및 암호화를 수행합니다.
     *
     * @param userIdx 로그인한 사용자의 식별자
     * @return 처리 결과 메시지
     */
    @Override
    @Transactional
    public String updateCard(Long userIdx, CardRequestReq req) {
        Users users = userQueryServiceImpl.findActiveUser(userIdx);

        UserCard userCard = userCardRepository.findByCardCodeAndDel(req.getCardCode(), 0)
                .orElseThrow(() -> new IllegalArgumentException("해당 카드 정보를 찾을 수 없습니다."));

        if (!userCard.getUsersIdx().equals(users.getUsersIdx())) {
            throw new IllegalStateException("본인의 카드만 수정할 수 있습니다.");
        }

        // [변경] 수동 암호화 없이 입력값이 있으면 업데이트, 없으면 기존값 유지
        userCard.updateCard(
                (req.getCardBrand() == null || req.getCardBrand().isEmpty()) ? userCard.getCardBrand() : req.getCardBrand(),
                (req.getCardName() == null || req.getCardName().isEmpty()) ? userCard.getCardName() : req.getCardName(),
                (req.getCardToken() == null || req.getCardToken().isEmpty()) ? userCard.getCardToken() : req.getCardToken(),
                req.getExpMonth() == 0 ? userCard.getExpMonth() : req.getExpMonth(),
                req.getExpYear() == 0 ? userCard.getExpYear() : req.getExpYear()
        );

        return "카드 정보가 수정되었습니다.";
    }

    /**
     * 카드를 삭제하는 메서드입니다.
     * 실제 DB에서 삭제하지 않고 Del 플래그를 변경하여 비활성화(Soft Delete)합니다.
     *
     * @param userIdx 로그인한 사용자의 식별자
     * @param CardCode 삭제할 카드 코드
     * @return 처리 결과 메시지
     */
    @Override
    @Transactional
    public String deleteCard(Long userIdx, String CardCode) {
        Users users = userQueryServiceImpl.findActiveUser(userIdx);

        // 삭제할 카드 조회
        UserCard userCard = userCardRepository.findByCardCodeAndDel(CardCode, 0)
                .orElseThrow(() -> new IllegalArgumentException("해당 주소 정보를 찾을 수 없습니다."));

        // 소유권 검증
        if (!userCard.getUsersIdx().equals(users.getUsersIdx())) {
            throw new IllegalStateException("본인의 주소만 수정할 수 있습니다.");
        }

        // 삭제 처리 (Soft Delete)
        userCard.deleteCard();

        return "카드가 삭제되었습니다.";
    }
}