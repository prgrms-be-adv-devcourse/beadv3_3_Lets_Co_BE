package co.kr.costomerservice.qnaProduct.model.response;

import co.kr.costomerservice.qnaProduct.model.QnaProductDetailDTO;
import co.kr.costomerservice.qnaProduct.model.QnaProductQuestionDTO;

import java.util.List;

public record QnaProductDetailResponse(

        QnaProductQuestionDTO questionDTO,

        List<QnaProductDetailDTO> answerDTOs

) {
}
