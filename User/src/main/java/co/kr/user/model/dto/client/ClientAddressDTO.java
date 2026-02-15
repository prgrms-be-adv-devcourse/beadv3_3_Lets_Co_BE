package co.kr.user.model.dto.client;

import lombok.Data;

/**
 * 타 서비스(예: 배송 시스템)에서 특정 사용자의 주소 정보를 조회할 때 제공하는 DTO입니다.
 * 배송에 필요한 최소한의 핵심 주소 정보만을 포함합니다.
 */
@Data
public class ClientAddressDTO {
    /** 수령인 성함 */
    private String recipient;
    /** 기본 배송지 주소 */
    private String address;
    /** 상세 배송지 주소 (동, 호수 등) */
    private String addressDetail;
    /** 수령인 비상 연락처 */
    private String phoneNumber;
}