package co.kr.user.model.dto.address;

import lombok.Data;

/**
 * 새로운 배송지를 등록하거나 기존 배송지 정보를 수정할 때 사용하는 DTO입니다.
 */
@Data
public class AddressRequestReq {

    /** 수정 시 대상 주소를 식별하기 위한 고유 코드 (등록 시에는 비어있을 수 있음) */
    private String addressCode;
    /** 해당 주소를 기본 배송지로 설정할지 여부 */
    private boolean defaultAddress;
    /** 수령인 이름 */
    private String recipient;
    /** 기본 주소 */
    private String address;
    /** 상세 주소 */
    private String addressDetail;
    /** 수령인 연락처 */
    private String phoneNumber;
}