package co.kr.costomerservice.qnaProduct.service.impl;


import co.kr.costomerservice.client.ProductServiceClient;
import co.kr.costomerservice.common.dto.request.ProductIdxsRequest;
import co.kr.costomerservice.common.dto.response.ProductInfoResponse;
import co.kr.costomerservice.common.entity.CustomerServiceDetailEntity;
import co.kr.costomerservice.common.entity.CustomerServiceEntity;
import co.kr.costomerservice.common.repository.CustomerServiceDetailRepository;
import co.kr.costomerservice.common.repository.CustomerServiceRepository;
import co.kr.costomerservice.common.dto.response.ResultResponse;
import co.kr.costomerservice.common.vo.CustomerServiceStatus;
import co.kr.costomerservice.common.vo.CustomerServiceType;
import co.kr.costomerservice.qnaProduct.mapper.QnaMapper;
import co.kr.costomerservice.qnaProduct.model.request.QnaProductUpsertRequest;
import co.kr.costomerservice.qnaProduct.model.response.*;
import co.kr.costomerservice.qnaProduct.service.QnaProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QnaProductServiceImpl implements QnaProductService {

    private final CustomerServiceRepository customerServiceRepository;
    private final CustomerServiceDetailRepository customerServiceDetailRepository;
    private final ProductServiceClient productServiceClient;

    @Override
    @Transactional(readOnly = true)
    public QnaProductListResponse getProductQnaList(Long productsIDX, Pageable pageable) {


        Page<CustomerServiceEntity> qnaPage = customerServiceRepository.findAllByTypeAndProductsIdxAndIsPrivateFalseAndDelFalse(CustomerServiceType.QNA_PRODUCT,productsIDX ,pageable);

        List<QnaProductResponse> result = qnaPage.stream()
                .map(entity -> new QnaProductResponse(
                        entity.getCode(),
                        entity.getCategory(),
                        entity.getStatus(),
                        entity.getTitle(),
                        entity.getViewCount(),
                        entity.getCreatedAt(),

                        entity.getUserName()

                ) )
                .toList();

        return new QnaProductListResponse(
                "success",
                result
        );

    }

    @Override
    @Transactional(readOnly = true)
    public QnaProductDetailResponse getProductQnaDetail(String qnaCode, Long userIdx){

        // 1. entity 가져오기
        CustomerServiceEntity questionEntity = customerServiceRepository.findByCodeAndDelFalse(qnaCode)
                .orElseThrow(() -> new IllegalArgumentException("존재 하지 않는 문의입니다."));

        // 2. 유효성 검사
        // 2.1 해당 글이 비밀글인데 본인이 아닌경우
        if (questionEntity.getIsPrivate() == true && !Objects.equals(questionEntity.getUsersIdx(), userIdx)){
            throw new IllegalArgumentException("해당 문의는 비밀 글 입니다.");
        }
        // 2.2 상품 문의가 아닌경우 
        if(questionEntity.getType() != CustomerServiceType.QNA_PRODUCT){
            throw new IllegalArgumentException("해당 글은 상품문의가 아닙니다.");
        }
        
        // 3. detail entity 가져오기 
        List<CustomerServiceDetailEntity> qnaDetailEntity = customerServiceDetailRepository.findAllByCustomerServiceAndDelFalse(questionEntity);

        return QnaMapper.toDetail(
                "success",
                questionEntity,
                qnaDetailEntity
        );
    }



    @Override
    @Transactional
    // 상품 문의 등록
    public QnaProductDetailResponse addProductQna(QnaProductUpsertRequest request, Long userIdx){

        // TODO 유저 idx 기반 name 받아오기
        // 우선은 받아온 이름만 사용하는거로


        // 2. new entity 생성
        CustomerServiceEntity requestEntity = CustomerServiceEntity.builder()
                .code(UUID.randomUUID().toString())
                .type(CustomerServiceType.QNA_PRODUCT)
                .category(request.category())
                .status(CustomerServiceStatus.WAITING)
                .title(request.title())
                .isPrivate(request.isPrivate())
                .isPinned(false)
                .usersIdx(userIdx)
                .username(request.name())
                .productsIdx(request.productsIdx())
                .build();

        // 2.1 저장
        customerServiceRepository.save(requestEntity);

        // 3. detailEntity 생성
        CustomerServiceDetailEntity requestDetailEntity = CustomerServiceDetailEntity.builder()
                .detailCode(UUID.randomUUID().toString())
                .usersIdx(userIdx)
                .userName(requestEntity.getUserName())
                .customerService(requestEntity)
                .content(request.content())
                .build();

        // 3.2 저장
        customerServiceDetailRepository.save(requestDetailEntity);

        return QnaMapper.toDetail(
                "success",
                requestEntity,
                List.of(requestDetailEntity)
        );
    }

    @Override
    @Transactional
    public QnaProductDetailResponse updateQna(String qnaCode ,QnaProductUpsertRequest request, Long userIdx){

        // 1. entity 가져오기
        CustomerServiceEntity questionEntity = customerServiceRepository.findByCodeAndDelFalse(qnaCode)
                .orElseThrow(() -> new IllegalArgumentException("존재 하지 않는 문의입니다."));

        // 2. 유효성 검사
        // 2.1  본인이 아닌경우
        if (!Objects.equals(questionEntity.getUsersIdx(), userIdx)){
            throw new IllegalArgumentException("본인 만 수정 가능합니다.");
        }
        // 2.2 상품 문의가 아닌경우
        if(questionEntity.getType() != CustomerServiceType.QNA_PRODUCT){
            throw new IllegalArgumentException("해당 글은 상품문의가 아닙니다.");
        }
        // 2.3 판매자가 답변 전 이라면 수정 가능
        if(questionEntity.getStatus() != CustomerServiceStatus.WAITING){
            throw new IllegalArgumentException("더 이상 수정이 불가능 합니다.");
        }

        // 3. detail entity 가져오기
        List<CustomerServiceDetailEntity> qnaDetailEntity = customerServiceDetailRepository.findAllByCustomerServiceAndDelFalse(questionEntity);

        // 3.1 찾은 DetailEntityList 중 수정 할 Entity를 code로 찾기
        CustomerServiceDetailEntity willUpdate = qnaDetailEntity.stream()
                .filter(detail -> request.detailCode().equals(detail.getDetailCode()))
                .findFirst()  // 첫 번째 발견된 것 가져오기
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 내용입니다."));


        // 4. request 기반 update 진행
        questionEntity.update(
                request.category(),
                questionEntity.getStatus(),
                request.title(),
                request.isPrivate(),
                false
        );
        // 4.1 디테일도 업데이트
        willUpdate.update(request.content());

        return QnaMapper.toDetail(
                "success",
                questionEntity,
                qnaDetailEntity
        );
    }


    @Override
    @Transactional
    public ResultResponse deleteQna(String qnaCode, Long userIdx){
        // 1. entity 가져오기
        CustomerServiceEntity questionEntity = customerServiceRepository.findByCodeAndDelFalse(qnaCode)
                .orElseThrow(() -> new IllegalArgumentException("존재 하지 않는 문의입니다."));

        // 2. 유효성 검사
        // 2.1  본인이 아닌경우
        if (!Objects.equals(questionEntity.getUsersIdx(), userIdx)){
            throw new IllegalArgumentException("본인 만 삭제 가능합니다.");
        }
        // 2.2 상품 문의가 아닌경우
        if(questionEntity.getType() != CustomerServiceType.QNA_PRODUCT){
            throw new IllegalArgumentException("해당 글은 상품문의가 아닙니다.");
        }

        // 3. detail entity 가져오기
        List<CustomerServiceDetailEntity> qnaDetailEntity = customerServiceDetailRepository.findAllByCustomerServiceAndDelFalse(questionEntity);

        // 4. 삭제 진행
        questionEntity.delete();

        // 4.1 아래 답변들까지 삭제 처리
        for(CustomerServiceDetailEntity entity : qnaDetailEntity){
            entity.delete();
        }

        return new ResultResponse("success");
    }

    @Override
    @Transactional(readOnly = true)
    public QnaAndProductInfoListResponse getMyProductQnaList(Long userIdx, Pageable pageable){
        // 1. entity 조회
        Page<CustomerServiceEntity> qnaPage = customerServiceRepository.findAllByTypeAndUsersIdxAndDelFalse(CustomerServiceType.QNA_PRODUCT,userIdx ,pageable);

        // 1.1 상품 idx 추출
        List<Long> productIdxs = qnaPage.getContent().stream()
                .map(CustomerServiceEntity::getProductsIdx)
                .filter(Objects::nonNull) // null 방지
                .distinct()               // 중복 ID 제거
                .toList();

        // 2. Feign Client 호출
        // productMap을 Map 내부에서 쓰려면 final 처럼 작동하게 해야한다고 함
        // 단 한번만 초기화 되도록
        Map<Long, ProductInfoResponse> productMap;

        if (!productIdxs.isEmpty()) {
            // DTO에 담아서 전송
            ProductIdxsRequest request = new ProductIdxsRequest(productIdxs);

            // 호출
            List<ProductInfoResponse> productList = productServiceClient.getProductInfo(request);

            // List -> Map으로 변환 (검색 속도 향상을 위함)
            productMap = productList.stream()
                    .collect(Collectors.toMap(
                            ProductInfoResponse::productIdx,
                            Function.identity(),
                            (p1, p2) -> p1 // 중복 막기
                    ));
        } else {
            productMap = new HashMap<>();
        }

        // 2. 반환 데이터 생성
        List<QnaAndProductInfoResponse> result = qnaPage.stream()
                .map(entity -> {
                    // 위에서 가져온 상품 정보
                    ProductInfoResponse productInfo = productMap.get(entity.getProductsIdx());

                    String productCode = (productInfo != null) ? productInfo.productCode() : null;
                    String productName = (productInfo != null) ? productInfo.name() : "없는 상품";
                    String productImg = (productInfo != null) ? productInfo.imageUrl() : "";

                    return new QnaAndProductInfoResponse(
                        entity.getCode(),
                        entity.getCategory(),
                        entity.getStatus(),
                        entity.getTitle(),
                        entity.getViewCount(),
                        entity.getCreatedAt(),
                        entity.getUserName(),
                            productCode,
                            productName,
                            productImg

                );
                })
                .toList();

        // 3. 반환
        return new QnaAndProductInfoListResponse(
                "success",
                result
        );

    }
}
