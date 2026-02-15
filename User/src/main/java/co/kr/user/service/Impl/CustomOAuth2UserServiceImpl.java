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

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserServiceImpl extends DefaultOAuth2UserService implements CustomOAuth2UserService {
    private final UserRepository userRepository;
    private final UserInformationRepository userInformationRepository;
    private final UserQueryService userQueryService;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String id = "";
        String name = "";
        String phone = "010-0000-0000";
        String birth = "1900-01-01";
        UsersInformationGender gender = UsersInformationGender.OTHER;

        if ("google".equals(registrationId)) {
            id = (String) attributes.get("email");
            name = (String) attributes.get("name");
        }
        else if ("kakao".equals(registrationId)) {
            id = attributes.get("id").toString();
            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
            Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
            name = (String) profile.get("nickname");
        }
        else if ("naver".equals(registrationId)) {
            Map<String, Object> response = (Map<String, Object>) attributes.get("response");
            id = (String) response.get("id");
            name = (String) response.get("name");
            phone = (String) response.get("mobile");
            birth = (String) response.get("birthyear") + "-" + (String) response.get("birthday");
            gender = convertGender((String) response.get("gender"));
        }

        Optional<Users> usersOptional = userRepository.findByIdAndDel(id, UserDel.ACTIVE);

        Users user;
        if (usersOptional.isPresent()) {
            user = usersOptional.get();
        } else {
            user = userRepository.save(Users.builder()
                    .id(id)
                    .pw("OAUTH_USER_" + UUID.randomUUID())
                    .build());

            user.activateUsers();

            UsersInformation usersInformation = userInformationRepository.save(UsersInformation.builder()
                    .usersIdx(user.getUsersIdx())
                    .mail("google".equals(registrationId) ? id : "OAUTH_" + id + "@oauth.com")
                    .name(name)
                    .phoneNumber(phone)
                    .birth(birth)
                    .gender(gender)
                    .build());

            usersInformation.activateInformation();
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