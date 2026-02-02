package co.kr.user.controller;

import co.kr.user.model.dto.my.*;
import co.kr.user.model.vo.UsersRole;
import co.kr.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WithMockUser  // 모든 테스트에 가짜 인증 사용자 부여
@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    @MockitoBean UserService userService;
    @MockitoBean JpaMetamodelMappingContext jpaMetamodelMappingContext;

    Long userIdx = 10L;
    String headerName = "X-USERS-IDX";

    @Test
    @DisplayName("내 정보 조회(기본) - 성공")
    void 내_정보_조회_기본() throws Exception {
        // Given
        UserDTO userDTO = new UserDTO();
        userDTO.setID("testUser123");
        userDTO.setRole(UsersRole.USERS);
        userDTO.setBalance(new BigDecimal("50000"));
        userDTO.setCreatedAt(LocalDateTime.now());

        given(userService.my(userIdx)).willReturn(userDTO);

        // When
        ResultActions resultActions = mvc.perform(
                post("/users/my")
                        .header(headerName, userIdx)
                        .with(csrf())  //
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
        ).andDo(print());

        // Then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("testUser123"))
                .andExpect(jsonPath("$.balance").value(50000));
    }

    @Test
    @DisplayName("내 정보 상세 조회 - 성공")
    void 내_정보_상세_조회() throws Exception {
        // Given
        UserProfileDTO profileDTO = new UserProfileDTO();
        profileDTO.setName("홍길동");
        profileDTO.setPhoneNumber("010-1234-5678");
        profileDTO.setBirth("1990-01-01");
        profileDTO.setGrade("VIP");

        given(userService.myDetails(userIdx)).willReturn(profileDTO);

        // When
        ResultActions resultActions = mvc.perform(
                post("/users/my/details")
                        .header(headerName, userIdx)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
        ).andDo(print());

        // Then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("홍길동"))
                .andExpect(jsonPath("$.phoneNumber").value("010-1234-5678"))
                .andExpect(jsonPath("$.birth").value("1990-01-01"))
                .andExpect(jsonPath("$.grade").value("VIP"));
    }

    @Test
    @DisplayName("회원 탈퇴 요청(1단계) - 성공")
    void 회원_탈퇴_요청_1단계() throws Exception {
        // Given
        UserDeleteDTO deleteDTO = new UserDeleteDTO();
        deleteDTO.setID("testUser123");
        deleteDTO.setCertificationTime(LocalDateTime.now());

        given(userService.myDelete(userIdx)).willReturn(deleteDTO);

        // When
        ResultActions resultActions = mvc.perform(
                post("/users/my/delete")
                        .header(headerName, userIdx)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
        ).andDo(print());

        // Then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("testUser123"));
    }

    @Test
    @DisplayName("회원 탈퇴 확정(2단계) - 성공")
    void 회원_탈퇴_확정_2단계_성공() throws Exception {
        // Given
        UserDeleteSecondStepReq request = new UserDeleteSecondStepReq();
        request.setAuthCode("123456");

        given(userService.myDelete(eq(userIdx), any(String.class), any(HttpServletResponse.class))).willReturn("회원 탈퇴가 정상 처리되었습니다.");

        // When
        ResultActions resultActions = mvc.perform(
                delete("/users/my/delete")
                        .header(headerName, userIdx)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(request)) // @JsonProperty("authCode") 때문에
        ).andDo(print());

        // Then
        resultActions
                .andExpect(status().isOk())
                .andExpect(content().string("회원 탈퇴가 정상 처리되었습니다."));
    }

    @Test
    @DisplayName("회원 탈퇴 확정(2단계) - 실패 (Validation: 인증코드 공백)")
    void 회원_탈퇴_확정_2단계_실패_Validation() throws Exception {
        // Given
        UserDeleteSecondStepReq request = new UserDeleteSecondStepReq();
        request.setAuthCode(""); // @NotBlank 위반

        // When
        ResultActions resultActions = mvc.perform(
                delete("/users/my/delete")
                        .header(headerName, userIdx)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(request))
        ).andDo(print());

        // Then
        resultActions
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("내 정보 수정 - 성공")
    void 내_정보_수정() throws Exception {
        // Given
        UserAmendReq request = new UserAmendReq();
        request.setName("수정된이름");
        request.setPhoneNumber("010-9999-9999");
        request.setBirth("2000-01-01");
        request.setGrade("GOLD");

        given(userService.myAmend(eq(userIdx), any(UserAmendReq.class))).willReturn(request);

        // When
        ResultActions resultActions = mvc.perform(
                put("/users/my/details")
                        .header(headerName, userIdx)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(request))
        ).andDo(print());

        // Then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("수정된이름"))
                .andExpect(jsonPath("$.phoneNumber").value("010-9999-9999"))
                .andExpect(jsonPath("$.birth").value("2000-01-01"))
                .andExpect(jsonPath("$.grade").value("GOLD"));
    }
}