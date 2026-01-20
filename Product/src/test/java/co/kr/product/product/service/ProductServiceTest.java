package co.kr.product.product.service;

import co.kr.product.product.dto.response.ProductDetailResponse;
import co.kr.product.product.dto.vo.ProductStatus;
import co.kr.product.product.entity.ProductEntity;
import co.kr.product.product.entity.ProductImageEntity;
import co.kr.product.product.entity.ProductOptionEntity;
import co.kr.product.product.repository.ProductImageRepository;
import co.kr.product.product.repository.ProductOptionRepository;
import co.kr.product.product.repository.ProductRepository;
import co.kr.product.product.service.impl.ProductServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * 표준 service 단위테스트 코드
 */
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock ProductRepository productRepository;
    @Mock ProductImageRepository productImageRepository;
    @Mock ProductOptionRepository productOptionRepository;

    // 테스트 대상 서비스 주입
    // 위에서 만든 @Mock 객체들을 이 Service 안에 자동으로 넣어줍니다. (생성자 주입)
    @InjectMocks ProductServiceImpl productService;

    @Test
    @DisplayName("상품 상세 조회 성공 - 조회수 증가 및 데이터 반환 확인")
    void getProductDetail_Success() {
        // [Given] 테스트 준비 단계
        String productCode = "P001";

        // 가짜 상품 데이터 생성
        ProductEntity product = ProductEntity.builder()
                .sellerIdx(1L)
                .productsCode(productCode)
                .productsName("테스트 상품")
                .price(BigDecimal.valueOf(10000))
                .salePrice(BigDecimal.valueOf(9000))
                .stock(100)
                .status(ProductStatus.ON_SALE)
                .build();

        // 가짜 이미지 리스트
        List<ProductImageEntity> images = List.of(
                ProductImageEntity.builder().url("img1.jpg").sortOrders(1).isThumbnail(true).build()
        );

        // 가짜 옵션 리스트 (빈 리스트로 가정)
        List<ProductOptionEntity> options = List.of();

        // Mock 객체의 행동 정의 (Stubbing)
        // "repository.findBy... 가 호출되면, 위의 product 객체를 Optional로 감싸서 리턴해라"라고 설정
        given(productRepository.findByProductsCodeAndDelFalse(productCode))
                .willReturn(Optional.of(product));

        given(productImageRepository.findByProductAndDelFalseOrderByIsThumbnailDescSortOrdersAsc(product))
                .willReturn(images);

        given(productOptionRepository.findByProductAndDelFalseOrderBySortOrdersAsc(product))
                .willReturn(options);

        // [When] 실제 테스트 실행
        ProductDetailResponse response = productService.getProductDetail(productCode);

        // [Then] 결과 검증
        // 조회된 상품 코드가 요청한 코드와 같은지
        assertThat(response.productsCode()).isEqualTo(productCode);

        // 조회수가 0에서 1로 증가했는지 검증 (Service 내부 로직 확인)
        assertThat(response.viewCount()).isEqualTo(1L);

        // 3. 이미지가 잘 매핑되었는지
        assertThat(response.images()).hasSize(1);
        assertThat(response.images().get(0).url()).isEqualTo("img1.jpg");

        // 실제로 repository가 호출되었는지 확인 (verify 쓰는 법)
        // 1. 횟수 검증 (가장 많이 씀)
        // "상품 조회 repository는 정확히 1번 호출되어야 해"
        verify(productRepository, times(1)).findByProductsCodeAndDelFalse(productCode);

        // "이미지 조회도 1번 호출되어야 해"
        verify(productImageRepository, times(1)).findByProductAndDelFalseOrderByIsThumbnailDescSortOrdersAsc(any());

        // "옵션 조회도 최소 1번은 호출되어야 해" (1번 이상이면 통과)
        verify(productOptionRepository, atLeastOnce()).findByProductAndDelFalseOrderBySortOrdersAsc(any());

        // "삭제(Delete) 관련 메서드는 절대 호출되면 안 돼!" (매우 중요)
        verify(productRepository, never()).delete(any());

        // 2. 순서 검증 (InOrder)
        // "반드시 상품을 먼저 조회하고, 그 다음에 이미지를 조회해야 해" (순서가 꼬이면 에러)
        InOrder inOrder = inOrder(productRepository, productImageRepository);

        inOrder.verify(productRepository).findByProductsCodeAndDelFalse(productCode);
        inOrder.verify(productImageRepository).findByProductAndDelFalseOrderByIsThumbnailDescSortOrdersAsc(any());

        // 3. 더 이상 딴짓 안 했는지 검증 (Strict Mode)
        // "위에서 검증한 것들 외에, productRepository를 몰래 더 건드린 거 없어?"
        verifyNoMoreInteractions(productRepository);
    }

    @Test
    @DisplayName("상품 상세 조회 실패 - 존재하지 않는 상품 코드")
    void getProductDetail_NotFound() {
        // [Given]
        String invalidCode = "INVALID_CODE";

        // Repository가 빈 값(Optional.empty)을 반환하도록 설정
        given(productRepository.findByProductsCodeAndDelFalse(invalidCode))
                .willReturn(Optional.empty());

        // [When] & [Then]
        // 예외가 발생하는지 검증
        Assertions.assertThatThrownBy(() -> productService.getProductDetail(invalidCode))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Product not found");
    }
}