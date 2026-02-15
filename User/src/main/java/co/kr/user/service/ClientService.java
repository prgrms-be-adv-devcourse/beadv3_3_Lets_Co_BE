package co.kr.user.service;

import co.kr.user.model.dto.client.BalanceReq;
import co.kr.user.model.dto.client.ClientAddressDTO;
import co.kr.user.model.dto.client.ClientRoleDTO;

/**
 * 클라이언트(사용자)의 편의 기능 및 결제 관련 동작을 지원하는 서비스 인터페이스입니다.
 * 권한 조회, 잔액 관리, 배송지 및 카드 조회 등의 기능을 정의합니다.
 */
public interface ClientService {

    /**
     * 특정 사용자의 현재 권한(Role)을 조회합니다.
     * @param userIdx 사용자 식별자 (PK)
     * @return 사용자의 권한 정보가 담긴 DTO (USERS, SELLER, ADMIN 등)
     */
    ClientRoleDTO getRole(Long userIdx);

    /**
     * 사용자의 잔액(포인트/머니)을 변경합니다. (충전, 결제, 환불 등)
     * @param userIdx 사용자 식별자 (PK)
     * @param balanceReq 변경할 잔액 금액과 상태(CHARGE, PAYMENT, REFUND)가 담긴 요청 객체
     * @return 처리 결과 메시지 ("잔액 처리가 완료되었습니다.")
     */
    String balance(Long userIdx, BalanceReq balanceReq);

    /**
     * 사용자의 기본 배송지 정보를 조회합니다.
     * @param userIdx 사용자 식별자 (PK)
     * @return 기본 배송지 정보 DTO (수령인, 주소, 연락처 등)
     */
    ClientAddressDTO defaultAddress(Long userIdx);

    /**
     * 특정 주소 코드로 사용자의 배송지 정보를 조회합니다.
     * @param userIdx 사용자 식별자 (PK)
     * @param addressCode 조회할 주소의 고유 코드
     * @return 해당 배송지 정보 DTO
     */
    ClientAddressDTO searchAddress(Long userIdx, String addressCode);

    /**
     * 사용자의 기본 결제 카드 식별자(PK)를 조회합니다.
     * 유효기간이 만료된 카드는 조회되지 않거나 예외가 발생할 수 있습니다.
     * @param userIdx 사용자 식별자 (PK)
     * @return 기본 카드의 식별자 (Card IDX)
     */
    Long defaultCard(Long userIdx);

    /**
     * 특정 카드 코드로 사용자의 카드 식별자(PK)를 조회합니다.
     * @param userIdx 사용자 식별자 (PK)
     * @param cardCode 조회할 카드의 고유 코드
     * @return 해당 카드의 식별자 (Card IDX)
     */
    Long searchCard(Long userIdx, String cardCode);
}