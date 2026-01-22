package co.kr.costomerservice.qnaProduct.service;

import co.kr.costomerservice.client.ProductServiceClient;
import co.kr.costomerservice.common.dto.request.ProductIdxsRequest;
import co.kr.costomerservice.common.dto.response.ProductInfoResponse;
import co.kr.costomerservice.common.dto.response.ResultResponse;
import co.kr.costomerservice.common.entity.CustomerServiceDetailEntity;
import co.kr.costomerservice.common.entity.CustomerServiceEntity;
import co.kr.costomerservice.common.repository.CustomerServiceDetailRepository;
import co.kr.costomerservice.common.repository.CustomerServiceRepository;
import co.kr.costomerservice.common.vo.CustomerServiceCategory;
import co.kr.costomerservice.common.vo.CustomerServiceStatus;
import co.kr.costomerservice.common.vo.CustomerServiceType;
import co.kr.costomerservice.qnaProduct.model.request.QnaProductUpsertRequest;
import co.kr.costomerservice.qnaProduct.model.response.QnaAndProductInfoListResponse;
import co.kr.costomerservice.qnaProduct.model.response.QnaProductDetailResponse;
import co.kr.costomerservice.qnaProduct.model.response.QnaProductListResponse;
import co.kr.costomerservice.qnaProduct.service.impl.QnaProductServiceImpl;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class QnaProductServiceTest {

    @InjectMocks QnaProductServiceImpl qnaProductService;

    @Mock CustomerServiceRepository customerServiceRepository;
    @Mock CustomerServiceDetailRepository customerServiceDetailRepository;
    @Mock ProductServiceClient productServiceClient;

    final Long USER_IDX = 1L;
    final Long PRODUCTS_IDX = 100L;
    final String QNA_CODE = "QNA-001";

    @Test
    @DisplayName("상품 QnA 목록 조회 - 성공")
    void getProductQnaList_Success() {

        // Given
        Pageable pageable = PageRequest.of(0, 10);
        CustomerServiceEntity qna = createQnaEntity(USER_IDX, PRODUCTS_IDX, false);
        Page<CustomerServiceEntity> pageResult = new PageImpl<>(List.of(qna));

        given(customerServiceRepository.findAllByTypeAndProductsIdxAndIsPrivateFalseAndDelFalse(
                CustomerServiceType.QNA_PRODUCT, PRODUCTS_IDX, pageable))
                .willReturn(pageResult);

        // When
        QnaProductListResponse response = qnaProductService.getProductQnaList(PRODUCTS_IDX, pageable);

        // Then
        assertThat(response.resultCode()).isEqualTo("success");
        assertThat(response.items()).hasSize(1);
        assertThat(response.items().get(0).title()).isEqualTo(qna.getTitle());

        // 확인용
        System.out.println(response);
    }

    @Test
    @DisplayName("상품 QnA 상세 조회 - 성공 (본인 비밀글)")
    void getProductQnaDetail_Success_PrivateOwner() {

        // Given
        CustomerServiceEntity qna = createQnaEntity(USER_IDX, PRODUCTS_IDX, true); // 비밀글
        CustomerServiceDetailEntity detail = createDetailEntity(qna);

        given(customerServiceRepository.findByCodeAndDelFalse(QNA_CODE)).willReturn(Optional.of(qna));
        given(customerServiceDetailRepository.findAllByCustomerServiceAndDelFalse(qna)).willReturn(List.of(detail));

        // When
        QnaProductDetailResponse response = qnaProductService.getProductQnaDetail(QNA_CODE, USER_IDX);

        // Then
        assertThat(response.resultCode()).isEqualTo("success");
        assertThat(response.questionDTO().title()).isEqualTo(qna.getTitle());

        // 확인용
        System.out.println(response);
    }

    @Test
    @DisplayName("상품 QnA 상세 조회 실패 - 비밀글 (타인)")
    void getProductQnaDetail_Fail_PrivateNotOwner() {

        // Given
        Long otherUserIdx = 999L;
        CustomerServiceEntity qna = createQnaEntity(otherUserIdx, PRODUCTS_IDX, true); // 타인 비밀글

        given(customerServiceRepository.findByCodeAndDelFalse(QNA_CODE)).willReturn(Optional.of(qna));

        // When & Then
        assertThatThrownBy(() -> qnaProductService.getProductQnaDetail(QNA_CODE, USER_IDX))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("해당 문의는 비밀 글 입니다.");
    }

    @Test
    @DisplayName("상품 QnA 등록 - 성공")
    void addProductQna_Success() {
        // Given
        QnaProductUpsertRequest request = new QnaProductUpsertRequest(
                PRODUCTS_IDX, CustomerServiceCategory.PRODUCT, "문의합니다",
                true, null, "홍길동", null, "내용입니다"
        );

        given(customerServiceRepository.save(any(CustomerServiceEntity.class)))
                .willAnswer(i -> i.getArgument(0));
        given(customerServiceDetailRepository.save(any(CustomerServiceDetailEntity.class)))
                .willAnswer(i -> i.getArgument(0));

        // When
        QnaProductDetailResponse response = qnaProductService.addProductQna(request, USER_IDX);

        // Then
        assertThat(response.resultCode()).isEqualTo("success");
        assertThat(response.questionDTO().title()).isEqualTo("문의합니다");

        verify(customerServiceRepository).save(any(CustomerServiceEntity.class));
        verify(customerServiceDetailRepository).save(any(CustomerServiceDetailEntity.class));

        // 확인용
        System.out.println(response);
    }

    @Test
    @DisplayName("상품 QnA 수정 - 성공")
    void updateQna_Success() {
        // Given
        String detailCode = "DETAIL-001";
        CustomerServiceEntity qna = createQnaEntity(USER_IDX, PRODUCTS_IDX, false);
        CustomerServiceDetailEntity detail = createDetailEntity(qna);
        ReflectionTestUtils.setField(detail, "detailCode", detailCode);

        QnaProductUpsertRequest request = new QnaProductUpsertRequest(
                PRODUCTS_IDX, CustomerServiceCategory.SHIPPING, "수정제목",
                true, detailCode, "홍길동", null, "수정내용"
        );

        given(customerServiceRepository.findByCodeAndDelFalse(QNA_CODE)).willReturn(Optional.of(qna));
        given(customerServiceDetailRepository.findAllByCustomerServiceAndDelFalse(qna)).willReturn(List.of(detail));

        // When
        QnaProductDetailResponse response = qnaProductService.updateQna(QNA_CODE, request, USER_IDX);

        // Then
        assertThat(qna.getTitle()).isEqualTo("수정제목");
        assertThat(detail.getContent()).isEqualTo("수정내용");
        assertThat(qna.getIsPrivate()).isTrue();

        // 확인용
        System.out.println(response);
    }

    @Test
    @DisplayName("상품 QnA 수정 실패 - 답변 완료 상태")
    void updateQna_Fail_AlreadyAnswered() {
        // Given
        CustomerServiceEntity qna = createQnaEntity(USER_IDX, PRODUCTS_IDX, false);
        qna.updateStatus(CustomerServiceStatus.ANSWERED);

        given(customerServiceRepository.findByCodeAndDelFalse(QNA_CODE)).willReturn(Optional.of(qna));

        // When & Then
        assertThatThrownBy(() -> qnaProductService.updateQna(QNA_CODE, null, USER_IDX))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("더 이상 수정이 불가능 합니다.");
    }

    @Test
    @DisplayName("상품 QnA 삭제 - 성공")
    void deleteQna_Success() {
        // Given
        CustomerServiceEntity qna = createQnaEntity(USER_IDX, PRODUCTS_IDX, false);
        CustomerServiceDetailEntity detail = createDetailEntity(qna);

        given(customerServiceRepository.findByCodeAndDelFalse(QNA_CODE)).willReturn(Optional.of(qna));
        given(customerServiceDetailRepository.findAllByCustomerServiceAndDelFalse(qna)).willReturn(List.of(detail));

        // When
        ResultResponse response = qnaProductService.deleteQna(QNA_CODE, USER_IDX);

        // Then
        assertThat(response.resultCode()).isEqualTo("success");
        assertThat(qna.getDel()).isTrue();
        assertThat(detail.getDel()).isTrue();

        // 확인용
        System.out.println(response);
    }

    @Test
    @DisplayName("내 QnA 목록 조회 (Feign 연동) - 성공")
    void getMyProductQnaList_Success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        CustomerServiceEntity qna = createQnaEntity(USER_IDX, PRODUCTS_IDX, false);
        Page<CustomerServiceEntity> qnaPage = new PageImpl<>(List.of(qna));

        ProductInfoResponse productInfo = new ProductInfoResponse(PRODUCTS_IDX, "P-CODE", "테스트상품", "img.jpg");

        given(customerServiceRepository.findAllByTypeAndUsersIdxAndDelFalse(CustomerServiceType.QNA_PRODUCT, USER_IDX, pageable))
                .willReturn(qnaPage);

        given(productServiceClient.getProductInfo(any(ProductIdxsRequest.class)))
                .willReturn(List.of(productInfo));

        // When
        QnaAndProductInfoListResponse response = qnaProductService.getMyProductQnaList(USER_IDX, pageable);

        // Then
        assertThat(response.resultCode()).isEqualTo("success");
        assertThat(response.items()).hasSize(1);

        assertThat(response.items().get(0).name()).isEqualTo("테스트상품");
        assertThat(response.items().get(0).title()).isEqualTo(qna.getTitle());

        // 확인용
        System.out.println(response);
    }

    /**
     * Helper Methods
     */
    private CustomerServiceEntity createQnaEntity(Long userIdx, Long productIdx, boolean isPrivate) {
        CustomerServiceEntity entity = CustomerServiceEntity.builder()
                .code(QNA_CODE)
                .type(CustomerServiceType.QNA_PRODUCT)
                .category(CustomerServiceCategory.PRODUCT)
                .status(CustomerServiceStatus.WAITING)
                .title("테스트 문의")
                .isPrivate(isPrivate)
                .usersIdx(userIdx)
                .productsIdx(productIdx)
                .username("유저1")
                .build();
        ReflectionTestUtils.setField(entity, "idx", 1L);
        ReflectionTestUtils.setField(entity, "createdAt", LocalDateTime.now());
        ReflectionTestUtils.setField(entity, "del", false);
        return entity;
    }

    private CustomerServiceDetailEntity createDetailEntity(CustomerServiceEntity parent) {
        CustomerServiceDetailEntity detail = CustomerServiceDetailEntity.builder()
                .detailCode("DETAIL-001")
                .usersIdx(parent.getUsersIdx())
                .userName(parent.getUserName())
                .customerService(parent)
                .content("상세 내용")
                .build();
        ReflectionTestUtils.setField(detail, "del", false);
        return detail;
    }
}