package co.kr.user.model.dto.address;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 배송지 삭제 요청 시 사용되는 DTO(Data Transfer Object)입니다.
 * 삭제할 배송지를 식별하기 위한 코드 정보를 담고 있습니다.
 */
@Data
public class AddressDelReq {

    /**
     * 삭제할 배송지의 고유 코드입니다.
     * 필수 입력 값이며, 공백일 수 없습니다.
     */
    @NotBlank(message = "주소 코드를 입력해 주세요.")
    private String addressCode;
}