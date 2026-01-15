package co.kr.order.model.dto;

/**
 * @param recipient : 수신자
 * @param address : 주소
 * @param addressDetail : 상세 주소
 * @param phoneNum : 핸드폰 번호
 */
public record AddressInfo(
        String recipient,
        String address,
        String addressDetail,
        String phoneNum
) {}