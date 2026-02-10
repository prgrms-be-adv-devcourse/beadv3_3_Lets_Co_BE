package co.kr.user.model.dto.admin;

import co.kr.user.model.dto.address.AddressListDTO;
import co.kr.user.model.dto.address.AddressRequestReq;
import co.kr.user.model.dto.card.CardListDTO;
import co.kr.user.model.dto.card.CardRequestReq;
import co.kr.user.model.vo.UsersInformationGender;
import co.kr.user.model.vo.UsersMembership;
import co.kr.user.model.vo.UsersRole;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class AdminUserDetailDTO {
    private String id;
    private LocalDateTime lockedUntil;
    private UsersRole role;
    private UsersMembership membership;
    private LocalDateTime agreeTermsAt;
    private LocalDateTime agreePrivacyAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String mail;
    private UsersInformationGender gender;
    private BigDecimal balance;
    private String name;
    private String phoneNumber;
    private String birth;
    private List<AddressListDTO> addressListDTO;
    private List<CardListDTO> cardListDTO;
    private LocalDateTime agreeMarketingAt;
}
