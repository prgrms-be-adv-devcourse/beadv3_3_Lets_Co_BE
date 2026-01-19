package co.kr.costomerservice.qnaProduct.mapper;

import co.kr.costomerservice.common.entity.CustomerServiceDetailEntity;
import co.kr.costomerservice.common.entity.CustomerServiceEntity;
import co.kr.costomerservice.qnaProduct.model.QnaProductDetailDTO;
import co.kr.costomerservice.qnaProduct.model.QnaProductQuestionDTO;
import co.kr.costomerservice.qnaProduct.model.response.QnaProductDetailResponse;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class QnaMapper {

    public static QnaProductDetailResponse toDetail(
            String resultCode,
            CustomerServiceEntity csEntity,
            List<CustomerServiceDetailEntity> csDetailEntities) {

        Map<Long, String> detailCodeMap = csDetailEntities.stream()
                .collect(Collectors.toMap(
                        CustomerServiceDetailEntity::getDetailIdx,
                        CustomerServiceDetailEntity::getDetailCode
                ));
        return new QnaProductDetailResponse(
                resultCode,
                new QnaProductQuestionDTO(
                        csEntity.getCode(),
                        csEntity.getCategory(),
                        csEntity.getStatus(),
                        csEntity.getTitle(),
                        csEntity.getUserName(),
                        csEntity.getViewCount(),
                        csEntity.getCreatedAt(),
                        csEntity.getIsPrivate(),
                        csEntity.getUsersIdx(),
                        csEntity.getProductsIdx()
                ),
                csDetailEntities.stream()
                        .map(entity -> new QnaProductDetailDTO(
                                entity.getDetailCode(),
                                detailCodeMap.get(entity.getDetailIdx()),
                                entity.getContent(),
                                entity.getUserName(),
                                entity.getCreatedAt()
                                )
                        ).toList()
        );

    }
}
