package co.kr.user.service;

import co.kr.user.model.DTO.retrieve.RetrieveFirstDTO;
import co.kr.user.model.DTO.retrieve.RetrieveSecondReq;
import co.kr.user.model.DTO.retrieve.RetrieveThirdReq;

public interface RetrieveServiceImpl {

    RetrieveFirstDTO findPwFirst(String ID);

    String findPwSecond(RetrieveSecondReq retrieveSecondReq);

    String findPwThird(RetrieveThirdReq retrieveThirdReq);

}
