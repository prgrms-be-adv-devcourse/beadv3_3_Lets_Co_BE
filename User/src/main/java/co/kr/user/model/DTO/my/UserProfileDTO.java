package co.kr.user.model.DTO.my;

import lombok.*;

/**
 * [회원 상세 정보 응답 DTO]
 * UserController의 getMyPageDetails() 요청에 대한 응답 객체
 * 'User' 테이블의 정보와 'UserInformation' 테이블의 정보를 합쳐서 제공
 */
@Data
public class UserProfileDTO {

    private String name;        // 실명
    private String phoneNumber; // 전화번호
    private String birth;       // 생년월일

    // [추후 확장 예정 - MSA] 카드 서비스로부터 받아온 카드 목록이 여기에 포함될 수 있음
    // private List<CardDto> cards;

    private String grade;       // 회원 등급
}



