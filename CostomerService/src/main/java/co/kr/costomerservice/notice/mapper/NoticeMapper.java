package co.kr.costomerservice.notice.mapper;

import co.kr.costomerservice.common.entity.CustomerServiceDetailEntity;
import co.kr.costomerservice.common.entity.CustomerServiceEntity;
import co.kr.costomerservice.notice.model.dto.response.AdminNoticeDetailResponse;

public class NoticeMapper {

    /**
     * 3개의 입력으로 공지 상세 정보를 반환해주는 ResponseDTO 생성
     * @param resultCode
     * @param csEntity
     * @param csDetailEntity
     * @return AdminNoticeDetailResponse
     */
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
