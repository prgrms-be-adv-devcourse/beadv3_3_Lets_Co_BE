package co.kr.costomerservice.inquiryAdmin.mapper;

import co.kr.costomerservice.common.entity.CustomerServiceDetailEntity;
import co.kr.costomerservice.common.entity.CustomerServiceEntity;
import co.kr.costomerservice.inquiryAdmin.dto.InquiryDTO;
import co.kr.costomerservice.inquiryAdmin.dto.InquiryDetailDTO;
import co.kr.costomerservice.inquiryAdmin.dto.response.InquiryDetailResponse;

import java.util.List;

public class InquiryMapper {
    public static InquiryDetailResponse toDetailResponse(
            String resultCode,
            boolean isOwner,
            CustomerServiceEntity csEntity,
            List<CustomerServiceDetailEntity> csDetailEntities
    ){
        return new InquiryDetailResponse(
                resultCode,
                isOwner,
                new InquiryDTO(
                        csEntity.getCode(),
                        csEntity.getCategory(),
                        csEntity.getStatus(),
                        csEntity.getTitle(),
                        csEntity.getIsPrivate(),
                        csEntity.getCreatedAt()

                ),
                csDetailEntities.stream()
                        .map( entity -> new InquiryDetailDTO(
                                entity.getDetailCode(),
                                entity.getContent(),
                                entity.getCreatedAt()
                        ) ).toList()
        );
    }
}
