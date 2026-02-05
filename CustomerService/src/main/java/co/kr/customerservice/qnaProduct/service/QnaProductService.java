package co.kr.customerservice.qnaProduct.service;

import co.kr.customerservice.common.model.dto.response.ResultResponse;
import co.kr.customerservice.qnaProduct.model.request.QnaProductUpsertReq;
import co.kr.customerservice.qnaProduct.model.response.QnaAndProductInfoListRes;
import co.kr.customerservice.qnaProduct.model.response.QnaProductDetailRes;
import co.kr.customerservice.qnaProduct.model.response.QnaProductListRes;
import org.springframework.data.domain.Pageable;

public interface QnaProductService {
    QnaProductListRes getProductQnaList(String productsCode , Pageable pageable);

    QnaProductDetailRes getProductQnaDetail(String qnaCode, Long userIdx);

    QnaProductDetailRes addProductQna(String productsCode, QnaProductUpsertReq request, Long userIdx);

    QnaProductDetailRes updateQna(String qnaCode , QnaProductUpsertReq request, Long userIdx);

    ResultResponse deleteQna(String qnaCode, Long userIdx);

    QnaAndProductInfoListRes getMyProductQnaList(Long userIdx, Pageable pageable);
}
