package co.kr.user.service;

import co.kr.user.model.DTO.my.UserAmendReq;
import co.kr.user.model.DTO.my.UserDTO;
import co.kr.user.model.DTO.my.UserDeleteDTO;
import co.kr.user.model.DTO.my.UserProfileDTO;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 회원 정보 관리(마이페이지) 관련 비즈니스 로직을 정의하는 인터페이스입니다.
 * 내 정보 조회, 상세 프로필 확인, 회원 탈퇴 프로세스, 개인정보 수정 기능을 명세합니다.
 * 구현체: UserServiceImpl
 */
public interface UserService {

    /**
     * 내 정보 조회(기본 정보) 메서드 정의입니다.
     * 로그인한 사용자의 기본적인 정보(아이디, 권한, 잔액 등)를 조회하여 반환합니다.
     *
     * @param userIdx 사용자 고유 식별자 (로그인된 사용자)
     * @return UserDTO 기본 회원 정보 객체
     */
    UserDTO my(Long userIdx);

    /**
     * 내 상세 정보 조회 메서드 정의입니다.
     * 사용자의 민감한 정보(이름, 연락처, 생년월일 등)를 포함한 상세 프로필을 조회합니다.
     * 구현체에서는 데이터 복호화 로직이 포함되어야 합니다.
     *
     * @param userIdx 사용자 고유 식별자
     * @return UserProfileDTO 상세 회원 정보 객체
     */
    UserProfileDTO myDetails(Long userIdx);

    /**
     * 회원 탈퇴 요청(1단계) 메서드 정의입니다.
     * 탈퇴 프로세스를 시작하며, 본인 확인을 위한 인증 번호 발송 등의 로직을 수행합니다.
     *
     * @param userIdx 사용자 고유 식별자
     * @return UserDeleteDTO 탈퇴 요청 결과 및 인증 만료 시간 정보
     */
    UserDeleteDTO myDelete(Long userIdx);

    /**
     * 회원 탈퇴 확정(2단계) 메서드 정의입니다.
     * 1단계에서 발송된 인증 코드를 검증하고, 최종적으로 회원을 탈퇴(삭제/비활성화) 처리합니다.
     *
     * @param userIdx 사용자 고유 식별자
     * @param authCode 사용자가 입력한 인증 코드
     * @return 탈퇴 처리 결과 메시지
     */
    String myDelete(Long userIdx, String authCode, HttpServletResponse response);

    /**
     * 회원 정보 수정 메서드 정의입니다.
     * 사용자의 개인정보(이름, 전화번호 등)를 수정합니다.
     * 구현체에서는 정보 암호화 및 부분 수정 로직이 포함되어야 합니다.
     *
     * @param userIdx 사용자 고유 식별자
     * @param userAmendReq 수정할 정보가 담긴 요청 객체
     * @return UserAmendReq 수정이 반영된 정보 객체
     */
    UserAmendReq myAmend(Long userIdx, UserAmendReq userAmendReq);
}