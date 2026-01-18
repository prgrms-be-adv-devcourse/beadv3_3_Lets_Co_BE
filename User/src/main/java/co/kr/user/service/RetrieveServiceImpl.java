package co.kr.user.service;

import co.kr.user.model.DTO.retrieve.FindPWFirstStepReq;
import co.kr.user.model.DTO.retrieve.FindPWSecondStepReq;
import co.kr.user.model.DTO.retrieve.FindPWFirstStepDTO;

public interface RetrieveServiceImpl {
    FindPWFirstStepDTO findPwFirst(FindPWFirstStepReq findPWFirstStepReq);

    String findPwSecond(FindPWSecondStepReq findPWSecondStepReq);
}
