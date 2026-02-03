package co.kr.customerservice.qnaProduct.mapper;

import co.kr.customerservice.common.model.entity.CustomerServiceDetailEntity;
import co.kr.customerservice.common.model.entity.CustomerServiceEntity;
import co.kr.customerservice.qnaProduct.model.QnaProductDetailDTO;
import co.kr.customerservice.qnaProduct.model.QnaProductQuestionDTO;
import co.kr.customerservice.qnaProduct.model.response.QnaProductDetailResponse;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class QnaMapper {

    public static QnaProductDetailResponse toDetail(
            CustomerServiceEntity csEntity,
            List<CustomerServiceDetailEntity> csDetailEntities) {

        Map<Long, String> detailCodeMap = csDetailEntities.stream()
                .collect(Collectors.toMap(
                        CustomerServiceDetailEntity::getDetailIdx,
                        CustomerServiceDetailEntity::getDetailCode
                ));
        return new QnaProductDetailResponse(
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
