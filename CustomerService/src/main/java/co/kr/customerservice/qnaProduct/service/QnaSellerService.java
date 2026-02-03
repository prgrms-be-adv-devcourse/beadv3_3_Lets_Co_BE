package co.kr.customerservice.qnaProduct.service;

import co.kr.customerservice.qnaProduct.model.request.QnaAnswerUpsertReq;
import co.kr.customerservice.qnaProduct.model.response.QnaAndProductInfoListRes;
import co.kr.customerservice.qnaProduct.model.response.QnaProductDetailRes;
import org.springframework.data.domain.Pageable;

public interface QnaSellerService {
    QnaAndProductInfoListRes getMyQnaList(Long userIdx, Pageable pageable);

    QnaProductDetailRes addAnswer(String qnaCode, Long userIdx , QnaAnswerUpsertReq request);
}
