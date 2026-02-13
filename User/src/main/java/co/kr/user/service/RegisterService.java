package co.kr.user.service;

import co.kr.user.model.dto.register.RegisterDTO;
import co.kr.user.model.dto.register.RegisterReq;

public interface RegisterService {
    String checkDuplicate(String id);

    RegisterDTO signup(RegisterReq registerReq);

    String signupAuthentication(String code);
}