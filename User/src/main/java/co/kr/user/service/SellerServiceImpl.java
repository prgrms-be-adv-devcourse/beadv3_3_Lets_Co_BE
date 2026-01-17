package co.kr.user.service;

import co.kr.user.model.DTO.seller.SellerRegisterDTO;
import co.kr.user.model.DTO.seller.SellerRegisterReq;

public interface SellerServiceImpl {
    // 1. 메서드명 소문자 시작 (SellerRegister -> sellerRegister)
    // 2. 파라미터 타입 변경 (SellerRegisterDTO -> SellerRegisterReq)
    // 3. 변수명 변경 (user_Idx -> userIdx)
    SellerRegisterDTO sellerRegister(Long userIdx, SellerRegisterReq sellerRegisterReq);

    String sellerRegisterCheck(Long userIdx, String authCode);
}