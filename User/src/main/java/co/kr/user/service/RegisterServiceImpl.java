package co.kr.user.service;

import co.kr.user.model.DTO.register.RegisterDTO;
import co.kr.user.model.DTO.register.RegisterReq;

/**
 * [회원가입 서비스 인터페이스]
 * 회원가입 프로세스와 관련된 핵심 비즈니스 로직의 설계도입니다.
 * 구현체(RegisterService)가 반드시 수행해야 할 기능들을 명시합니다.
 *
 * * 인터페이스 활용의 이점:
 * 1. [가독성] 구현부의 복잡한 코드를 보지 않고도, 서비스가 어떤 기능을 제공하는지 한눈에 파악할 수 있습니다.
 * 2. [유연성] 추후 비즈니스 로직이 변경되거나 다른 구현체로 교체되더라도, 이를 호출하는 컨트롤러 코드는 수정할 필요가 없습니다.
 */
public interface RegisterServiceImpl {

    /**
     * [이메일 중복 확인]
     * 사용자가 가입하려는 이메일(ID)이 이미 데이터베이스에 존재하는지 검사합니다.
     *
     * @param email 중복 여부를 확인할 이메일 주소
     * @return 중복 여부에 따른 결과 메시지 문자열 (예: "사용 가능한 이메일입니다.")
     */
    String checkDuplicate(String email);

    /**
     * [회원가입 요청 처리]
     * 클라이언트로부터 받은 회원 정보를 검증하고, 암호화하여 DB에 저장한 뒤 인증 메일을 발송합니다.
     *
     * @param registerReq 회원가입 요청 데이터 (아이디, 비밀번호, 이름, 연락처, 생년월일 등)
     * @return 가입 완료된 회원의 ID와 인증 코드 만료 시간을 담은 응답 객체(RegisterDTO)
     */
    RegisterDTO signup(RegisterReq registerReq);

    /**
     * [이메일 인증 코드 검증]
     * 사용자가 이메일로 수신한 인증 코드를 입력하면, 해당 코드가 유효한지 확인하고 인증 상태를 변경합니다.
     *
     * @param code 사용자가 입력한 인증 코드
     * @return 인증 성공/실패 또는 만료 여부에 대한 결과 메시지
     */
    String signupAuthentication(String code);

}