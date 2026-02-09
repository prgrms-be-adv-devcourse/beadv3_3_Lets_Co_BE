package co.kr.user.model.dto.address;

import lombok.Data;

@Data
public class AddressListDTO {
    private String addressCode;
    private int defaultAddress;
    private String recipient;
    private String address;
    private String addressDetail;
    private String phoneNumber;
}