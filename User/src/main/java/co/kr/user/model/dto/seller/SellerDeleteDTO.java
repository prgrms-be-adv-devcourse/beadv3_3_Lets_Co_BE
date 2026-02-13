package co.kr.user.model.dto.seller;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SellerDeleteDTO {
    private String id;
    private LocalDateTime certificationTime;
}