package co.kr.costomerservice.service.impl;


import co.kr.costomerservice.entity.CustomerServiceDetailEntity;
import co.kr.costomerservice.entity.CustomerServiceEntity;
import co.kr.costomerservice.mapper.NoticeMapper;
import co.kr.costomerservice.model.dto.request.NoticeUpsertRequest;
import co.kr.costomerservice.model.dto.response.AdminNoticeDetailResponse;
import co.kr.costomerservice.model.dto.response.NoticeListResponse;
import co.kr.costomerservice.model.dto.response.NoticeResponse;
import co.kr.costomerservice.model.dto.response.ResultResponse;
import co.kr.costomerservice.repository.CustomerServiceDetailRepository;
import co.kr.costomerservice.repository.CustomerServiceRepository;
import co.kr.costomerservice.service.AdminNoticeService;
import co.kr.costomerservice.vo.CustomerServiceStatus;
import co.kr.costomerservice.vo.CustomerServiceType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static co.kr.costomerservice.mapper.NoticeMapper.toDetailMapper;

@Service
@RequiredArgsConstructor
public class AdminNoticeServiceImpl implements AdminNoticeService {

    private final CustomerServiceRepository customerServiceRepository;

    private final CustomerServiceDetailRepository customerServiceDetailRepository;

    @Override
    @Transactional
    public AdminNoticeDetailResponse addNotice(Long userId, NoticeUpsertRequest request){

        CustomerServiceEntity requestEntity = CustomerServiceEntity.builder()
                .code(UUID.randomUUID().toString())
                .type(CustomerServiceType.NOTICE)
                .category(request.category())
                .status(CustomerServiceStatus.PUBLISHED)
                .title(request.title())
                .isPrivate(request.isPrivate())
                .publishedAt(request.publishedAt())
                .isPinned(request.isPinned())
                .usersIdx(userId)
                .build();

        customerServiceRepository.save(requestEntity);

        CustomerServiceDetailEntity detailEntity = CustomerServiceDetailEntity.builder()
                .detailCode(UUID.randomUUID().toString())
                .usersIdx(userId)
                .customerService(requestEntity)
                .content(request.content())
                .build();


        customerServiceDetailRepository.save(detailEntity);


        return toDetailMapper("success", requestEntity, detailEntity);


    }
    @Override
    public NoticeListResponse getNoticeList(Long userId, Pageable pageable){
        // 관리자 권한 확인!!!!!!!!!!!!!!!!!@!@!@

        Page<CustomerServiceEntity> noticeEntityPage = customerServiceRepository.findAllByTypeAndDelFalse(CustomerServiceType.NOTICE,pageable);

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
    public AdminNoticeDetailResponse getNoticeDetail(Long userId, String noticeCode){

        CustomerServiceEntity serviceEntity = customerServiceRepository.findByCodeAndDelFalse(noticeCode)
                .orElseThrow(() -> new IllegalArgumentException("존재 하지 않는 공지입니다."));
        if(serviceEntity.getType() != CustomerServiceType.NOTICE){
            throw new IllegalArgumentException("해당 게시글은 공지사항이 아닙니다.");
        }
        CustomerServiceDetailEntity detailEntity = customerServiceDetailRepository.findByCustomerServiceAndDelFalse(serviceEntity)
                .orElseThrow(() -> new IllegalArgumentException("존재 하지 않는 공지입니다."));

        return toDetailMapper("success", serviceEntity, detailEntity);
    }

    @Override
    @Transactional
    public AdminNoticeDetailResponse updateNotice(Long userId, String noticeCode,NoticeUpsertRequest request){

        CustomerServiceEntity serviceEntity = customerServiceRepository.findByCodeAndDelFalse(noticeCode)
                .orElseThrow(() -> new IllegalArgumentException("존재 하지 않는 공지입니다."));
        if(serviceEntity.getType() != CustomerServiceType.NOTICE){
            throw new IllegalArgumentException("해당 게시글은 공지사항이 아닙니다.");
        }
        CustomerServiceDetailEntity detailEntity = customerServiceDetailRepository.findByCustomerServiceAndDelFalse(serviceEntity)
                .orElseThrow(() -> new IllegalArgumentException("존재 하지 않는 공지입니다."));

        serviceEntity.update(
                request.category(),
                request.status(),
                request.title(),
                request.isPrivate(),
                request.isPinned()
        );
        detailEntity.update(request.content());

        return toDetailMapper("success", serviceEntity, detailEntity);


    }

    public ResultResponse deleteNotice(Long userId, String noticeCode){
        CustomerServiceEntity serviceEntity = customerServiceRepository.findByCodeAndDelFalse(noticeCode)
                .orElseThrow(() -> new IllegalArgumentException("존재 하지 않는 공지입니다."));

        if(serviceEntity.getType() != CustomerServiceType.NOTICE){
            throw new IllegalArgumentException("해당 게시글은 공지사항이 아닙니다.");
        }
        CustomerServiceDetailEntity detailEntity = customerServiceDetailRepository.findByCustomerServiceAndDelFalse(serviceEntity)
                .orElseThrow(() -> new IllegalArgumentException("존재 하지 않는 공지입니다."));



        serviceEntity.delete();
        detailEntity.delete();
        return new ResultResponse("success");
    }
}
