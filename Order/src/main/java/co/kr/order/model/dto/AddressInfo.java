package co.kr.order.model.dto;

import jakarta.validation.constraints.NotBlank;

/*
 * 주소 정보
 * @param recipient: 수령인
 * @param address: 주소
 * @param addressDetail: 상세 주소
 * @param phone: 전화번호(핸드폰)
 */
public record AddressInfo(

        @NotBlank(message = "수령인은 필수입니다.")
        String recipient,

        @NotBlank(message = "주소는 필수입니다.")
        String address,
        String addressDetail,

        @NotBlank(message = "전화번호는 필수입니다.")
        String phone
) {}