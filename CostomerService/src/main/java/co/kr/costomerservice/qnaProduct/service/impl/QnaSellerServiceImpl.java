package co.kr.costomerservice.qnaProduct.service.impl;


import co.kr.costomerservice.client.AuthServiceClient;
import co.kr.costomerservice.common.entity.CustomerServiceDetailEntity;
import co.kr.costomerservice.common.entity.CustomerServiceEntity;
import co.kr.costomerservice.common.repository.CustomerServiceDetailRepository;
import co.kr.costomerservice.common.repository.CustomerServiceRepository;
import co.kr.costomerservice.common.vo.CustomerServiceStatus;
import co.kr.costomerservice.common.vo.CustomerServiceType;
import co.kr.costomerservice.qnaProduct.mapper.QnaMapper;
import co.kr.costomerservice.qnaProduct.model.request.QnaAnswerUpsertRequest;
import co.kr.costomerservice.qnaProduct.model.response.*;
import co.kr.costomerservice.qnaProduct.service.QnaSellerService;
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
public class QnaSellerServiceImpl implements QnaSellerService {

    private final CustomerServiceRepository customerServiceRepository;
    private final CustomerServiceDetailRepository customerServiceDetailRepository;
    private final AuthServiceClient authServiceClient;


    // 본인상품에 온 모든 문의 조회(상품이 달라도)
    @Override
    @Transactional(readOnly = true)
    public QnaProductForSellerListResponse getMyQnaList(Long userIdx, Pageable pageable){


        // 1. 유저 확인
        String role = authServiceClient.getUserRole(userIdx);
        if (!"SELLER".equals(role) && !"ADMIN".equals(role)) {
            throw new RuntimeException("판매자 권한이 없습니다.");
        }

        // 2. 엔티티 조회
        Page<CustomerServiceEntity> qnaPage = customerServiceRepository.findAllByTypeAndUsersIdxAndDelFalse(CustomerServiceType.QNA_PRODUCT, userIdx, pageable);

        // 3. 상품 정보 조회
        // TODO : 상품 정보 가져와서 넣을것!!!!!!!!!!!!!!!!!
        // 해당 기능의 경우 상품정보 내에서 보이는 문의 내역이 아닌,
        // 판매자가 자신에게 온 모든 문의를 보기에 상품정보가 별도로 필요함 (이름 사진 정도만이라도)
        
        // 4. List로 변환
        List<QnaProductForSellerResponse> result = qnaPage.stream()
                .map(entity -> new QnaProductForSellerResponse(
                        entity.getCode(),
                        entity.getCategory(),
                        entity.getStatus(),
                        entity.getTitle(),
                        entity.getViewCount(),
                        entity.getCreatedAt(),

                        entity.getUserName()

                ) )
                .toList();

        // 5. 반환
        return new QnaProductForSellerListResponse(
                "success",
                result
        );

    }

    @Override
    @Transactional
    public QnaProductDetailResponse addAnswer(String qnaCode,Long userIdx, QnaAnswerUpsertRequest request){


        // 1. entity 가져오기
        CustomerServiceEntity questionEntity = customerServiceRepository.findByCodeAndDelFalse(qnaCode)
                .orElseThrow(() -> new IllegalArgumentException("존재 하지 않는 문의입니다."));

        // 2. 유효성 검사

        // 2.1 상품 문의가 아닌경우
        if(questionEntity.getType() != CustomerServiceType.QNA_PRODUCT){
            throw new IllegalArgumentException("해당 글은 상품문의가 아닙니다.");
        }
        // 2.2 본인 상품이 아닌 경우
        // TODO : 할 거 너무 많다!!! 상품 주인 조회 필요

        // 3. DetailEntity 가져오기
        List<CustomerServiceDetailEntity> qnaDetailEntity = customerServiceDetailRepository.findAllByCustomerServiceAndDelFalse(questionEntity);

        // 3.1 parentIdx 찾기
        CustomerServiceDetailEntity parentEntity = qnaDetailEntity.stream()
                .filter(detail -> request.parentCode().equals(detail.getDetailCode()))
                .findFirst()  // 첫 번째 발견된 것 가져오기
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 내용입니다."));

        // 4. detailEntity 생성
        CustomerServiceDetailEntity requestDetailEntity = CustomerServiceDetailEntity.builder()
                .detailCode(UUID.randomUUID().toString())
                .usersIdx(userIdx)
                .parentIdx(parentEntity.getParentIdx())
                .userName(request.userName())
                .customerService(questionEntity)
                .content(request.content())
                .build();

        // 5. 저장
        customerServiceDetailRepository.save(requestDetailEntity);
        // 5.1 상태 변화
        questionEntity.updateStatus(CustomerServiceStatus.ANSWERED);

        // 6. 반환용 리스트 추가
        qnaDetailEntity.add(requestDetailEntity);

        return QnaMapper.toDetail(
                "success",
                questionEntity,
                qnaDetailEntity
        );
    }

}
