package co.kr.customerservice.qnaProduct.service.impl;


import co.kr.customerservice.client.AuthServiceClient;
import co.kr.customerservice.client.ProductServiceClient;
import co.kr.customerservice.common.model.dto.request.ProductIdxsRequest;
import co.kr.customerservice.common.model.dto.response.ProductInfoResponse;
import co.kr.customerservice.common.model.dto.response.ProductSellerResponse;
import co.kr.customerservice.common.model.entity.CustomerServiceDetailEntity;
import co.kr.customerservice.common.model.entity.CustomerServiceEntity;
import co.kr.customerservice.common.model.vo.CustomerServiceStatus;
import co.kr.customerservice.common.model.vo.CustomerServiceType;
import co.kr.customerservice.common.repository.CustomerServiceDetailRepository;
import co.kr.customerservice.common.repository.CustomerServiceRepository;
import co.kr.customerservice.qnaProduct.mapper.QnaMapper;
import co.kr.customerservice.qnaProduct.model.request.QnaAnswerUpsertReq;
import co.kr.customerservice.qnaProduct.model.response.*;
import co.kr.customerservice.qnaProduct.service.QnaSellerService;
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
public class QnaSellerServiceImpl implements QnaSellerService {

    private final CustomerServiceRepository customerServiceRepository;
    private final CustomerServiceDetailRepository customerServiceDetailRepository;
    private final AuthServiceClient authServiceClient;
    private final ProductServiceClient productServiceClient;

    // 본인상품에 온 모든 문의 조회(상품이 달라도)
    @Override
    @Transactional(readOnly = true)
    public QnaAndProductInfoListRes getMyQnaList(Long userIdx, Pageable pageable){


        // 1. 유저 확인
        String role = authServiceClient.getUserRole(userIdx).getBody();
        if (!"SELLER".equals(role) && !"ADMIN".equals(role)) {
            throw new RuntimeException("판매자 권한이 없습니다.");
        }

        // 2. 엔티티 조회
        Page<CustomerServiceEntity> qnaPage = customerServiceRepository.findAllByTypeAndUsersIdxAndDelFalse(CustomerServiceType.QNA_PRODUCT, userIdx, pageable);

        // 3. 상품 정보 조회
        // 해당 기능의 경우 상품정보 내에서 보이는 문의 내역이 아닌,
        // 판매자가 자신에게 온 모든 문의를 보기에 상품정보가 별도로 필요함 (이름 사진 정도만이라도)
        List<Long> productIdxs = qnaPage.getContent().stream()
                .map(CustomerServiceEntity::getProductsIdx)
                .filter(Objects::nonNull) // null 방지
                .distinct()               // 중복 ID 제거
                .toList();

        // 3.1 Feign Client 호출
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
        
        // 4. List로 변환
        List<QnaAndProductInfoRes> result = qnaPage.stream()
                .map(entity -> {

                    // 위에서 가져온 상품 정보
                    ProductInfoResponse productInfo = productMap.get(entity.getProductsIdx());

                    String productCode = (productInfo != null) ? productInfo.productCode() : null;
                    String productName = (productInfo != null) ? productInfo.name() : "없는 상품";
                    String productImg = (productInfo != null) ? productInfo.imageUrl() : "";

                        return new QnaAndProductInfoRes(
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

                );} )
                .toList();

        // 5. 반환
        return new QnaAndProductInfoListRes(
                result
        );

    }

    // 상품 주인이 문의 답변하기
    @Override
    @Transactional
    public QnaProductDetailRes addAnswer(String qnaCode, Long userIdx, QnaAnswerUpsertReq request){


        // 1. entity 가져오기
        CustomerServiceEntity questionEntity = customerServiceRepository.findByCodeAndDelFalse(qnaCode)
                .orElseThrow(() -> new IllegalArgumentException("존재 하지 않는 문의입니다."));

        // 2. 유효성 검사

        // 2.1 상품 문의가 아닌경우
        if(questionEntity.getType() != CustomerServiceType.QNA_PRODUCT){
            throw new IllegalArgumentException("해당 글은 상품문의가 아닙니다.");
        }
        // 2.2 본인 상품이 아닌 경우
        // FeignClient로 판매자 IDX를 받아옴. 문의에 대한 답변의 경우 한차례 통신, 검사를 하더라도 문제 없다고 판단
        ProductSellerResponse sellerIdxDto = productServiceClient.getSellerIdx(questionEntity.getProductsIdx());

        if (!Objects.equals(userIdx, sellerIdxDto.sellerIdx())){
            throw new IllegalArgumentException("해당 상품의 판매자가 아닙니다.");
        }

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
                questionEntity,
                qnaDetailEntity
        );
    }

}
