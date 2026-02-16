package co.kr.user.service;

import co.kr.user.model.dto.register.RegisterDTO;
import co.kr.user.model.dto.register.RegisterReq;

/**
 * 회원 가입(Registration) 프로세스를 처리하기 위한 서비스 인터페이스입니다.
 * 아이디 중복 체크, 회원 가입 요청, 이메일 인증 확인 등의 기능을 정의합니다.
 */
public interface RegisterService {

    /**
     * 회원 가입 시 아이디 중복 여부를 확인합니다.
     * * @param id 사용자가 입력한 가입 희망 아이디
     * @return 중복 여부에 따른 메시지 ("사용 가능한 아이디입니다" 또는 "이미 사용 중인 아이디입니다")
     */
    String checkDuplicate(String id);

    /**
     * 회원 가입을 수행합니다.
     * 사용자 정보를 저장하고, 이메일 인증을 위한 인증 메일을 발송합니다.
     * * @param registerReq 회원 가입 요청 정보 (아이디, 비밀번호, 이메일, 이름 등)
     * @return 가입 결과 DTO (인증 메일이 발송된 이메일 주소, 인증 만료 시간 등 포함)
     */
    RegisterDTO signup(RegisterReq registerReq);

    /**
     * 이메일로 전송된 인증 코드를 검증하여 회원 가입을 최종 완료(활성화)합니다.
     * * @param code 사용자가 입력한 이메일 인증 코드
     * @return 인증 결과 메시지 ("인증이 완료되었습니다." 등)
     */
    String signupAuthentication(String code);
}