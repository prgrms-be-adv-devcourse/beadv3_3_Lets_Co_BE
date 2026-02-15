package co.kr.user.service;

import co.kr.user.model.dto.my.*;
import jakarta.servlet.http.HttpServletResponse;

import java.math.BigDecimal;

public interface UserService {
    BigDecimal balance(Long userIdx);

    UserDTO my(Long userIdx);

    UserProfileDTO myDetails(Long userIdx);

    UserDeleteDTO myDelete(Long userIdx);

    String myDelete(Long userIdx, String authCode, HttpServletResponse response);

    UserAmendDTO myAmend(Long userIdx, UserAmendReq userAmendReq);
}