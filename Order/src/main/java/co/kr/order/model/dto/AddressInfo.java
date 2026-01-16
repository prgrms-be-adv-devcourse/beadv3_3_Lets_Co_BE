package co.kr.order.model.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * @param recipient : 수신자
 * @param address : 주소
 * @param addressDetail : 상세 주소
 * @param phoneNum : 핸드폰 번호
 */
public record AddressInfo(
        Long addressIdx,

        @NotBlank(message = "수령인은 필수입니다.")
        String recipient,

        @NotBlank(message = "주소는 필수입니다.")
        String address,
        String addressDetail,

        @NotBlank(message = "전화번호는 필수입니다.")
        String phoneNum
) {}