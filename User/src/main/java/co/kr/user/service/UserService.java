package co.kr.user.service;

import co.kr.user.model.dto.my.UserAmendReq;
import co.kr.user.model.dto.my.UserDTO;
import co.kr.user.model.dto.my.UserDeleteDTO;
import co.kr.user.model.dto.my.UserProfileDTO;
import jakarta.servlet.http.HttpServletResponse;

import java.math.BigDecimal;

public interface UserService {
    BigDecimal balance(Long userIdx);

    UserDTO my(Long userIdx);

    UserProfileDTO myDetails(Long userIdx);

    UserDeleteDTO myDelete(Long userIdx);

    String myDelete(Long userIdx, String authCode, HttpServletResponse response);

    UserAmendReq myAmend(Long userIdx, UserAmendReq userAmendReq);
}