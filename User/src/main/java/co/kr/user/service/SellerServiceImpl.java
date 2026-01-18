package co.kr.user.service;

import co.kr.user.model.DTO.seller.SellerRegisterDTO;
import co.kr.user.model.DTO.seller.SellerRegisterReq;

public interface SellerServiceImpl {
    SellerRegisterDTO sellerRegister(Long userIdx, SellerRegisterReq sellerRegisterReq);

    String sellerRegisterCheck(Long userIdx, String authCode);
}