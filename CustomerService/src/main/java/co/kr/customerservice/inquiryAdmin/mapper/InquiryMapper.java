package co.kr.customerservice.inquiryAdmin.mapper;

import co.kr.customerservice.common.model.entity.CustomerServiceDetailEntity;
import co.kr.customerservice.common.model.entity.CustomerServiceEntity;
import co.kr.customerservice.inquiryAdmin.model.dto.InquiryDTO;
import co.kr.customerservice.inquiryAdmin.model.dto.InquiryDetailDTO;
import co.kr.customerservice.inquiryAdmin.model.dto.response.InquiryDetailRes;

import java.util.List;

public class InquiryMapper {
    public static InquiryDetailRes toDetailResponse(
            boolean isOwner,
            CustomerServiceEntity csEntity,
            List<CustomerServiceDetailEntity> csDetailEntities
    ){
        return new InquiryDetailRes(
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
