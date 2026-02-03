package co.kr.customerservice.notice.service.impl;


import co.kr.customerservice.client.AuthServiceClient;
import co.kr.customerservice.common.model.dto.response.ResultResponse;
import co.kr.customerservice.common.model.entity.CustomerServiceDetailEntity;
import co.kr.customerservice.common.model.entity.CustomerServiceEntity;
import co.kr.customerservice.common.model.vo.CustomerServiceStatus;
import co.kr.customerservice.common.model.vo.CustomerServiceType;
import co.kr.customerservice.common.repository.CustomerServiceDetailRepository;
import co.kr.customerservice.common.repository.CustomerServiceRepository;
import co.kr.customerservice.notice.model.dto.request.NoticeUpsertReq;
import co.kr.customerservice.notice.model.dto.response.AdminNoticeDetailRes;
import co.kr.customerservice.notice.model.dto.response.NoticeListRes;
import co.kr.customerservice.notice.model.dto.response.NoticeRes;
import co.kr.customerservice.notice.service.AdminNoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static co.kr.customerservice.notice.mapper.NoticeMapper.toDetailMapper;

@Service
@RequiredArgsConstructor
public class AdminNoticeServiceImpl implements AdminNoticeService {

    private final CustomerServiceRepository customerServiceRepository;

    private final CustomerServiceDetailRepository customerServiceDetailRepository;

    private final AuthServiceClient authServiceClient;


    // 공지 추가
    @Override
    @Transactional
    public AdminNoticeDetailRes addNotice(Long userId, NoticeUpsertReq request){

        String role = authServiceClient.getUserRole(userId).getBody();
        if (!"ADMIN".equals(role)) {
            throw new RuntimeException("판매자 권한이 없습니다.");
        }
        // 1. 받은 request 기반 새로운 CustomerServiceEntity 생성
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
                .username("관리자")
                .build();

        // 2. 생성한 Entity를 DB에 저장
        customerServiceRepository.save(requestEntity);

        
        // 3. 위에 저장 된 CustomerServiceEntity와 request를 통해 상세내용Entity 생성
        // 3.1 위에서 requestEntity를 먼저 save 하지 않으면, 해당 entity 내 idx가 비어있음
        //      따라서 저장 후 idx가 들어간 entity를 가져와서 사용

        CustomerServiceDetailEntity detailEntity = CustomerServiceDetailEntity.builder()
                .detailCode(UUID.randomUUID().toString())
                .usersIdx(userId)
                .userName("관리자")
                .customerService(requestEntity)
                .content(request.content())
                .build();

        // 4. 상세내용 entity 저장
        customerServiceDetailRepository.save(detailEntity);

        // 5. 매퍼를 통해 반환데이터 생성 및 반환
        return toDetailMapper("success", requestEntity, detailEntity);


    }

    // 공지 목록 조회
    @Override
    @Transactional(readOnly = true)
    public NoticeListRes getNoticeList(Long userId, Pageable pageable){

        // 1. 관리자 권한 확인
        String role = authServiceClient.getUserRole(userId).getBody();
        if (!"ADMIN".equals(role)) {
            throw new RuntimeException("판매자 권한이 없습니다.");
        }
        // 2. 목록 조회(Page)
        Page<CustomerServiceEntity> noticeEntityPage = customerServiceRepository.findAllByTypeAndDelFalse(CustomerServiceType.NOTICE,pageable);

        // 3. Page 객체를 List로 변환
        List<NoticeRes> result = noticeEntityPage.stream()
                .map(doc -> new NoticeRes(

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

        return new NoticeListRes(
                "success",
                result
        );

    }

    // 공지 상세 조회
    @Override
    @Transactional(readOnly = true)
    public AdminNoticeDetailRes getNoticeDetail(Long userId, String noticeCode){

        // 0. 관리자 권한 확인
        String role = authServiceClient.getUserRole(userId).getBody();
        if (!"ADMIN".equals(role)) {
            throw new RuntimeException("판매자 권한이 없습니다.");
        }

        // 1. CustomerServiceEntity 조회
        CustomerServiceEntity serviceEntity = customerServiceRepository.findByCodeAndDelFalse(noticeCode)
                .orElseThrow(() -> new IllegalArgumentException("존재 하지 않는 공지입니다."));
        
        // 1.1 공지사항이 맞는지 체크
        if(serviceEntity.getType() != CustomerServiceType.NOTICE){
            throw new IllegalArgumentException("해당 게시글은 공지사항이 아닙니다.");
        }
        
        // 2. CustomerServiceDetailEntity 조회
        CustomerServiceDetailEntity detailEntity = customerServiceDetailRepository.findByCustomerServiceAndDelFalse(serviceEntity)
                .orElseThrow(() -> new IllegalArgumentException("존재 하지 않는 공지입니다."));

        // 3. 매퍼를 통한 반환 데이터 생성 및 반환
        return toDetailMapper("success", serviceEntity, detailEntity);
    }

    // 공지 수정
    @Override
    @Transactional
    public AdminNoticeDetailRes updateNotice(Long userId, String noticeCode, NoticeUpsertReq request){
        // 1. 관리자 권한 확인
        String role = authServiceClient.getUserRole(userId).getBody();
        if (!"ADMIN".equals(role)) {
            throw new RuntimeException("판매자 권한이 없습니다.");
        }
        // 2. 위와 똑같이 Entity 조회 및 유효성 검사
        CustomerServiceEntity serviceEntity = customerServiceRepository.findByCodeAndDelFalse(noticeCode)
                .orElseThrow(() -> new IllegalArgumentException("존재 하지 않는 공지입니다."));
        if(serviceEntity.getType() != CustomerServiceType.NOTICE){
            throw new IllegalArgumentException("해당 게시글은 공지사항이 아닙니다.");
        }
        CustomerServiceDetailEntity detailEntity = customerServiceDetailRepository.findByCustomerServiceAndDelFalse(serviceEntity)
                .orElseThrow(() -> new IllegalArgumentException("존재 하지 않는 공지입니다."));

        // 3. request 기반 update
        serviceEntity.update(
                request.category(),
                request.status(),
                request.title(),
                request.isPrivate(),
                request.isPinned()
        );
        // 3.1 detailEntity에서는 수정할 항목이 content밖에 없음
        detailEntity.update(request.content());

        // 4. 매퍼를 통한 반환 데이터 생성 및 반환
        return toDetailMapper("success", serviceEntity, detailEntity);


    }

    // 공지 삭제
    @Override
    @Transactional
    public ResultResponse deleteNotice(Long userId, String noticeCode){
        // 1. 관리자 권한 확인
        String role = authServiceClient.getUserRole(userId).getBody();
        if (!"ADMIN".equals(role)) {
            throw new RuntimeException("판매자 권한이 없습니다.");
        }

        // 2. Entity 조회 및 유효성 검사

        CustomerServiceEntity serviceEntity = customerServiceRepository.findByCodeAndDelFalse(noticeCode)
                .orElseThrow(() -> new IllegalArgumentException("존재 하지 않는 공지입니다."));

        if(serviceEntity.getType() != CustomerServiceType.NOTICE){
            throw new IllegalArgumentException("해당 게시글은 공지사항이 아닙니다.");
        }
        CustomerServiceDetailEntity detailEntity = customerServiceDetailRepository.findByCustomerServiceAndDelFalse(serviceEntity)
                .orElseThrow(() -> new IllegalArgumentException("존재 하지 않는 공지입니다."));


        // 3. 삭제 진행 (softDelete)
        serviceEntity.delete();
        detailEntity.delete();

        return new ResultResponse("success");
    }
}
