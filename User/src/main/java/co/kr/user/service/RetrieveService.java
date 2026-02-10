package co.kr.user.service;

import co.kr.user.model.dto.retrieve.FindIDFirstStepDTO;
import co.kr.user.model.dto.retrieve.FindIDSecondStepReq;
import co.kr.user.model.dto.retrieve.FindPWSecondStepReq;
import co.kr.user.model.dto.retrieve.FindPWFirstStepDTO;

public interface RetrieveService {
    FindIDFirstStepDTO findIdFirst(String mail);

    String findIdSecond(FindIDSecondStepReq findIDSecondStepReq);

    FindPWFirstStepDTO findPwFirst(String mail);

    String findPwSecond(FindPWSecondStepReq findPWSecondStepReq);
}