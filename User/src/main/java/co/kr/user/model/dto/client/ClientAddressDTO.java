package co.kr.user.model.dto.client;

import lombok.Data;

@Data
public class ClientAddressDTO {
    private String recipient;
    private String address;
    private String addressDetail;
    private String phoneNumber;
}
