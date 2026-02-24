package co.kr.user.model.dto.address;

import lombok.Data;

/**
 * 사용자의 등록된 배송지 정보를 목록 형태로 제공할 때 사용하는 DTO입니다.
 */
@Data
public class AddressListDTO {

    /** 기본 배송지 여부 (1: 기본, 0: 일반) */
    private int defaultAddress;
    /** 배송지 조회를 위한 고유 식별 코드 */
    private String addressCode;
    /** 수령인 이름 */
    private String recipient;
    /** 기본 주소 */
    private String address;
    /** 상세 주소 */
    private String addressDetail;
    /** 수령인 연락처 */
    private String phoneNumber;
}