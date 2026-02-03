package co.kr.costomerservice.qnaProduct.service;

import co.kr.costomerservice.common.model.dto.response.ResultResponse;
import co.kr.costomerservice.qnaProduct.model.request.QnaProductUpsertRequest;
import co.kr.costomerservice.qnaProduct.model.response.QnaAndProductInfoListResponse;
import co.kr.costomerservice.qnaProduct.model.response.QnaProductDetailResponse;
import co.kr.costomerservice.qnaProduct.model.response.QnaProductListResponse;
import org.springframework.data.domain.Pageable;

public interface QnaProductService {
    QnaProductListResponse getProductQnaList(String productsCode , Pageable pageable);

    QnaProductDetailResponse getProductQnaDetail(String qnaCode,Long userIdx);

    QnaProductDetailResponse addProductQna(String productsCode,QnaProductUpsertRequest request, Long userIdx);

    QnaProductDetailResponse updateQna(String qnaCode , QnaProductUpsertRequest request, Long userIdx);

    ResultResponse deleteQna(String qnaCode, Long userIdx);

    QnaAndProductInfoListResponse getMyProductQnaList(Long userIdx, Pageable pageable);
}
