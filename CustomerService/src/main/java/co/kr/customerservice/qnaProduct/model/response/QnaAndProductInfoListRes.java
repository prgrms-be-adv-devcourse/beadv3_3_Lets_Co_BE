package co.kr.customerservice.qnaProduct.model.response;

import java.util.List;

public record QnaAndProductInfoListRes(
        List<QnaAndProductInfoRes> items
) {
}
