package co.kr.user.model.dto.myPage;

import co.kr.user.model.entity.Users;
import co.kr.user.model.entity.Users_Information;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * [회원 상세 정보 응답 DTO]
 * UserController의 getMyPageDetails() 요청에 대한 응답 객체
 * 'User' 테이블의 정보와 'UserInformation' 테이블의 정보를 합쳐서 제공
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileResponse {

    private Long userId;        // 사용자 PK
    private String name;        // 실명
    private String phoneNumber; // 전화번호
    private String birth;       // 생년월일

    // [추후 확장 예정 - MSA] 카드 서비스로부터 받아온 카드 목록이 여기에 포함될 수 있음
    // private List<CardDto> cards;

    private String grade;       // 회원 등급

    /**
     * [Entity -> DTO 변환 메서드]
     * 여러 소스(User 엔티티, UserInformation 엔티티, 추후 카드 정보 등)를 조합하여 하나의 응답을 만듭니다.
     */
    public static UserProfileResponse of(
            Users user, Users_Information userInfo /*, List<CardDto> cards */
    ) {
        return UserProfileResponse.builder()
                .userId(user.getUsersIdx())
                .name(userInfo.getName())
                .phoneNumber(userInfo.getPhoneNumber())
                .birth(userInfo.getBirth())
                // .cards(cards) // 추후 카드 정보 세팅
                .grade("SILVER") // 현재는 로직 없이 고정값 반환 (추후 등급 산정 로직 적용 필요)
                .build();
    }
}



