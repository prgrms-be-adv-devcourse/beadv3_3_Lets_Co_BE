package co.kr.user.model.dto.address;

import lombok.Data;

@Data
public class AddressRequestReq {
    private String addressCode;
    private boolean defaultAddress;
    private String recipient;
    private String address;
    private String addressDetail;
    private String phoneNumber;
}