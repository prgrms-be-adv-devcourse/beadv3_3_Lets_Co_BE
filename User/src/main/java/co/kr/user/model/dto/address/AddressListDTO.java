package co.kr.user.model.dto.address;

import lombok.Data;

@Data
public class AddressListDTO {
    private int defaultAddress;
    private String addressCode;
    private String recipient;
    private String address;
    private String addressDetail;
    private String phoneNumber;
}