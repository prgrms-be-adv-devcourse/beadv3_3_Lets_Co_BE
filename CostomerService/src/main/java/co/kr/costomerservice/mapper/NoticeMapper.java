package co.kr.costomerservice.mapper;

import co.kr.costomerservice.entity.CustomerServiceDetailEntity;
import co.kr.costomerservice.entity.CustomerServiceEntity;
import co.kr.costomerservice.model.dto.response.AdminNoticeDetailResponse;

public class NoticeMapper {

    public static AdminNoticeDetailResponse toDetailMapper(
            String resultCode,
            CustomerServiceEntity csEntity,
            CustomerServiceDetailEntity csDetailEntity
    ){
        return new AdminNoticeDetailResponse(
                resultCode,
                csEntity.getCode(),
                csDetailEntity.getDetailCode(),
                csEntity.getCategory(),
                csEntity.getStatus(),
                csEntity.getTitle(),
                csDetailEntity.getContent(),
                csEntity.getViewCount(),
                csEntity.getIsPinned(),
                csEntity.getPublishedAt(),
                csEntity.getUpdatedAt()
        );
    }
}
