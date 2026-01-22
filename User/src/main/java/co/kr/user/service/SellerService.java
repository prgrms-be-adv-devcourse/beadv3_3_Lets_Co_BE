package co.kr.user.service;

import co.kr.user.model.DTO.seller.SellerRegisterDTO;
import co.kr.user.model.DTO.seller.SellerRegisterReq;

/**
 * 판매자(Seller) 등록 및 관리 비즈니스 로직을 정의하는 인터페이스입니다.
 * 일반 회원의 판매자 전환 신청과 최종 등록 승인(인증) 기능을 명세합니다.
 * 구현체: SellerServiceImpl
 */
public interface SellerService {

    /**
     * 판매자 등록 신청 메서드 정의입니다.
     * 판매자 정보를 받아 등록 절차를 시작하고, 본인 확인을 위한 인증 메일을 발송합니다.
     *
     * @param userIdx 사용자 고유 식별자
     * @param sellerRegisterReq 판매자 등록 요청 정보 (사업자 번호, 상점명 등)
     * @return SellerRegisterDTO 등록 신청 결과 및 인증 만료 시간 정보
     */
    SellerRegisterDTO sellerRegister(Long userIdx, SellerRegisterReq sellerRegisterReq);

    /**
     * 판매자 등록 인증 확인 메서드 정의입니다.
     * 이메일로 발송된 인증 코드를 검증하여, 판매자 등록을 최종 승인하고 권한을 변경합니다.
     *
     * @param userIdx 사용자 고유 식별자
     * @param authCode 사용자가 입력한 인증 코드
     * @return 등록 완료 결과 메시지
     */
    String sellerRegisterCheck(Long userIdx, String authCode);
}