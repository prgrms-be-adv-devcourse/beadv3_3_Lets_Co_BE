package co.kr.costomerservice.inquiryAdmin.service.impl;

import co.kr.costomerservice.client.AuthServiceClient;
import co.kr.costomerservice.common.model.entity.CustomerServiceDetailEntity;
import co.kr.costomerservice.common.model.entity.CustomerServiceEntity;
import co.kr.costomerservice.common.repository.CustomerServiceDetailRepository;
import co.kr.costomerservice.common.repository.CustomerServiceRepository;
import co.kr.costomerservice.common.model.dto.response.ResultResponse;
import co.kr.costomerservice.common.model.vo.CustomerServiceStatus;
import co.kr.costomerservice.common.model.vo.CustomerServiceType;
import co.kr.costomerservice.inquiryAdmin.model.dto.InquiryDTO;
import co.kr.costomerservice.inquiryAdmin.model.dto.request.InquiryAnswerDeleteRequest;
import co.kr.costomerservice.inquiryAdmin.model.dto.request.InquiryAnswerUpsertRequest;
import co.kr.costomerservice.inquiryAdmin.model.dto.request.InquiryUpsertRequest;
import co.kr.costomerservice.inquiryAdmin.model.dto.response.InquiryDetailResponse;
import co.kr.costomerservice.inquiryAdmin.model.dto.response.InquiryListResponse;
import co.kr.costomerservice.inquiryAdmin.mapper.InquiryMapper;
import co.kr.costomerservice.inquiryAdmin.service.InquiryAdminManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class InquiryAdminManagementServiceImpl implements InquiryAdminManagementService {


    private final CustomerServiceRepository customerServiceRepository;
    private final CustomerServiceDetailRepository customerServiceDetailRepository;
    private final AuthServiceClient authServiceClient;

    // 문의 목록 조회
    @Override
    @Transactional(readOnly = true)
    public InquiryListResponse getInquiryList(Pageable pageable, Long usersIdx){

        // 1. 관리자 권환 확인
        String role = authServiceClient.getUserRole(usersIdx).getBody();
        if (!"ADMIN".equals(role)) {
            throw new RuntimeException("관리자 권한이 없습니다.");
        }

        // 2. 엔티티 조회
        Page<CustomerServiceEntity> inquiryPage = customerServiceRepository.findAllByTypeAndDelFalse(CustomerServiceType.QNA_ADMIN, pageable);

        // 3. page > List
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

        // 4, 반환
        return new InquiryListResponse(
                "success",
                result
        );
    }


    // 문의 답변 추가
    @Override
    @Transactional
    public InquiryDetailResponse addInquiryAnswer(Long userId, String inquiryCode, InquiryAnswerUpsertRequest request) {

        //1. 권한 확인
        String role = authServiceClient.getUserRole(userId).getBody();
        if (!"ADMIN".equals(role)) {
            throw new RuntimeException("관리자 권한이 없습니다.");
        }
        // 2. entity 조회
        CustomerServiceEntity foundInquiry = customerServiceRepository.findByCodeAndDelFalse(inquiryCode)
                .orElseThrow(() -> new IllegalArgumentException("존재 하지 않는 문의입니다."));

        // 2.1 detail Entity 조회
        List<CustomerServiceDetailEntity> foundInquiryDetailList = customerServiceDetailRepository.findAllByCustomerServiceAndDelFalse(foundInquiry);

        CustomerServiceDetailEntity foundInquiryDetail = foundInquiryDetailList.stream()
                .filter(detail -> request.detailCode().equals(detail.getDetailCode()))
                .findFirst()  // 첫 번째 발견된 것 가져오기
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 내용입니다."));

        // 3. detail 객체 생성
        CustomerServiceDetailEntity requestDetailEntity = CustomerServiceDetailEntity.builder()
                .detailCode(UUID.randomUUID().toString())
                .usersIdx(userId)
                
                // 3.1 부모의 idx 가져옴
                .parentIdx(foundInquiryDetail.getParentIdx())
                .customerService(foundInquiry)
                .content(request.content())
                .build();

        // 4. entity 저장 및 반환용 리스트에 추가
        customerServiceDetailRepository.save(requestDetailEntity);

        foundInquiryDetailList.add(requestDetailEntity);

        // 5. Status 업데이트
        foundInquiry.updateStatus(CustomerServiceStatus.ANSWERED);

        // 6. 문의 상세 내용 반환
        return InquiryMapper.toDetailResponse(
                "success",
                true,
                foundInquiry,
                foundInquiryDetailList
        );
    }

    // 문의 답변 삭제
    @Override
    @Transactional
    public ResultResponse deleteInquiryAnswer(String inquiryCode, InquiryAnswerDeleteRequest request, Long usersIdx){

        // 1. 관리자 권환 확인
        String role = authServiceClient.getUserRole(usersIdx).getBody();
        if (!"ADMIN".equals(role)) {
            throw new RuntimeException("관리자 권한이 없습니다.");
        }

        // 엔티티 조회
        CustomerServiceDetailEntity byDetailCodeAndDelFalse = customerServiceDetailRepository.findByDetailCodeAndDelFalse(request.detailCode())
                .orElseThrow(() -> new IllegalArgumentException("존재 하지 않는 답변입니다."));

        byDetailCodeAndDelFalse.delete();

        return new ResultResponse("success");

    }








    // 문의 내용 수정
    @Override
    @Transactional
    public InquiryDetailResponse updateInquiry(String inquiryCode, InquiryUpsertRequest request, Long usersIdx){

        // 1. 관리자 권환 확인
        String role = authServiceClient.getUserRole(usersIdx).getBody();
        if (!"ADMIN".equals(role)) {
            throw new RuntimeException("관리자 권한이 없습니다.");
        }

        // 2. 엔티티 조회'및 유효성 검사
        CustomerServiceEntity inquiryEntity = customerServiceRepository.findByCodeAndDelFalse(inquiryCode)
                .orElseThrow(() -> new IllegalArgumentException("존재 하지 않는 문의입니다."));

        if(inquiryEntity.getType() != CustomerServiceType.QNA_ADMIN){
            throw new IllegalArgumentException("해당 게시글은 문의가 아닙니다.");
        }

        // 2.1 detail entity 조회
        List<CustomerServiceDetailEntity> inquiryDetailEntity = customerServiceDetailRepository.findAllByCustomerServiceAndDelFalse(inquiryEntity);

        // 찾은 DetailEntityList 중 수정 할 Entity를 code로 찾기
        CustomerServiceDetailEntity willUpdate = inquiryDetailEntity.stream()
                .filter(detail -> inquiryCode.equals(detail.getDetailCode()))
                .findFirst()  // 첫 번째 발견된 것 가져오기
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 내용입니다."));

        // 3. request 기반 update 진행
        inquiryEntity.update(
                request.category(),
                inquiryEntity.getStatus(), // status는 유저가 바꿀것이 아님.
                request.title(),
                request.isPrivate(),
                inquiryEntity.getIsPinned() // IsPinned 역시 문의에서는 사용 x
        );

        willUpdate.update(request.content());

        // 4. 반환
        return InquiryMapper.toDetailResponse(
                "success",
                true,
                inquiryEntity,
                inquiryDetailEntity
        );
    }

    // 문의 내용 삭제
    @Override
    @Transactional
    public ResultResponse deleteInquiry(String inquiryCode, Long usersIdx){

        // 1. 관리자 권환 확인
        String role = authServiceClient.getUserRole(usersIdx).getBody();
        if (!"ADMIN".equals(role)) {
            throw new RuntimeException("관리자 권한이 없습니다.");
        }


        // 2. 엔티티 조회'및 유효성 검사
        CustomerServiceEntity inquiryEntity = customerServiceRepository.findByCodeAndDelFalse(inquiryCode)
                .orElseThrow(() -> new IllegalArgumentException("존재 하지 않는 문의입니다."));

        if(inquiryEntity.getType() != CustomerServiceType.QNA_ADMIN){
            throw new IllegalArgumentException("해당 게시글은 문의가 아닙니다.");
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



}
