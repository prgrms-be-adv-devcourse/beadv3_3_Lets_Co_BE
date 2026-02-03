package co.kr.customerservice.qnaProduct.service;

import co.kr.customerservice.qnaProduct.model.request.QnaAnswerUpsertRequest;
import co.kr.customerservice.qnaProduct.model.response.QnaAndProductInfoListResponse;
import co.kr.customerservice.qnaProduct.model.response.QnaProductDetailResponse;
import org.springframework.data.domain.Pageable;

public interface QnaSellerService {
    QnaAndProductInfoListResponse getMyQnaList(Long userIdx, Pageable pageable);

    QnaProductDetailResponse addAnswer(String qnaCode, Long userIdx , QnaAnswerUpsertRequest request);
}
