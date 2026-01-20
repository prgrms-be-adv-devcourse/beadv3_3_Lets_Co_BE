package co.kr.user.service;

import co.kr.user.DAO.UserCardRepository;
import co.kr.user.DAO.UserRepository;
import co.kr.user.model.DTO.card.CardListDTO;
import co.kr.user.model.DTO.card.CardRequestReq;
import co.kr.user.model.entity.UserCard;
import co.kr.user.model.entity.Users;
import co.kr.user.util.AesUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
public class CardService implements CardServiceImpl{
    private final UserRepository userRepository;
    private final UserCardRepository userCardRepository;

    private final AesUtil aesUtil; // 암호화 유틸리티

    /**
     * 사용자의 기본 결제 카드를 조회하고 유효성을 검증하는 메서드입니다.
     * 기본 카드(DefaultCard=1)를 찾은 후, 유효기간이 지났는지 확인합니다.
     *
     * @param userIdx 로그인한 사용자의 식별자
     * @return 기본 카드의 CardIdx
     */
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

        // 기본 카드 조회
        UserCard userCard = userCardRepository.findFirstByUsersIdxAndDefaultCardAndDelOrderByCardIdxDesc(users.getUsersIdx(), 1, 0)
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
        Users users = userRepository.findById(userIdx)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이디입니다."));

        if (users.getDel() == 1) {
            throw new IllegalStateException("탈퇴한 회원입니다.");
        }
        else if (users.getDel() == 2) {
            throw new IllegalStateException("인증을 먼저 시도해 주세요.");
        }

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
        Users users = userRepository.findById(userIdx)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이디입니다."));

        if (users.getDel() == 1) {
            throw new IllegalStateException("탈퇴한 회원입니다.");
        }
        else if (users.getDel() == 2) {
            throw new IllegalStateException("인증을 먼저 시도해 주세요.");
        }

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
        Users users = userRepository.findById(userIdx)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이디입니다."));

        if (users.getDel() == 1) {
            throw new IllegalStateException("탈퇴한 회원입니다.");
        }
        else if (users.getDel() == 2) {
            throw new IllegalStateException("인증을 먼저 시도해 주세요.");
        }

        // 카드 엔티티 생성 및 암호화 저장
        UserCard userCard = UserCard.builder()
                .usersIdx(users.getUsersIdx())
                .cardCode(UUID.randomUUID().toString()) // 고유 식별 코드 생성
                .defaultCard(cardRequestReq.getDefaultCard())
                .cardBrand(aesUtil.encrypt(cardRequestReq.getCardBrand()))
                .cardName(aesUtil.encrypt(cardRequestReq.getCardName()))
                .cardToken(aesUtil.encrypt(cardRequestReq.getCardToken())) // Billing Key 암호화
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
     * @param cardRequestReq 수정할 카드 정보 (CardCode 필수)
     * @return 처리 결과 메시지
     */
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

        // 수정할 카드 조회
        UserCard userCard = userCardRepository.findByCardCode(cardRequestReq.getCardCode())
                .orElseThrow(() -> new IllegalArgumentException("해당 카드 정보를 찾을 수 없습니다."));

        // 소유권 검증
        if (!userCard.getUsersIdx().equals(userIdx)) {
            throw new IllegalStateException("본인의 주소만 수정할 수 있습니다.");
        }

        // 업데이트 DTO 준비 (기존 값 유지 또는 새 값 암호화)
        CardRequestReq dto = new CardRequestReq();
        dto.setCardCode(userCard.getCardCode());

        // 기본 카드 설정
        if (cardRequestReq.getDefaultCard() == 1) {
            dto.setDefaultCard(1);
        } else {
            dto.setDefaultCard(0);
        }

        // 브랜드
        if (cardRequestReq.getCardBrand() == null || cardRequestReq.getCardBrand().equals("")) {
            dto.setCardBrand(userCard.getCardBrand());
        }
        else {
            dto.setCardBrand(aesUtil.encrypt(cardRequestReq.getCardBrand()));
        }

        // 카드 별칭
        if (cardRequestReq.getCardName() == null || cardRequestReq.getCardName().equals("")) {
            dto.setCardName(userCard.getCardName());
        }
        else {
            dto.setCardName(aesUtil.encrypt(cardRequestReq.getCardName()));
        }

        // 카드 토큰
        if (cardRequestReq.getCardToken() == null || cardRequestReq.getCardToken().equals("")) {
            dto.setCardToken(userCard.getCardToken());
        }
        else {
            dto.setCardToken(aesUtil.encrypt(cardRequestReq.getCardToken()));
        }

        // 유효기간 (월)
        if (cardRequestReq.getExpMonth() == 0) {
            dto.setExpMonth(userCard.getExpMonth());
        }
        else {
            dto.setExpMonth(cardRequestReq.getExpMonth());
        }

        // 유효기간 (년)
        if (cardRequestReq.getExpYear() == 0) {
            dto.setExpYear(userCard.getExpYear());
        }
        else {
            dto.setExpYear(cardRequestReq.getExpYear());
        }

        // 엔티티 업데이트 수행
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
        Users users = userRepository.findById(userIdx)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이디입니다."));

        if (users.getDel() == 1) {
            throw new IllegalStateException("탈퇴한 회원입니다.");
        }
        else if (users.getDel() == 2) {
            throw new IllegalStateException("인증을 먼저 시도해 주세요.");
        }

        // 삭제할 카드 조회
        UserCard userCard = userCardRepository.findByCardCode(CardCode)
                .orElseThrow(() -> new IllegalArgumentException("해당 주소 정보를 찾을 수 없습니다."));

        // 소유권 검증
        if (!userCard.getUsersIdx().equals(userIdx)) {
            throw new IllegalStateException("본인의 주소만 수정할 수 있습니다.");
        }

        // 삭제 처리 (Soft Delete)
        userCard.del();

        return "카드가 삭제되었습니다.";
    }
}