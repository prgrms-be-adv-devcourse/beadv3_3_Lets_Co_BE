package co.kr.customerservice.qnaProduct.model.response;

import co.kr.customerservice.qnaProduct.model.QnaProductDetailDTO;
import co.kr.customerservice.qnaProduct.model.QnaProductQuestionDTO;

import java.util.List;

public record QnaProductDetailResponse(

        QnaProductQuestionDTO questionDTO,

        List<QnaProductDetailDTO> answerDTOs

) {
}
