package co.kr.costomerservice.qnaProduct.service;

import co.kr.costomerservice.common.response.ResultResponse;
import co.kr.costomerservice.qnaProduct.model.request.QnaProductUpsertRequest;
import co.kr.costomerservice.qnaProduct.model.response.QnaProductDetailResponse;
import co.kr.costomerservice.qnaProduct.model.response.QnaProductListResponse;
import org.springframework.data.domain.Pageable;

public interface QnaProductService {
    QnaProductListResponse getProductQnaList(Long aLong, Pageable pageable);

    QnaProductDetailResponse getProductQnaDetail(String qnaCode,Long userIdx);

    QnaProductDetailResponse addProductQna(QnaProductUpsertRequest request, Long userIdx);

    QnaProductDetailResponse updateQna(String qnaCode , QnaProductUpsertRequest request, Long userIdx);

    ResultResponse deleteQna(String qnaCode, Long userIdx);

    QnaProductListResponse getMyProductQnaList(Long userIdx, Pageable pageable);
}
