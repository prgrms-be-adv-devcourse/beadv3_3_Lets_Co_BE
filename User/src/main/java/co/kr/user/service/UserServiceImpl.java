package co.kr.user.service;

import co.kr.user.model.DTO.my.UserAmendReq;
import co.kr.user.model.DTO.my.UserDTO;
import co.kr.user.model.DTO.my.UserDeleteDTO;
import co.kr.user.model.DTO.my.UserProfileDTO;

public interface UserServiceImpl {
    UserDTO my(Long userIdx);

    UserProfileDTO myDetails(Long userIdx);

    UserDeleteDTO myDelete(Long userIdx);

    String myDelete(Long userIdx, String authCode);

    UserAmendReq myAmend(Long userIdx, UserAmendReq userAmendReq);
}