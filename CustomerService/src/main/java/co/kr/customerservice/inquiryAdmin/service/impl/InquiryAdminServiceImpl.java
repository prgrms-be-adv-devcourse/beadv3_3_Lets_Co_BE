package co.kr.customerservice.inquiryAdmin.service.impl;

import co.kr.customerservice.common.exception.ForbiddenException;
import co.kr.customerservice.common.model.dto.response.ResultResponse;
import co.kr.customerservice.common.model.entity.CustomerServiceDetailEntity;
import co.kr.customerservice.common.model.entity.CustomerServiceEntity;
import co.kr.customerservice.common.model.vo.CustomerServiceStatus;
import co.kr.customerservice.common.model.vo.CustomerServiceType;
import co.kr.customerservice.common.repository.CustomerServiceDetailRepository;
import co.kr.customerservice.common.repository.CustomerServiceRepository;
import co.kr.customerservice.inquiryAdmin.mapper.InquiryMapper;
import co.kr.customerservice.inquiryAdmin.model.dto.InquiryDTO;
import co.kr.customerservice.inquiryAdmin.model.dto.request.InquiryUpsertReq;
import co.kr.customerservice.inquiryAdmin.model.dto.response.InquiryDetailRes;
import co.kr.customerservice.inquiryAdmin.model.dto.response.InquiryListRes;
import co.kr.customerservice.inquiryAdmin.service.InquiryAdminService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InquiryAdminServiceImpl implements InquiryAdminService {

    private final CustomerServiceRepository customerServiceRepository;
    private final CustomerServiceDetailRepository customerServiceDetailRepository;

    @Override
    @Transactional(readOnly = true)
    public InquiryListRes getInquiryList(Pageable pageable){

        Page<CustomerServiceEntity> inquiryPage = customerServiceRepository.findAllByTypeAndIsPrivateFalseAndDelFalse(CustomerServiceType.QNA_ADMIN, pageable);

        List<InquiryDTO> result = inquiryPage.stream()
                .map(entity -> new InquiryDTO(
                        entity.getCode(),
                        entity.getCategory(),
                        entity.getStatus(),
                        entity.getTitle(),
                        entity.getIsPrivate(),
                        entity.getCreatedAt()
                ) )
                .toList();

        return new InquiryListRes(
                result
        );
    }

    // 문의 생성
    @Override
    @Transactional
    public InquiryDetailRes addInquiry(Long userId, InquiryUpsertReq request){
        // 2. 새로운 entity 객체 생성
        CustomerServiceEntity requestEntity = CustomerServiceEntity.builder()
                .code(UUID.randomUUID().toString())
                .type(CustomerServiceType.QNA_ADMIN)
                .category(request.category())
                .status(CustomerServiceStatus.WAITING)
                .title(request.title())
                .isPrivate(request.isPrivate())
                .isPinned(false)
                .usersIdx(userId)
                .username(request.name())
                .build();

        // 2.1 저장
        customerServiceRepository.save(requestEntity);

        // 3. detail 객체 생성
        CustomerServiceDetailEntity requestDetailEntity =CustomerServiceDetailEntity.builder()
                .detailCode(UUID.randomUUID().toString())
                .usersIdx(userId)
                // .Parent_IDX의 경우 해당 엔티티가 부모의 입장이 되므로 생략하는거로
                .userName(request.name())
                // customerService : 위에서 저장을 진행했으므로 idx에 값이 들어감. 이를 받아옴
                .customerService(requestEntity)
                .content(request.content())
                .build();
    
        // 3.1 저장
        customerServiceDetailRepository.save(requestDetailEntity);

        // 4 반환
        return InquiryMapper.toDetailResponse(
                true,
                requestEntity,
                List.of(requestDetailEntity)
        );
    }

    // 문의 상세 보기
    @Override
    @Transactional(readOnly = true)
    public InquiryDetailRes getInquiryDetail(Long userId, String inquiryCode){
        // 1. 엔티티 조회
        CustomerServiceEntity inquiryEntity = customerServiceRepository.findByCodeAndDelFalse(inquiryCode)
                .orElseThrow(() -> new EntityNotFoundException("존재 하지 않는 문의입니다."));
        // 2. 유효성 검사
        // 2.1 작성자인가 확인 후 비밀글인지 확인
        boolean isOwner = Objects.equals(inquiryEntity.getUsersIdx(), userId);
        if(!isOwner && inquiryEntity.getIsPrivate() == true){
            throw new IllegalArgumentException("해당 문의는 비밀 글 입니다.");
        }

        // 2.2 타입이 문의가 아닌경우
        if(inquiryEntity.getType() != CustomerServiceType.QNA_ADMIN){
            throw new IllegalArgumentException("해당 게시글은 문의가 아닙니다.");
        }
        List<CustomerServiceDetailEntity> inquiryDetailEntity = customerServiceDetailRepository.findAllByCustomerServiceAndDelFalse(inquiryEntity);

        // 3. 반환
        return InquiryMapper.toDetailResponse(
                isOwner,
                inquiryEntity,
                inquiryDetailEntity
        );

        
    }

    // 문의 내용 수정
    @Override
    @Transactional
    public InquiryDetailRes updateInquiry(Long userId, String inquiryCode, InquiryUpsertReq request){

        // 1. 엔티티 조회
        CustomerServiceEntity inquiryEntity = customerServiceRepository.findByCodeAndDelFalse(inquiryCode)
                .orElseThrow(() -> new EntityNotFoundException("존재 하지 않는 문의입니다."));

        // 2. 유효성 검사
        // 2.1 작성자인가?
        boolean isOwner = Objects.equals(inquiryEntity.getUsersIdx(), userId);
        if(!isOwner){
            throw new ForbiddenException("해당 문의의 작성자가 아닙니다.");
        }

        // 2.2 타입이 문의 인가?
        if(inquiryEntity.getType() != CustomerServiceType.QNA_ADMIN){
            throw new IllegalArgumentException("해당 게시글은 문의가 아닙니다.");
        }

        // 2.3 관리자가 답변 전이라면 수정 가능
        if (inquiryEntity.getStatus() != CustomerServiceStatus.WAITING){
            throw new IllegalArgumentException("더 이상 수정이 불가능 합니다.");
        }
/*
        CustomerServiceDetailEntity inquiryDetailEntity = customerServiceDetailRepository
                .findByDetailCodeAndDelFalse(request.detailCode())

                .orElseThrow(() -> new IllegalArgumentException("존재 하지 않는 답변입니다."));
        if (inquiryEntity != inquiryDetailEntity.getCustomerService() ){
            throw new IllegalArgumentException("해당 문의에 대한 답변이 아닙니다.");
        }
                */


        List<CustomerServiceDetailEntity> inquiryDetailEntity = customerServiceDetailRepository.findAllByCustomerServiceAndDelFalse(inquiryEntity);

        // 3. 찾은 DetailEntityList 중 수정 할 Entity를 code로 찾기
        CustomerServiceDetailEntity willUpdate = inquiryDetailEntity.stream()
                .filter(detail -> inquiryCode.equals(detail.getDetailCode()))
                .findFirst()  // 첫 번째 발견된 것 가져오기
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 내용입니다."));

        // 4. request 기반 update 진행
        inquiryEntity.update(
                request.category(),
                inquiryEntity.getStatus(), // status는 유저가 바꿀것이 아님.
                request.title(),
                request.isPrivate(),
                inquiryEntity.getIsPinned() // IsPinned 역시 문의에서는 사용 x
        );

        willUpdate.update(request.content());

        // 5. 반환
        return InquiryMapper.toDetailResponse(
                isOwner,
                inquiryEntity,
                inquiryDetailEntity
        );
    }

    // 문의 내용 삭제
    @Override
    @Transactional
    public ResultResponse deleteInquiry(Long userId, String inquiryCode){
        // 1. 엔티티 조회
        CustomerServiceEntity inquiryEntity = customerServiceRepository.findByCodeAndDelFalse(inquiryCode)
                .orElseThrow(() -> new EntityNotFoundException("존재 하지 않는 문의입니다."));

        // 2. 유효성 검사
        if(inquiryEntity.getType() != CustomerServiceType.QNA_ADMIN){
            throw new IllegalArgumentException("해당 게시글은 문의가 아닙니다.");
        }

        if(!Objects.equals(inquiryEntity.getUsersIdx(), userId)){
            throw new ForbiddenException("해당 문의의 작성자가 아닙니다.");
        }

        List<CustomerServiceDetailEntity> inquiryDetailEntity = customerServiceDetailRepository.findAllByCustomerServiceAndDelFalse(inquiryEntity);

        // 3. 삭제 진행
        inquiryEntity.delete();
        // 3.1 아래 답변들까지 삭제처리
        for( CustomerServiceDetailEntity entity:inquiryDetailEntity){
            entity.delete();
        }

        return new ResultResponse(
                "success"
        );

    }

    // 본인 문의 내역 목록 조회
    @Override
    @Transactional
    public InquiryListRes getMyInquiryList(Long userId, Pageable pageable){

        Page<CustomerServiceEntity> inquiryPage = customerServiceRepository.findAllByTypeAndUsersIdxAndDelFalse(CustomerServiceType.QNA_ADMIN,userId ,pageable);

        List<InquiryDTO> result = inquiryPage.stream()
                .map(entity -> new InquiryDTO(
                        entity.getCode(),
                        entity.getCategory(),
                        entity.getStatus(),
                        entity.getTitle(),
                        entity.getIsPrivate(),
                        entity.getCreatedAt()
                ) )
                .toList();

        return new InquiryListRes(
                result
        );
    }
}
