package co.kr.user.service;

import co.kr.user.DAO.UserInformationRepository;
import co.kr.user.DAO.UserRepository;
import co.kr.user.model.DTO.myPage.UserProfileResponse;
import co.kr.user.model.DTO.myPage.UserResponse;
import co.kr.user.model.entity.Users;
import co.kr.user.model.entity.Users_Information;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 컨트롤러의 요청을 받아 Repository를 통해 데이터를 처리하고 가공하여 반환
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 기본적으로 조회 성능 최적화를 위해 읽기 전용 트랜잭션 적용
public class UserService {

    private final UserRepository userRepository;
    private final UserInformationRepository userInformationRepository;

    // TODO: [MSA 확장 포인트] 결제/카드 서비스 등 외부 API 호출을 위한 FeignClient가 이곳에 주입될 예정

    /**
     * 마이페이지 기본 정보 조회 로직
     * @param userId 조회할 사용자의 PK
     * @return UserResponse (기본 정보 DTO)
     */
    public UserResponse getMyPageInfo(Long userId) {
        // DB에서 사용자 조회 (없으면 예외 발생)
        Users user = findUserByIdOrThrow(userId);

        // Entity를 DTO로 변환하여 반환
        return UserResponse.from(user);
    }

    /**
     * 상세 개인 정보 조회 로직
     * @param userId 조회할 사용자의 PK
     * @return UserProfileResponse (상세 정보 + 부가 정보 DTO)
     */
    public UserProfileResponse getMyPageDetails(Long userId) {
        // 기본 유저 정보 조회 (로그인 ID, 역할 등 확인용)
        Users user = findUserByIdOrThrow(userId);

        // 상세 유저 정보 조회 (이름, 전화번호 등)
        // UserInformation 테이블에서 해당 유저와 매핑된 정보를 찾음
        Users_Information userInfo = userInformationRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("상세 회원 정보를 찾을 수 없습니다. UserID: " + userId));

        /*
         * [MSA 연동 시나리오]
         * Payment Service 등에 API 요청을 보내 카드가 있는지 확인하는 로직이 여기에 추가
         * List<CardDto> cards = paymentClient.getCards(userId);
         */

        // 조회된 정보들을 조합하여 하나의 응답 DTO로 생성
        return UserProfileResponse.of(user, userInfo /*, cards */);
    }

    /**
     * [내부 헬퍼 메서드]
     * ID로 User를 찾고, 없으면 예외를 던지는 반복 로직을 추출
     */
    private Users findUserByIdOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. UserID: " + userId));
    }
}

