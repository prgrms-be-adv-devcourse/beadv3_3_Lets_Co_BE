package co.kr.user.model.DTO.my;

import lombok.Data;

/**
 * [회원 상세 정보 응답 DTO]
 * UserController의 getMyPageDetails() 요청에 대한 응답 객체
 * 'User' 테이블의 정보와 'UserInformation' 테이블의 정보를 합쳐서 제공
 */
@Data
public class UserAmendReq {

    private String name;        // 실명
    private String phoneNumber; // 전화번호
    private String birth;       // 생년월일
    private String grade;       // 회원 등급
}



