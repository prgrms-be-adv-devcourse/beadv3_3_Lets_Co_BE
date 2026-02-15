package co.kr.user.model.dto.admin;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 관리자가 특정 회원의 계정을 정지(잠금)할 때 사용하는 DTO입니다.
 * 언제까지 계정을 사용할 수 없게 할지 정지 해제 시간을 담고 있습니다.
 */
@Data
public class AdminLockedUntilReq {
    /**
     * 계정 잠금이 유지될 일시입니다. (필수 값)
     * JSON 데이터와 매핑 시 "yyyy-MM-dd HH:mm:ss.SSS" 형식을 사용합니다.
     */
    @NotNull(message = "잠금 기간은 필수 값입니다.")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    private LocalDateTime localDateTime;
}