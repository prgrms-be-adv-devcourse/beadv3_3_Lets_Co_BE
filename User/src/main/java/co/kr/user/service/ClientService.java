package co.kr.user.service;

import co.kr.user.model.dto.client.BalanceReq;
import co.kr.user.model.dto.client.ClientAddressDTO;
import co.kr.user.model.dto.client.ClientRoleDTO;

public interface ClientService {
    ClientRoleDTO getRole(Long userIdx);

    String balance(Long userIdx, BalanceReq balanceReq);

    ClientAddressDTO defaultAddress(Long userIdx);

    ClientAddressDTO searchAddress(Long userIdx, String addressCode);

    Long defaultCard(Long userIdx);

    Long searchCard(Long userIdx, String cardCode);
}