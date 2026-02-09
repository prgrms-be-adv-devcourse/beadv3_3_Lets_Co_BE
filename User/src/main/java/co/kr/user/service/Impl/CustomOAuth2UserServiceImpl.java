package co.kr.user.service.Impl;

import co.kr.user.dao.UserInformationRepository;
import co.kr.user.dao.UserRepository;
import co.kr.user.model.entity.Users;
import co.kr.user.model.entity.UsersInformation;
import co.kr.user.model.vo.UsersInformationGender;
import co.kr.user.service.CustomOAuth2UserService;
import co.kr.user.util.AESUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserServiceImpl extends DefaultOAuth2UserService implements CustomOAuth2UserService {

    private final UserRepository userRepository;
    private final UserInformationRepository userInformationRepository;
    private final AESUtil aesUtil;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String emailOrId = "";
        String name = "";
        // 기본값 설정
        String phone = "010-0000-0000";
        String birth = "1900-01-01";
        UsersInformationGender gender = UsersInformationGender.OTHER;

        if ("google".equals(registrationId)) {
            // 1. 구글은 이메일 사용
            emailOrId = (String) attributes.get("email");
            name = (String) attributes.get("name");
        }
        else if ("kakao".equals(registrationId)) {
            // 2. 카카오는 고유 ID를 메일 형식으로 변환하여 사용
            log.info("kakao attributes: {}", attributes.toString());
            emailOrId = attributes.get("id") + "@kakao.com";
            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
            Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
            name = (String) profile.get("nickname");
        }
        else if ("naver".equals(registrationId)) {
            // 3. 네이버는 제공된 이메일을 사용
            Map<String, Object> response = (Map<String, Object>) attributes.get("response");
            emailOrId = (String) response.get("id") + "@naver.com";
            name = (String) response.get("name");
            phone = (String) response.get("mobile");
            birth = (String) response.get("birthyear") + "-" + (String) response.get("birthday");
            gender = convertGender((String) response.get("gender"));
        }

        // 최종 결정된 emailOrId(메일 형식)로 DB 조회
        final String finalId = emailOrId;
        log.info("finalId: {}", finalId);
        Users user = userRepository.findById(finalId)
                .orElseGet(() -> userRepository.save(Users.builder()
                        .id(finalId) // 메일 형식이 ID로 저장됨
                        .pw("OAUTH_USER_" + UUID.randomUUID())
                        .build()));

        user.activateUsers();

        if (userInformationRepository.findByUsersIdx(user.getUsersIdx()).isEmpty()) {
            userInformationRepository.save(UsersInformation.builder()
                    .usersIdx(user.getUsersIdx())
                    .name(aesUtil.encrypt(name))
                    .phoneNumber(aesUtil.encrypt(phone))
                    .birth(aesUtil.encrypt(birth))
                    .gender(gender)
                    .build());
        }

        return oAuth2User;
    }

    private UsersInformationGender convertGender(String g) {
        if (g == null) return UsersInformationGender.OTHER;
        String upperG = g.toUpperCase();
        if (upperG.startsWith("M")) return UsersInformationGender.MALE;
        if (upperG.startsWith("F")) return UsersInformationGender.FEMALE;
        return UsersInformationGender.OTHER;
    }
}