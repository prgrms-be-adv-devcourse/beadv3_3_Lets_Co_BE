package co.kr.user.controller;

import co.kr.user.model.dto.auth.AuthenticationReq;
import co.kr.user.model.dto.seller.SellerRegisterDTO;
import co.kr.user.model.dto.seller.SellerRegisterReq;
import co.kr.user.service.SellerService;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SellerController.class)
@WithMockUser
class SellerControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    // JPA Auditing 등 설정 충돌 방지를 위한 Mock
    @MockitoBean JpaMetamodelMappingContext jpaMetamodelMappingContext;
    @MockitoBean SellerService sellerService;


    Long userIdx = 1004L;
    String headerName = "X-USERS-IDX";


    @Test
    @DisplayName("판매자 등록 신청 - 성공")
    void 판매자_등록_신청_성공() throws Exception {

        // Given
        SellerRegisterReq request = new SellerRegisterReq();
        request.setBusinessLicense("123-45-67890");
        request.setBankBrand("국민은행");
        request.setBankName("홍길동");
        request.setBankToken("111-222-333333");

        SellerRegisterDTO responseDTO = new SellerRegisterDTO();
        responseDTO.setID("testUser");
        responseDTO.setCertificationTime(LocalDateTime.now());

        given(sellerService.sellerRegister(eq(userIdx), any(SellerRegisterReq.class)))
                .willReturn(responseDTO);

        // When
        ResultActions resultActions = mvc.perform(
                post("/seller/users/register")
                        .header(headerName, userIdx)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(request))
        ).andDo(print());

        // Then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("testUser"))
                .andExpect(jsonPath("$.certificationTime").exists());
    }

    @Test
    @DisplayName("판매자 등록 신청 - 실패 (유효성 검증: 사업자번호 형식)")
    void 판매자_등록_신청_실패_Validation() throws Exception {

        // Given
        SellerRegisterReq request = new SellerRegisterReq();
        request.setBusinessLicense("1234567890"); // 형식이 틀림 (하이픈 없음)
        request.setBankBrand("국민은행");
        request.setBankName("홍길동");
        request.setBankToken("111-222");

        // When
        ResultActions resultActions = mvc.perform(
                post("/seller/users/register")
                        .header(headerName, userIdx)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(request))
        ).andDo(print());

        // Then
        resultActions
                .andExpect(status().isBadRequest());
    }

    /**
     * 2. 판매자 등록 확인 (인증)
     * POST /seller/users/register/check
     */
    @Test
    @DisplayName("판매자 등록 확인(인증) - 성공")
    void 판매자_등록_확인_성공() throws Exception {

        // Given
        AuthenticationReq request = new AuthenticationReq();
        request.setCode("123456");

        given(sellerService.sellerRegisterCheck(eq(userIdx), any(String.class)))
                .willReturn("판매자 등록 완료");

        // When
        ResultActions resultActions = mvc.perform(
                post("/seller/users/register/check")
                        .header(headerName, userIdx)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(request))
        ).andDo(print());

        // Then
        resultActions
                .andExpect(status().isOk())
                .andExpect(content().string("판매자 등록 완료"));
    }

    @Test
    @DisplayName("판매자 등록 확인(인증) - 실패 (인증코드 공백)")
    void 판매자_등록_확인_실패_Validation() throws Exception {

        // Given
        AuthenticationReq request = new AuthenticationReq();
        request.setCode(""); // @NotBlank 위반

        // When
        ResultActions resultActions = mvc.perform(
                post("/seller/users/register/check")
                        .header(headerName, userIdx)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(request))
        ).andDo(print());

        // Then
        resultActions
                .andExpect(status().isBadRequest());
    }
}