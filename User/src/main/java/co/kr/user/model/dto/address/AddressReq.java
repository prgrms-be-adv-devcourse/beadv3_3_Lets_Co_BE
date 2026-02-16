package co.kr.user.model.dto.address;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 배송지 코드를 통해 특정 배송지 정보를 요청할 때 사용되는 DTO입니다.
 */
@Data
public class AddressReq {

    /** 조회의 기준이 되는 배송지 고유 식별 코드 */
    @NotBlank(message = "주소 코드를 입력해 주세요.")
    private String addressCode;
}