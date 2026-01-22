package co.kr.costomerservice.qnaProduct.model;

import org.springframework.cglib.core.Local;

import java.time.LocalDateTime;

public record QnaProductDetailDTO(
    String detailCode,

    String parentCode,
    String content,
    String userName,
    LocalDateTime detailCreatedAt

) {

}
