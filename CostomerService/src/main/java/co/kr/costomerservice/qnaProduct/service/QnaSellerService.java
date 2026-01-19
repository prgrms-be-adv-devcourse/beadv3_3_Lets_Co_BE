package co.kr.costomerservice.qnaProduct.service;

import co.kr.costomerservice.qnaProduct.model.request.QnaAnswerUpsertRequest;
import co.kr.costomerservice.qnaProduct.model.response.QnaProductDetailResponse;
import co.kr.costomerservice.qnaProduct.model.response.QnaProductForSellerListResponse;
import co.kr.costomerservice.qnaProduct.model.response.QnaProductListResponse;
import org.springframework.data.domain.Pageable;

public interface QnaSellerService {
    QnaProductForSellerListResponse getMyQnaList(Long userIdx, Pageable pageable);

    QnaProductDetailResponse addAnswer(String qnaCode, Long userIdx , QnaAnswerUpsertRequest request);
}
