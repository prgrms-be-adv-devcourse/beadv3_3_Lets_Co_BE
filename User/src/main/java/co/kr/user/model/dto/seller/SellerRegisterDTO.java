package co.kr.user.model.dto.seller;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SellerRegisterDTO {
    private String mail;
    private LocalDateTime certificationTime;
}