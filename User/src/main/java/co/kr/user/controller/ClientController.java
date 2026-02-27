package co.kr.user.controller;

import co.kr.user.model.dto.address.AddressReq;
import co.kr.user.model.dto.client.*;
import co.kr.user.model.dto.card.CardReq;
import co.kr.user.service.ClientService;
import co.kr.user.util.BaseResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 타 마이크로서비스(주문 서비스, 상품 서비스 등)와의 통신을 위한 전용 컨트롤러입니다.
 * 사용자 권한, 잔액, 주소, 결제 수단 조회 등 내부 시스템 연동 기능을 담당합니다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/client/users")
public class ClientController {
    private final ClientService clientService;

    /**
     * 특정 사용자의 권한(Role) 정보를 조회합니다.
     *
     * @param userIdx 사용자 고유 식별자 (Query Parameter)
     * @return 사용자의 현재 권한 및 활성 상태 정보를 포함한 DTO
     */
    @GetMapping("/role")
    public ResponseEntity<BaseResponse<ClientRoleDTO>> getRole(@RequestParam @Valid Long userIdx) {
        return ResponseEntity.ok(new BaseResponse<>("SUCCESS", clientService.getRole(userIdx)));
    }

    /**
     * 사용자의 잔액(포인트/머니)을 변경 처리합니다.
     * 결제 시 차감, 충전 시 증액, 취소 시 환급 등의 상황에서 주문/결제 서비스가 호출합니다.
     *
     * @param userIdx 사용자 고유 식별자 (Path Variable)
     * @param balanceReq 잔액 변경 금액 및 작업 유형(결제, 충전 등)
     * @return 처리 성공 메시지
     */
    @PostMapping("/{userIdx}/balance")
    public ResponseEntity<BaseResponse<String>> balance(@PathVariable Long userIdx,
                                                        @RequestBody BalanceReq balanceReq) {
        return ResponseEntity.ok(new BaseResponse<>("SUCCESS", clientService.balance(userIdx, balanceReq)));
    }

    /**
     * 사용자가 설정한 기본 배송지 정보를 조회합니다.
     * 주문 시 사용자의 수동 입력 없이 기본 배송지를 불러올 때 활용됩니다.
     *
     * @param userIdx 사용자 고유 식별자
     * @return 기본 배송지 상세 정보 (주소, 수령인, 연락처 등)
     */
    @PostMapping("/{userIdx}/address/default")
    public ResponseEntity<BaseResponse<ClientAddressDTO>> defaultAddress(@PathVariable Long userIdx) {
        return ResponseEntity.ok(new BaseResponse<>("SUCCESS", clientService.defaultAddress(userIdx)));
    }

    /**
     * 특정 주소 코드로 배송지 상세 정보를 검색합니다.
     *
     * @param userIdx 사용자 고유 식별자
     * @param addressReq 조회하고자 하는 배송지의 고유 코드
     * @return 해당 배송지의 상세 정보
     */
    @PostMapping("/{userIdx}/address/search")
    public ResponseEntity<BaseResponse<ClientAddressDTO>> searchAddress(@PathVariable Long userIdx,
                                                                        @RequestBody AddressReq addressReq) {
        return ResponseEntity.ok(new BaseResponse<>("SUCCESS", clientService.searchAddress(userIdx, addressReq.getAddressCode())));
    }

    /**
     * 사용자의 기본 결제 카드 식별자(PK)를 조회합니다.
     *
     * @param userIdx 사용자 고유 식별자
     * @return 기본 결제 카드의 내부 식별 번호(Long)
     */
    @PostMapping("/{userIdx}/card/default")
    public ResponseEntity<BaseResponse<Long>> defaultCard(@PathVariable Long userIdx) {
        return ResponseEntity.ok(new BaseResponse<>("SUCCESS", clientService.defaultCard(userIdx)));
    }

    /**
     * 특정 카드 코드로 카드 식별자(PK)를 검색합니다.
     *
     * @param userIdx 사용자 고유 식별자
     * @param cardReq 조회하고자 하는 카드의 고유 코드
     * @return 해당 카드의 내부 식별 번호(Long)
     */
    @PostMapping("/{userIdx}/card/search")
    public ResponseEntity<BaseResponse<Long>> searchCard(@PathVariable Long userIdx,
                                                         @RequestBody CardReq cardReq) {
        return ResponseEntity.ok(new BaseResponse<>("SUCCESS", clientService.searchCard(userIdx, cardReq.getCardCode())));
    }

    /**
     * 특정 판매자의 프로필 이미지 URL을 조회합니다.
     * 상품 상세 페이지 등에서 판매자 로고를 표시하기 위해 상품 서비스 등에서 호출합니다.
     *
     * @param sellerIdx 판매자 고유 식별자
     * @return 판매자 프로필 이미지의 S3 접근 가능 주소
     */
    @PostMapping("/seller/{sellerIdx}/image")
    public ResponseEntity<BaseResponse<String>> getSellerImage(@PathVariable Long sellerIdx) {
        return ResponseEntity.ok(new BaseResponse<>("SUCCESS", clientService.getSellerProfileImage(sellerIdx)));
    }

    /**
     * 판매자의 정산용 계좌 정보를 조회합니다.
     * 정산 서비스에서 판매 대금을 입금해줄 때 필요한 정보를 얻기 위해 사용됩니다.
     *
     * @param sellerIdx 판매자 고유 식별자
     * @return 은행명, 예금주명, 암호화된 계좌 토큰을 포함한 DTO
     */
    @PostMapping("/seller")
    public ResponseEntity<BaseResponse<List<SellerBankDTO>>> getSellerBanks(@RequestBody List<Long> sellerIdx) {
        return ResponseEntity.ok(new BaseResponse<>("SUCCESS", clientService.getSellerBankInfos(sellerIdx)));
    }

    /**
     * AI 맞춤 추천용: 사용자의 권한, 멤버십, 성별, 생년월일 조회
     */
    @GetMapping("/{userIdx}/context")
    public ResponseEntity<BaseResponse<UserContextDTO>> getUserContext(@PathVariable Long userIdx) {
        return ResponseEntity.ok(new BaseResponse<>("SUCCESS", clientService.getUserContext(userIdx)));
    }
}