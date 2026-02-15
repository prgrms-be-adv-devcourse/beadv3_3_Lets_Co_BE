package co.kr.user.service.Impl;

import co.kr.user.dao.UserInformationRepository;
import co.kr.user.dao.UserRepository;
import co.kr.user.model.entity.Users;
import co.kr.user.model.entity.UsersInformation;
import co.kr.user.model.vo.UserDel;
import co.kr.user.model.vo.UsersInformationGender;
import co.kr.user.service.CustomOAuth2UserService;
import co.kr.user.service.UserQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * CustomOAuth2UserService 인터페이스의 구현체입니다.
 * Spring Security의 DefaultOAuth2UserService를 상속받아 소셜 로그인 후처리 로직을 구현합니다.
 * 구글, 카카오, 네이버 로그인을 지원하며, 회원가입이 안 된 경우 자동 가입을 처리합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserServiceImpl extends DefaultOAuth2UserService implements CustomOAuth2UserService {
    private final UserRepository userRepository;
    private final UserInformationRepository userInformationRepository;
    private final UserQueryService userQueryService;

    /**
     * OAuth2 공급자로부터 사용자 정보를 로드하고, 내부 회원 가입 로직을 수행합니다.
     * @param userRequest OAuth2 요청 정보 (Client Registration, Access Token 등)
     * @return 인증된 OAuth2User 객체
     * @throws OAuth2AuthenticationException 인증 실패 시
     */
    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 부모 클래스의 loadUser를 호출하여 공급자로부터 기본 사용자 정보를 가져옵니다.
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // 어떤 소셜 서비스인지 식별 (google, kakao, naver)
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        // 사용자 정보 속성 맵(Map)을 가져옵니다.
        Map<String, Object> attributes = oAuth2User.getAttributes();

        // 소셜 로그인 정보를 우리 서비스에 맞게 파싱하기 위한 변수들
        String id = "";
        String name = "";
        String phone = "010-0000-0000"; // 기본값 (정보 제공 동의 안 했을 경우 등 대비)
        String birth = "1900-01-01";    // 기본값
        UsersInformationGender gender = UsersInformationGender.OTHER; // 기본값

        // 1. 구글 로그인 처리
        if ("google".equals(registrationId)) {
            id = (String) attributes.get("email"); // 구글은 이메일을 ID로 사용
            name = (String) attributes.get("name");
        }
        // 2. 카카오 로그인 처리
        else if ("kakao".equals(registrationId)) {
            id = attributes.get("id").toString(); // 카카오는 고유 ID(숫자) 사용
            // 카카오 계정 정보 내의 프로필에서 닉네임 추출
            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
            Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
            name = (String) profile.get("nickname");
        }
        // 3. 네이버 로그인 처리
        else if ("naver".equals(registrationId)) {
            // 네이버는 'response' 객체 안에 실제 정보가 담겨 있음
            Map<String, Object> response = (Map<String, Object>) attributes.get("response");
            id = (String) response.get("id");
            name = (String) response.get("name");
            phone = (String) response.get("mobile");
            birth = (String) response.get("birthyear") + "-" + (String) response.get("birthday");
            gender = convertGender((String) response.get("gender"));
        }

        // DB에서 해당 ID로 가입된 활성 회원이 있는지 확인
        Optional<Users> usersOptional = userRepository.findByIdAndDel(id, UserDel.ACTIVE);

        Users user;
        if (usersOptional.isPresent()) {
            // 이미 가입된 회원이면 기존 정보 사용 (로그인 처리)
            user = usersOptional.get();
        } else {
            // 가입되지 않은 회원이면 신규 회원 가입 처리
            // Users 엔티티 생성 및 저장 (비밀번호는 랜덤 UUID로 설정하여 직접 로그인 방지)
            user = userRepository.save(Users.builder()
                    .id(id)
                    .pw("OAUTH_USER_" + UUID.randomUUID())
                    .build());

            user.activateUsers(); // 상태를 ACTIVE로 변경

            // UsersInformation 엔티티 생성 및 저장
            UsersInformation usersInformation = userInformationRepository.save(UsersInformation.builder()
                    .usersIdx(user.getUsersIdx())
                    // 이메일 정보가 없는 경우(카카오 등) 임시 이메일 생성
                    .mail("google".equals(registrationId) ? id : "OAUTH_" + id + "@oauth.com")
                    .name(name)
                    .phoneNumber(phone)
                    .birth(birth)
                    .gender(gender)
                    .build());

            usersInformation.activateInformation(); // 상태를 ACTIVE로 변경
        }

        return oAuth2User; // 인증된 사용자 반환
    }

    /**
     * 성별 문자열(M, F 등)을 내부 Enum 타입으로 변환하는 헬퍼 메서드입니다.
     * @param g 성별 문자열
     * @return UsersInformationGender Enum
     */
    private UsersInformationGender convertGender(String g) {
        if (g == null) return UsersInformationGender.OTHER;
        String upperG = g.toUpperCase();
        if (upperG.startsWith("M")) return UsersInformationGender.MALE;
        if (upperG.startsWith("F")) return UsersInformationGender.FEMALE;
        return UsersInformationGender.OTHER;
    }
}