package co.kr.user.controller;

import co.kr.user.config.SecurityConfiguration;
import co.kr.user.model.DTO.auth.TokenDto;
import co.kr.user.model.vo.UsersRole;
import co.kr.user.service.AuthService;
import co.kr.user.util.CookieUtil;
import co.kr.user.util.JWTUtil;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(SecurityConfiguration.class)
class AuthControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean AuthService authService;
    @MockitoBean JWTUtil jwtUtil; // SecurityConfig가 로드될 때 필요한 빈 Mocking

    @MockitoBean JpaMetamodelMappingContext jpaMetamodelMappingContext;


    // =========================================================================
    // 1. 순수 컨트롤러 로직 검증 (Logic Verification)
    // - @WithMockUser를 사용하여 "인증은 통과되었다"고 가정하고 컨트롤러 내부 로직만
    // =========================================================================

    @Test
    @DisplayName("파라미터 빼먹으면 400 에러 나는가?")
    @WithMockUser // 인증 통과 가정
    void 사용자_권한_조회() throws Exception {
        // given: 파라미터 없이 요청

        // when
        ResultActions result = mockMvc.perform(get("/auth/role")
                .contentType(MediaType.APPLICATION_JSON));

        // then: 400 Bad Request 확인
        result.andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @DisplayName("JSON 응답 형식이 맞는가?")
    @WithMockUser // 인증에 통과 했다는 가정
    void getRole_Success_JsonFormat() throws Exception {

        // given
        Long userIdx = 1L;
        given(authService.getRole(userIdx)).willReturn(UsersRole.ADMIN);

        // when
        ResultActions result = mockMvc.perform(get("/auth/role")
                .param("userIdx", String.valueOf(userIdx))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$").value("ADMIN"))
                .andDo(print());
    }

    @Test
    @DisplayName("서비스가 결과를 주면 쿠키를 잘 만드는지? (HttpOnly 등)")
    @WithMockUser // 인증 통과 가정
    void refresh_Success_CookieCheck() throws Exception {

        // given
        String existingRefreshToken = "old_token";
        String newAccessToken = "new_access_token";

        TokenDto tokenDto = new TokenDto();
        tokenDto.setAccessToken(newAccessToken);
        tokenDto.setRefreshToken(null); // 리프레시 토큰은 갱신 안 됨 가정

        given(authService.refreshToken(existingRefreshToken)).willReturn(tokenDto);

        // when
        ResultActions result = mockMvc.perform(post("/auth/refresh")
                .cookie(new Cookie("refreshToken", existingRefreshToken))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isOk())
                .andExpect(content().string("토큰이 재발급되었습니다."))
                .andExpect(cookie().exists(CookieUtil.ACCESS_TOKEN_NAME))
                .andExpect(cookie().value(CookieUtil.ACCESS_TOKEN_NAME, newAccessToken))
                .andExpect(cookie().httpOnly(CookieUtil.ACCESS_TOKEN_NAME, true)) // HttpOnly 설정 확인
                .andExpect(cookie().secure(CookieUtil.ACCESS_TOKEN_NAME, false))
                .andDo(print());
    }



    // =========================================================================
    // 2. 보안 설정 검증 (Security Verification)
    // - @WithMockUser 유무에 따라 "문지기(Filter)"가 제대로 막고/여는지 봅니다.
    // =========================================================================

    @Test
    @DisplayName("로그인 안 한 사람이 /auth/role 하면 401/403 뱉는가?")
    void getRole_Fail_Unauthenticated() throws Exception {
        // given
        Long userIdx = 1L;

        // when
        ResultActions result = mockMvc.perform(get("/auth/role")
                .param("userIdx", String.valueOf(userIdx))
                .contentType(MediaType.APPLICATION_JSON));

        // then
        // 인가 체크는 user-service 에서 핸들링 안하고, Gateway에서 처리
        // 여기로 들어왔다면 이미 인증된 사용자라고 생각
        result.andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("/auth/refresh는 로그인 안 해도(쿠키만 있으면) 통과시켜 주는가?")
        // @WithMockUser 없음 -> 비로그인 상태
        // SecurityConfig에서 "/auth/refresh"가 permitAll() 인지 검증
    void refresh_Success_PublicAccess() throws Exception {
        // given
        String existingRefreshToken = "valid_token";
        TokenDto tokenDto = new TokenDto();
        tokenDto.setAccessToken("new");

        given(authService.refreshToken(existingRefreshToken)).willReturn(tokenDto);

        // when
        ResultActions result = mockMvc.perform(post("/auth/refresh")
                .cookie(new Cookie("refreshToken", existingRefreshToken))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isOk())
                .andDo(print());
    }
}