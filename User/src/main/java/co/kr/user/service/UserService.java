package co.kr.user.service;

import co.kr.user.model.dto.my.*;
import jakarta.servlet.http.HttpServletResponse;

import java.math.BigDecimal;

/**
 * 일반 사용자(User)의 마이페이지 관련 기능을 정의한 서비스 인터페이스입니다.
 * 내 정보 조회, 수정, 탈퇴, 잔액 조회 등의 기능을 제공합니다.
 */
public interface UserService {

    /**
     * 사용자의 현재 잔액(포인트/머니)을 조회합니다.
     * * @param userIdx 사용자 식별자 (PK)
     * @return 현재 잔액 (BigDecimal)
     */
    BigDecimal balance(Long userIdx);

    /**
     * 마이페이지 메인에 표시할 사용자의 요약 정보를 조회합니다.
     * 아이디, 등급, 가입일, 잔액 등을 포함합니다.
     * * @param userIdx 사용자 식별자 (PK)
     * @return 사용자 요약 정보 DTO
     */
    UserDTO my(Long userIdx);

    /**
     * 사용자의 상세 프로필 정보를 조회합니다.
     * 이름, 전화번호, 생년월일, 성별, 이메일 등이 포함됩니다.
     * * @param userIdx 사용자 식별자 (PK)
     * @return 사용자 상세 프로필 DTO
     */
    UserProfileDTO myDetails(Long userIdx);

    /**
     * [회원 탈퇴 1단계]
     * 회원 탈퇴를 위해 본인 확인용 이메일 인증 코드를 요청합니다.
     * * @param userIdx 탈퇴하려는 사용자 식별자 (PK)
     * @return 탈퇴 요청 결과 DTO (인증 메일 발송 정보 포함)
     */
    UserDeleteDTO myDelete(Long userIdx);

    /**
     * [회원 탈퇴 2단계]
     * 인증 코드를 검증하여 회원 탈퇴를 최종 완료합니다.
     * 탈퇴 시 로그아웃 처리(쿠키 삭제 등)도 함께 수행될 수 있도록 Response 객체를 받습니다.
     * * @param userIdx 탈퇴하려는 사용자 식별자 (PK)
     * @param authCode 이메일로 전송된 인증 코드
     * @param response 쿠키 삭제 등을 위한 HttpServletResponse 객체
     * @return 처리 결과 메시지 ("회원 탈퇴가 정상 처리되었습니다.")
     */
    String myDelete(Long userIdx, String authCode, HttpServletResponse response);

    /**
     * 사용자의 회원 정보를 수정합니다.
     * 이메일, 성별, 이름, 전화번호, 생년월일 등을 변경할 수 있습니다.
     * * @param userIdx 사용자 식별자 (PK)
     * @param userAmendReq 수정할 정보가 담긴 요청 객체
     * @return 수정된 회원 정보 DTO
     */
    UserAmendDTO myAmend(Long userIdx, UserAmendReq userAmendReq);
}