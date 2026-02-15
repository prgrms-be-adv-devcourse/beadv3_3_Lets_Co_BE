package co.kr.user.service;

import co.kr.user.model.dto.my.UserDeleteDTO;
import co.kr.user.model.dto.seller.SellerAmendReq;
import co.kr.user.model.dto.seller.SellerProfileDTO;
import co.kr.user.model.dto.seller.SellerRegisterDTO;
import co.kr.user.model.dto.seller.SellerRegisterReq;

/**
 * 판매자(Seller)와 관련된 비즈니스 로직을 정의한 인터페이스입니다.
 * 판매자 등록(입점 신청), 정보 조회, 수정, 탈퇴 등의 기능을 제공합니다.
 */
public interface SellerService {

    /**
     * 일반 회원이 판매자로 전환하기 위해 등록(입점)을 신청합니다.
     * 판매자 정보를 저장하고, 본인 확인을 위한 인증 메일을 발송합니다.
     * * @param userIdx 신청하는 회원의 식별자 (PK)
     * @param sellerRegisterReq 판매자 등록에 필요한 정보(상점명, 사업자번호, 계좌정보 등)
     * @return 등록 신청 결과 DTO (인증 메일 발송 정보 포함)
     */
    SellerRegisterDTO sellerRegister(Long userIdx, SellerRegisterReq sellerRegisterReq);

    /**
     * 판매자 등록을 위한 이메일 인증 코드를 검증하고, 최종적으로 판매자 권한을 부여합니다.
     * * @param userIdx 신청한 회원의 식별자 (PK)
     * @param authCode 이메일로 전송된 인증 코드
     * @return 처리 결과 메시지 ("판매자 등록이 완료되었습니다.")
     */
    String sellerRegisterCheck(Long userIdx, String authCode);

    /**
     * 현재 로그인한 판매자의 프로필 정보를 조회합니다.
     * * @param userIdx 판매자 회원의 식별자 (PK)
     * @return 판매자 프로필 정보 DTO (상점명, 사업자번호, 계좌 정보 등)
     */
    SellerProfileDTO my(Long userIdx);

    /**
     * 판매자 정보를 수정합니다.
     * 상점명이나 정산 계좌 정보 등을 변경할 수 있습니다.
     * * @param userIdx 판매자 회원의 식별자 (PK)
     * @param sellerAmendReq 수정할 판매자 정보가 담긴 요청 객체
     * @return 처리 결과 메시지 ("판매자 정보가 수정되었습니다.")
     */
    String myAmend(Long userIdx, SellerAmendReq sellerAmendReq);

    /**
     * [판매자 탈퇴 1단계]
     * 판매자 자격을 포기하고 탈퇴하기 위해 인증 메일을 요청합니다.
     * * @param userIdx 탈퇴하려는 판매자의 식별자 (PK)
     * @return 탈퇴 요청 결과 DTO (인증 메일 발송 정보 포함)
     */
    UserDeleteDTO myDelete(Long userIdx);

    /**
     * [판매자 탈퇴 2단계]
     * 인증 코드를 검증하여 최종적으로 판매자 자격을 삭제(탈퇴)합니다.
     * 일반 회원 자격은 유지될 수 있습니다.
     * * @param userIdx 탈퇴하려는 판매자의 식별자 (PK)
     * @param authCode 이메일로 전송된 인증 코드
     * @return 처리 결과 메시지 ("판매자 탈퇴가 정상 처리되었습니다.")
     */
    String myDelete(Long userIdx, String authCode);
}