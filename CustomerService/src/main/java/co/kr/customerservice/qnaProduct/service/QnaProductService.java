package co.kr.customerservice.qnaProduct.service;

import co.kr.customerservice.common.model.dto.response.ResultResponse;
import co.kr.customerservice.qnaProduct.model.request.QnaProductUpsertRequest;
import co.kr.customerservice.qnaProduct.model.response.QnaAndProductInfoListResponse;
import co.kr.customerservice.qnaProduct.model.response.QnaProductDetailResponse;
import co.kr.customerservice.qnaProduct.model.response.QnaProductListResponse;
import org.springframework.data.domain.Pageable;

public interface QnaProductService {
    QnaProductListResponse getProductQnaList(String productsCode , Pageable pageable);

    QnaProductDetailResponse getProductQnaDetail(String qnaCode,Long userIdx);

    QnaProductDetailResponse addProductQna(String productsCode,QnaProductUpsertRequest request, Long userIdx);

    QnaProductDetailResponse updateQna(String qnaCode , QnaProductUpsertRequest request, Long userIdx);

    ResultResponse deleteQna(String qnaCode, Long userIdx);

    QnaAndProductInfoListResponse getMyProductQnaList(Long userIdx, Pageable pageable);
}
