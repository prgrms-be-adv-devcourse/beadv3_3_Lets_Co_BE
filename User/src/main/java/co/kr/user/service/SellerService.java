package co.kr.user.service;

import co.kr.user.model.dto.my.UserDeleteDTO;
import co.kr.user.model.dto.seller.SellerAmendReq;
import co.kr.user.model.dto.seller.SellerProfileDTO;
import co.kr.user.model.dto.seller.SellerRegisterDTO;
import co.kr.user.model.dto.seller.SellerRegisterReq;

public interface SellerService {
    SellerRegisterDTO sellerRegister(Long userIdx, SellerRegisterReq sellerRegisterReq);

    String sellerRegisterCheck(Long userIdx, String authCode);

    SellerProfileDTO my(Long userIdx);

    String myAmend(Long userIdx, SellerAmendReq sellerAmendReq);

    UserDeleteDTO myDelete(Long userIdx);

    String myDelete(Long userIdx, String authCode);
}