package co.kr.user.service;

import co.kr.user.model.dto.retrieve.FindIDFirstStepDTO;
import co.kr.user.model.dto.retrieve.FindIDSecondStepReq;
import co.kr.user.model.dto.retrieve.FindPWSecondStepReq;
import co.kr.user.model.dto.retrieve.FindPWFirstStepDTO;

/**
 * 아이디 찾기 및 비밀번호 재설정(Retrieve)을 위한 서비스 인터페이스입니다.
 * 보안을 위해 이메일 인증 절차를 포함한 2단계 프로세스로 구성되어 있습니다.
 */
public interface RetrieveService {

    /**
     * [아이디 찾기 1단계]
     * 사용자가 입력한 이메일로 가입된 계정이 있는지 확인하고, 인증 메일을 발송합니다.
     * * @param mail 사용자가 입력한 이메일 주소
     * @return 1단계 결과 DTO (인증 메일이 발송된 이메일, 인증 만료 시간)
     */
    FindIDFirstStepDTO findIdFirst(String mail);

    /**
     * [아이디 찾기 2단계]
     * 이메일 인증 코드를 검증하고, 인증에 성공하면 아이디를 반환합니다.
     * * @param findIDSecondStepReq 이메일과 인증 코드가 담긴 요청 객체
     * @return 찾은 사용자 아이디 (일부분이 마스킹될 수도 있음)
     */
    String findIdSecond(FindIDSecondStepReq findIDSecondStepReq);

    /**
     * [비밀번호 찾기 1단계]
     * 사용자가 입력한 이메일로 가입된 계정이 있는지 확인하고, 비밀번호 재설정용 인증 메일을 발송합니다.
     * * @param mail 사용자가 입력한 이메일 주소
     * @return 1단계 결과 DTO (인증 메일이 발송된 이메일, 인증 만료 시간)
     */
    FindPWFirstStepDTO findPwFirst(String mail);

    /**
     * [비밀번호 찾기 2단계]
     * 이메일 인증 코드를 검증하고, 새로운 비밀번호로 변경합니다.
     * * @param findPWSecondStepReq 이메일, 인증 코드, 새로운 비밀번호가 담긴 요청 객체
     * @return 처리 결과 메시지 ("비밀번호가 성공적으로 변경되었습니다.")
     */
    String findPwSecond(FindPWSecondStepReq findPWSecondStepReq);
}