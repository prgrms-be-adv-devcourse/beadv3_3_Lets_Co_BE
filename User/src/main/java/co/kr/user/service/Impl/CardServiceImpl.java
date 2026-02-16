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

/**
 * CardService 인터페이스의 구현체입니다.
 * 사용자의 결제 카드(신용/체크카드) 정보 관리와 관련된 비즈니스 로직을 처리합니다.
 */
@Service // 스프링의 서비스 컴포넌트로 등록
@RequiredArgsConstructor // final 필드에 대한 생성자 자동 생성
@Transactional(readOnly = true) // 기본적으로 읽기 전용 트랜잭션 사용 (조회 최적화)
public class CardServiceImpl implements CardService {
    private final UserCardRepository userCardRepository;
    private final UserQueryService userQueryService;

    /**
     * 사용자의 등록된 카드 목록을 조회합니다.
     * 기본 카드를 목록의 최상단에 위치시킵니다.
     * @param userIdx 사용자 식별자 (PK)
     * @return 정렬된 카드 목록 DTO 리스트
     */
    @Override
    public List<CardListDTO> cardList(Long userIdx) {
        // 사용자의 상세 정보를 조회하여 현재 설정된 기본 카드 인덱스를 가져옵니다.
        UsersInformation usersInformation = userQueryService.findActiveUserInfo(userIdx);
        Long defaultCardIdx = usersInformation.getDefaultCard();

        // 삭제되지 않은(ACTIVE) 사용자의 모든 카드 목록을 조회합니다.
        List<UserCard> userCardList = userCardRepository.findAllByUsersIdxAndDel(userIdx, UserDel.ACTIVE);

        // 등록된 카드가 없으면 예외를 발생시켜 클라이언트에게 알립니다.
        if (userCardList.isEmpty()) {
            throw new IllegalArgumentException("카드를 추가해 주세요");
        }

        // 스트림 API를 사용하여 정렬 및 DTO 변환을 수행합니다.
        return userCardList.stream()
                .sorted((a, b) -> {
                    // 기본 카드가 맨 위로 오도록 내림차순 정렬 (기본카드 여부: 1, 아님: 0)
                    int aVal = a.getCardIdx().equals(defaultCardIdx) ? 1 : 0;
                    int bVal = b.getCardIdx().equals(defaultCardIdx) ? 1 : 0;
                    return Integer.compare(bVal, aVal);
                })
                .map(card -> {
                    CardListDTO dto = new CardListDTO();
                    // 현재 카드가 기본 카드인지 표시 (1: True, 0: False)
                    int isDefault = card.getCardIdx().equals(defaultCardIdx) ? 1 : 0;
                    dto.setDefaultCard(isDefault);
                    dto.setCardCode(card.getCardCode());
                    dto.setCardBrand(card.getCardBrand());
                    dto.setCardName(card.getCardName());
                    dto.setCardToken(card.getCardToken()); // 보안상 마스킹 처리가 필요할 수 있음
                    dto.setExpMonth(card.getExpMonth());
                    dto.setExpYear(card.getExpYear());
                    return dto;
                })
                .toList(); // 불변 리스트로 반환
    }

    /**
     * 새로운 카드를 등록합니다.
     * @param userIdx 사용자 식별자 (PK)
     * @param req 카드 등록 요청 정보 (브랜드, 번호, 유효기간 등)
     * @return 성공 메시지
     */
    @Override
    @Transactional // 데이터 쓰기 작업이므로 트랜잭션 필요
    public String addCard(Long userIdx, CardRequestReq req) {
        // 사용자 활성 상태 확인
        userQueryService.findActiveUser(userIdx);

        // 카드 엔티티 생성 및 정보 설정
        UserCard userCard = UserCard.builder()
                .usersIdx(userIdx)
                .cardBrand(req.getCardBrand())
                .cardName(req.getCardName())
                .cardToken(req.getCardToken()) // 실제 서비스에서는 결제사로부터 받은 빌링키 등을 저장
                .expMonth(req.getExpMonth())
                .expYear(req.getExpYear())
                .build();

        // DB에 카드 정보 저장
        userCardRepository.save(userCard);

        // '기본 카드로 설정' 옵션이 켜져 있는 경우, 사용자 정보의 기본 카드 ID 업데이트
        if (req.isDefaultCard()) {
            UsersInformation usersInformation = userQueryService.findActiveUserInfo(userIdx);
            usersInformation.updateDefaultCard(userCard.getCardIdx());
        }

        return "카드가 성공적으로 추가되었습니다.";
    }

    /**
     * 등록된 카드 정보를 수정합니다.
     * @param userIdx 사용자 식별자 (PK)
     * @param req 수정할 카드 정보 (CardCode 필수)
     * @return 성공 메시지
     */
    @Override
    @Transactional // 트랜잭션 적용
    public String updateCard(Long userIdx, CardRequestReq req) {
        // 사용자 활성 상태 확인
        userQueryService.findActiveUser(userIdx);

        // 카드 코드와 사용자 ID로 수정할 카드 엔티티 조회
        UserCard userCard = userCardRepository.findByCardCodeAndUsersIdxAndDel(req.getCardCode(), userIdx, UserDel.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("요청하신 카드 정보가 없습니다."));

        // 엔티티 정보 업데이트 (Dirty Checking으로 자동 반영)
        userCard.updateCard(req.getCardBrand(), req.getCardName(), req.getCardToken(), req.getExpMonth(), req.getExpYear());

        // 기본 카드로 설정 요청 시 업데이트
        if (req.isDefaultCard()) {
            UsersInformation usersInformation = userQueryService.findActiveUserInfo(userIdx);
            usersInformation.updateDefaultCard(userCard.getCardIdx());
        }

        return "카드 정보가 수정되었습니다.";
    }

    /**
     * 카드를 삭제합니다. (논리적 삭제)
     * @param userIdx 사용자 식별자 (PK)
     * @param cardCode 삭제할 카드 코드
     * @return 성공 메시지
     */
    // [참고] 기본 카드를 삭제할 경우, 별도의 대체 로직 없이 null 처리합니다.
    // 클라이언트 측에서 삭제 전 경고를 주거나, 추후 주문 시 주소 입력을 유도해야 합니다.
    @Override
    @Transactional // 트랜잭션 적용
    public String deleteCard(Long userIdx, String cardCode) {
        // 사용자 활성 상태 확인
        userQueryService.findActiveUser(userIdx);

        // 삭제할 카드 엔티티 조회
        UserCard userCard = userCardRepository.findByCardCodeAndUsersIdxAndDel(cardCode, userIdx, UserDel.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("요청하신 카드 정보가 없습니다."));

        // 상태를 DELETED로 변경
        userCard.deleteCard();
        return "카드가 삭제되었습니다.";
    }
}