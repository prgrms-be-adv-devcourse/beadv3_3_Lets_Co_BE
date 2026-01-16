package co.kr.user.service;

import co.kr.user.model.DTO.my.UserDTO;
import co.kr.user.model.DTO.my.UserDeleteDTO;
import co.kr.user.model.DTO.my.UserProfileDTO;

public interface UserServiceImpl {

    UserDTO my(Long user_Idx);

    UserProfileDTO myDetails(Long user_Idx);

    UserDeleteDTO myDelete(Long user_Idx);

    String myDelete(Long user_Idx, String authCode);
}
