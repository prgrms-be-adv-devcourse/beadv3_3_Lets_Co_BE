package co.kr.customerservice.qnaProduct.model;

import java.time.LocalDateTime;

public record QnaProductDetailDTO(
    String detailCode,

    String parentCode,
    String content,
    String userName,
    LocalDateTime detailCreatedAt

) {

}
