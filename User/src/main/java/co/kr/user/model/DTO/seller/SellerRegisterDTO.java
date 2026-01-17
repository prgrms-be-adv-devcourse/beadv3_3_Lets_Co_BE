package co.kr.user.model.DTO.seller;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SellerRegisterDTO {

    private String ID;
    private LocalDateTime certificationTime;
}
