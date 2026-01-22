package co.kr.user.service;

import co.kr.user.model.DTO.register.RegisterDTO;
import co.kr.user.model.DTO.register.RegisterReq;

/**
 * 회원가입(Sign Up) 관련 비즈니스 로직을 정의하는 인터페이스입니다.
 * 이메일 중복 확인, 가입 신청(인증 메일 발송), 최종 가입 승인 기능을 명세합니다.
 * 구현체: RegisterServiceImpl
 */
public interface RegisterService {

    /**
     * 아이디(이메일) 중복 확인 메서드 정의입니다.
     * 입력된 이메일이 이미 시스템에 등록되어 있는지 확인합니다.
     *
     * @param email 중복 확인할 이메일 주소
     * @return 중복 여부에 따른 결과 메시지 (사용 가능/불가능)
     */
    String checkDuplicate(String email);

    /**
     * 회원가입 신청 메서드 정의입니다.
     * 신규 사용자의 정보를 받아 임시 회원으로 등록하고, 이메일 인증을 위한 코드를 발송합니다.
     *
     * @param registerReq 회원가입 요청 정보 (아이디, 비밀번호, 이름 등)
     * @return RegisterDTO 가입 신청 결과 및 인증 관련 정보
     */
    RegisterDTO signup(RegisterReq registerReq);

    /**
     * 가입 인증 확인 메서드 정의입니다.
     * 이메일로 발송된 인증 코드를 검증하여, 회원 계정을 최종 활성화(정상 회원 전환)합니다.
     *
     * @param code 사용자가 입력한 인증 코드
     * @return 인증 성공 여부 또는 결과 메시지
     */
    String signupAuthentication(String code);
}