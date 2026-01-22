package co.kr.user.service;

import co.kr.user.model.DTO.retrieve.FindPWFirstStepReq;
import co.kr.user.model.DTO.retrieve.FindPWSecondStepReq;
import co.kr.user.model.DTO.retrieve.FindPWFirstStepDTO;

/**
 * 회원 정보 찾기(Retrieve) 관련 비즈니스 로직을 정의하는 인터페이스입니다.
 * 비밀번호 분실 시 본인 확인(1단계) 및 비밀번호 재설정(2단계) 기능을 명세합니다.
 * 구현체: RetrieveServiceImpl
 */
public interface RetrieveService {

    /**
     * 비밀번호 찾기 1단계: 회원 확인 및 인증번호 발송 메서드 정의입니다.
     * 입력된 회원 정보가 유효한지 확인하고, 등록된 연락처(이메일)로 인증번호를 전송합니다.
     *
     * @param findPWFirstStepReq 1단계 요청 정보 (아이디 등)
     * @return FindPWFirstStepDTO 인증 진행 정보 (인증 만료 시간 등)
     */
    FindPWFirstStepDTO findPwFirst(FindPWFirstStepReq findPWFirstStepReq);

    /**
     * 비밀번호 찾기 2단계: 인증 확인 및 비밀번호 변경 메서드 정의입니다.
     * 인증 코드를 검증하고, 유효한 경우 사용자의 비밀번호를 재설정합니다.
     *
     * @param findPWSecondStepReq 2단계 요청 정보 (인증 코드, 새 비밀번호)
     * @return 비밀번호 변경 완료 메시지
     */
    String findPwSecond(FindPWSecondStepReq findPWSecondStepReq);
}