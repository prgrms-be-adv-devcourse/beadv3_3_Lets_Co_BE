package co.kr.costomerservice.qnaProduct.service;

import co.kr.costomerservice.client.AuthServiceClient;
import co.kr.costomerservice.client.ProductServiceClient;
import co.kr.costomerservice.common.dto.request.ProductIdxsRequest;
import co.kr.costomerservice.common.dto.response.ProductInfoResponse;
import co.kr.costomerservice.common.dto.response.ProductSellerResponse;
import co.kr.costomerservice.common.entity.CustomerServiceDetailEntity;
import co.kr.costomerservice.common.entity.CustomerServiceEntity;
import co.kr.costomerservice.common.repository.CustomerServiceDetailRepository;
import co.kr.costomerservice.common.repository.CustomerServiceRepository;
import co.kr.costomerservice.common.vo.CustomerServiceCategory;
import co.kr.costomerservice.common.vo.CustomerServiceStatus;
import co.kr.costomerservice.common.vo.CustomerServiceType;
import co.kr.costomerservice.qnaProduct.model.request.QnaAnswerUpsertRequest;
import co.kr.costomerservice.qnaProduct.model.response.QnaAndProductInfoListResponse;
import co.kr.costomerservice.qnaProduct.model.response.QnaProductDetailResponse;
import co.kr.costomerservice.qnaProduct.service.impl.QnaSellerServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class QnaSellerServiceTest {

    @InjectMocks
    QnaSellerServiceImpl qnaSellerService;

    @Mock
    CustomerServiceRepository customerServiceRepository;
    @Mock
    CustomerServiceDetailRepository customerServiceDetailRepository;
    @Mock
    AuthServiceClient authServiceClient;
    @Mock
    ProductServiceClient productServiceClient;

    final Long SELLER_IDX = 10L;
    final Long PRODUCT_IDX = 100L;
    final String QNA_CODE = "QNA-001";
    final String PARENT_DETAIL_CODE = "DETAIL-PARENT";

    @Test
    @DisplayName("판매자 내 상품 문의 목록 조회 - 성공")
    void getMyQnaList_Success() {

        // Given
        Pageable pageable = PageRequest.of(0, 10);

        CustomerServiceEntity qna = createQnaEntity();
        Page<CustomerServiceEntity> qnaPage = new PageImpl<>(List.of(qna));

        ProductInfoResponse productInfo = new ProductInfoResponse(PRODUCT_IDX, "P-CODE", "상품명A", "img.jpg");

        given(authServiceClient.getUserRole(SELLER_IDX)).willReturn("SELLER");
        given(customerServiceRepository.findAllByTypeAndUsersIdxAndDelFalse(CustomerServiceType.QNA_PRODUCT, SELLER_IDX, pageable))
                .willReturn(qnaPage);
        given(productServiceClient.getProductInfo(any(ProductIdxsRequest.class)))
                .willReturn(List.of(productInfo));

        // When
        QnaAndProductInfoListResponse response = qnaSellerService.getMyQnaList(SELLER_IDX, pageable);

        // Then
        assertThat(response.resultCode()).isEqualTo("success");
        assertThat(response.items()).hasSize(1);
        assertThat(response.items().get(0).name()).isEqualTo("상품명A");
        assertThat(response.items().get(0).title()).isEqualTo(qna.getTitle());
    }

    @Test
    @DisplayName("판매자 내 상품 문의 목록 조회 실패 - 권한 없음")
    void getMyQnaList_Fail_Unauthorized() {
        // Given
        given(authServiceClient.getUserRole(SELLER_IDX)).willReturn("USER"); // 일반 유저

        // When & Then
        assertThatThrownBy(() -> qnaSellerService.getMyQnaList(SELLER_IDX, PageRequest.of(0, 10)))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("판매자 권한이 없습니다.");
    }

    @Test
    @DisplayName("판매자 답변 등록 - 성공")
    void addAnswer_Success() {
        // Given
        CustomerServiceEntity qna = createQnaEntity();

        // 부모 Detail (질문글)
        CustomerServiceDetailEntity parentDetail = createDetailEntity(qna, PARENT_DETAIL_CODE);
        // 부모의 parentIdx가 null이라고 가정 (루트 질문)

        List<CustomerServiceDetailEntity> detailList = new ArrayList<>();
        detailList.add(parentDetail);

        QnaAnswerUpsertRequest request = new QnaAnswerUpsertRequest("판매자", PARENT_DETAIL_CODE, "답변입니다.");

        given(customerServiceRepository.findByCodeAndDelFalse(QNA_CODE)).willReturn(Optional.of(qna));

        given(productServiceClient.getSellerIdx(PRODUCT_IDX)).willReturn(new ProductSellerResponse(SELLER_IDX));

        given(customerServiceDetailRepository.findAllByCustomerServiceAndDelFalse(qna)).willReturn(detailList);

        // When
        QnaProductDetailResponse response = qnaSellerService.addAnswer(QNA_CODE, SELLER_IDX, request);

        // Then
        verify(customerServiceDetailRepository).save(any(CustomerServiceDetailEntity.class));

        assertThat(qna.getStatus()).isEqualTo(CustomerServiceStatus.ANSWERED);

        assertThat(detailList).hasSize(2); // 원글 + 답변
    }

    @Test
    @DisplayName("판매자 답변 등록 실패 - 본인 상품 아님")
    void addAnswer_Fail_NotMyProduct() {
        // Given
        CustomerServiceEntity qna = createQnaEntity();
        Long otherSellerIdx = 999L; // 다른 판매자

        QnaAnswerUpsertRequest request = new QnaAnswerUpsertRequest("판매자", PARENT_DETAIL_CODE, "답변");

        given(customerServiceRepository.findByCodeAndDelFalse(QNA_CODE)).willReturn(Optional.of(qna));

        // 상품 주인 조회 결과가 다름
        given(productServiceClient.getSellerIdx(PRODUCT_IDX)).willReturn(new ProductSellerResponse(otherSellerIdx));

        // When & Then
        assertThatThrownBy(() -> qnaSellerService.addAnswer(QNA_CODE, SELLER_IDX, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("해당 상품의 판매자가 아닙니다.");
    }

    @Test
    @DisplayName("판매자 답변 등록 실패 - 부모글(Detail) 찾을 수 없음")
    void addAnswer_Fail_ParentNotFound() {
        // Given
        CustomerServiceEntity qna = createQnaEntity();
        List<CustomerServiceDetailEntity> detailList = new ArrayList<>();

        QnaAnswerUpsertRequest request = new QnaAnswerUpsertRequest("판매자", "INVALID-CODE", "답변");

        given(customerServiceRepository.findByCodeAndDelFalse(QNA_CODE)).willReturn(Optional.of(qna));
        given(productServiceClient.getSellerIdx(PRODUCT_IDX)).willReturn(new ProductSellerResponse(SELLER_IDX));
        given(customerServiceDetailRepository.findAllByCustomerServiceAndDelFalse(qna)).willReturn(detailList);

        // When & Then
        assertThatThrownBy(() -> qnaSellerService.addAnswer(QNA_CODE, SELLER_IDX, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 내용입니다.");
    }

    /**
     * Helper Methods
     */
    private CustomerServiceEntity createQnaEntity() {
        CustomerServiceEntity entity = CustomerServiceEntity.builder()
                .code(QNA_CODE)
                .type(CustomerServiceType.QNA_PRODUCT)
                .category(CustomerServiceCategory.PRODUCT)
                .status(CustomerServiceStatus.WAITING)
                .title("상품 문의")
                .productsIdx(PRODUCT_IDX)
                .usersIdx(1L) // 질문자 ID
                .username("구매자")
                .build();
        ReflectionTestUtils.setField(entity, "idx", 1L);
        ReflectionTestUtils.setField(entity, "createdAt", LocalDateTime.now());
        ReflectionTestUtils.setField(entity, "del", false);
        return entity;
    }

    private CustomerServiceDetailEntity createDetailEntity(CustomerServiceEntity parent, String detailCode) {
        CustomerServiceDetailEntity detail = CustomerServiceDetailEntity.builder()
                .detailCode(detailCode)
                .usersIdx(parent.getUsersIdx())
                .userName(parent.getUserName())
                .customerService(parent)
                .content("질문 내용")
                .build();
        ReflectionTestUtils.setField(detail, "detailIdx", 100L);
        ReflectionTestUtils.setField(detail, "del", false);
        return detail;
    }
}