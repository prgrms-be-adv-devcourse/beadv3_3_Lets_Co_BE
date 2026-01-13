package co.kr.costomerservice.service.impl;

import co.kr.costomerservice.entity.CustomerServiceDetailEntity;
import co.kr.costomerservice.entity.CustomerServiceEntity;
import co.kr.costomerservice.model.dto.response.NoticeDetailResponse;
import co.kr.costomerservice.model.dto.response.NoticeListResponse;
import co.kr.costomerservice.model.dto.response.NoticeResponse;
import co.kr.costomerservice.repository.CustomerServiceDetailRepository;
import co.kr.costomerservice.repository.CustomerServiceRepository;
import co.kr.costomerservice.service.UserNoticeService;
import co.kr.costomerservice.vo.CustomerServiceType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserNoticeServiceImpl implements UserNoticeService {

    private final CustomerServiceRepository customerServiceRepository;

    private final CustomerServiceDetailRepository customerServiceDetailRepository;

    @Override
    @Transactional(readOnly = true)
    public NoticeListResponse getNoticeList(Pageable pageable){

        // 1. NOTICE 타입 데이터 검색
        Page<CustomerServiceEntity> noticeEntityPage = customerServiceRepository.findAllByTypeAndDelFalse(CustomerServiceType.NOTICE,pageable);

        // 2. Page > List
        List<NoticeResponse> result = noticeEntityPage.stream()
                .map(doc -> new NoticeResponse(

                        doc.getIdx(),
                        doc.getCode(),
                        doc.getCategory(),
                        doc.getTitle(),
                        doc.getStatus(),
                        doc.getPublishedAt(),
                        doc.getViewCount(),
                        doc.getIsPrivate(),
                        doc.getIsPinned(),
                        doc.getUpdatedAt()

                ))
                .toList();

        return new NoticeListResponse(
                "success",
                result
        );
    }

    @Override
    @Transactional(readOnly = true)
    public NoticeDetailResponse getNoticeDetail(String noticeCode){

        // entity 조회 및 유효성 검사
        CustomerServiceEntity noticeEntity = customerServiceRepository.findByCodeAndDelFalse(noticeCode)
                .orElseThrow(() -> new IllegalArgumentException("존재 하지 않는 공지입니다."));

        if(noticeEntity.getType() != CustomerServiceType.NOTICE){
            throw new IllegalArgumentException("해당 게시글은 공지사항이 아닙니다.");
        }
        CustomerServiceDetailEntity result = customerServiceDetailRepository.findByCustomerServiceAndDelFalse(noticeEntity)
                .orElseThrow(() -> new IllegalArgumentException("존재 하지 않는 공지입니다."));

        // 반환, 한 번 밖에 안 쓰일거 같아서 mapper처리 x
        return new NoticeDetailResponse(
                "success",
                noticeEntity.getCategory(),
                noticeEntity.getTitle(),
                result.getContent(),
                noticeEntity.getViewCount(),
                noticeEntity.getPublishedAt(),
                result.getUpdatedAt()
        );

    }
}
