package co.kr.product.product.service;

import co.kr.product.product.dto.request.DeductStockReq;
import co.kr.product.product.dto.request.ProductInfoToOrderReq;
import co.kr.product.product.dto.response.ProductDetailRes;
import co.kr.product.product.dto.response.ProductInfoToOrderRes;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

/**
 * 표준 service 단위 테스트 코드
 */
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock ProductRepository productRepository;
    @Mock ProductImageRepository productImageRepository;
    @Mock ProductOptionRepository productOptionRepository;

    // 테스트 대상 서비스 주입
    // 위에서 만든 @Mock 객체들을 이 Service 안에 자동으로 넣어줍니다. (생성자 주입)
    @InjectMocks ProductServiceImpl productService;


    /**
     * ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ
     * Given 데이터 세팅
     * ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ
     */

    String productCode = "P001";
    String optionCode = "O001";

    ProductEntity product = ProductEntity.builder()
            .sellerIdx(1L)
            .productsCode(productCode)
            .productsName("테스트 상품")
            .price(BigDecimal.valueOf(10000))
            .salePrice(BigDecimal.valueOf(9000))
            .stock(null)
            .status(null)
            .build();

    ProductOptionEntity option1 = ProductOptionEntity.builder()
            .optionCode(optionCode)
            .optionName("옵션A")
            .sortOrders(2)
            .optionPrice(BigDecimal.valueOf(10000))
            .optionSalePrice(BigDecimal.valueOf(9000))
            .stock(100)
            .status(ProductStatus.ON_SALE.name())
            .build();

    ProductOptionEntity option2 = ProductOptionEntity.builder()
            .optionCode(optionCode)
            .optionName("옵션B")
            .sortOrders(1)
            .optionPrice(BigDecimal.valueOf(10000))
            .optionSalePrice(BigDecimal.valueOf(9000))
            .stock(90)
            .status(ProductStatus.ON_SALE.name())
            .build();



    /**
     * ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ
     * 테스트 진행
     * ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ
     */

    @Test
    @DisplayName("상품 상세 조회 성공 - 조회수 증가 및 데이터 반환 확인")
    void 상품_상세_조회() {

        // Given
        // 가짜 이미지 리스트
        List<ProductImageEntity> images = List.of(
                ProductImageEntity.builder().url("img1.jpg").sortOrders(1).isThumbnail(true).build()
        );

        // 가짜 옵션 리스트
        List<ProductOptionEntity> options = List.of(option1, option2);

        // Mock 객체의 행동 정의 (Stubbing)
        given(productRepository.findByProductsCodeAndDelFalse(productCode))
                .willReturn(Optional.of(product));

        given(productImageRepository.findByProductAndDelFalseOrderByIsThumbnailDescSortOrdersAsc(product))
                .willReturn(images);

        given(productOptionRepository.findByProductAndDelFalseOrderBySortOrdersAsc(product))
                .willReturn(options);

        // When
        ProductDetailRes response = productService.getProductDetail(productCode);

        // Then
        assertThat(response.productsCode()).isEqualTo(productCode);

        // 조회수가 0에서 1로 증가했는지 검증
        assertThat(response.viewCount()).isEqualTo(1L);

        // 이미지가 잘 매핑되었는지
        assertThat(response.images()).hasSize(1);
        assertThat(response.images().get(0).url()).isEqualTo("img1.jpg");


        /*
         * verify 쓰는 법
         * 1. 횟수 검증 (가장 많이 씀)
         * 2. 순서 검증 (InOrder)
         * 3. 더 이상 딴짓 안 했는지 검증 (Strict Mode)
         */

        // "상품/이미지 조회 repository는 정확히 1번 호출되어야 해"
        verify(productRepository, times(1)).findByProductsCodeAndDelFalse(productCode);
        verify(productImageRepository, times(1)).findByProductAndDelFalseOrderByIsThumbnailDescSortOrdersAsc(any());

        // "옵션 조회도 최소 1번은 호출되어야 해" (1번 이상이면 통과)
        verify(productOptionRepository, atLeastOnce()).findByProductAndDelFalseOrderBySortOrdersAsc(any());

        // "삭제(Delete) 관련 메서드는 절대 호출되면 안 돼!"
        verify(productRepository, never()).delete(any());

        // "반드시 상품을 먼저 조회하고, 그 다음에 이미지를 조회해야 해" (inOrder 안에 순서 바뀌어도 됨)
        InOrder inOrder = inOrder(productRepository, productImageRepository);

        // 여기부터 순서 틀리면 에러
        inOrder.verify(productRepository).findByProductsCodeAndDelFalse(productCode);
        inOrder.verify(productImageRepository).findByProductAndDelFalseOrderByIsThumbnailDescSortOrdersAsc(any());

        // "위에서 검증한 것들 외에, productRepository를 몰래 더 건드린 거 없어?"
        verifyNoMoreInteractions(productRepository);
    }

    @Test
    void 상품_상세_조회_실패_존재하지_않음 () {
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

    @Test
    void 상품_재고_체크 () {

        // todo
    }

    @Test
    void 상품_재고_감소_단일 () {

        // Given
        DeductStockReq request = new DeductStockReq(1L,1L,20);

        given(productOptionRepository.decreaseStock(request.optionIdx(), request.quantity())).willReturn(1);

        productService.deductStock(request);

        then(productOptionRepository).should(times(1)).decreaseStock(request.optionIdx(), request.quantity());
    }

    @Test
    void 상품_재고_감소_리스트 () {

        // Given
        DeductStockReq product1 = new DeductStockReq(1L, 1L, 2);
        DeductStockReq product2 = new DeductStockReq(2L, 3L, 10);
        DeductStockReq product3 = new DeductStockReq(3L, 4L, 7);
        List<DeductStockReq> requests = List.of(product1, product2, product3);

        given(productOptionRepository.decreaseStock(product1.optionIdx(), product1.quantity())).willReturn(1);
        given(productOptionRepository.decreaseStock(product2.optionIdx(), product2.quantity())).willReturn(1);
        given(productOptionRepository.decreaseStock(product3.optionIdx(), product3.quantity())).willReturn(1);

        // when
        productService.deductStocks(requests);

        verify(productOptionRepository, times(3)).decreaseStock(any(), anyInt());
    }

    @Test
    void 상품_재고_감소_리스트_재고부족() {

        // Given
        DeductStockReq product1 = new DeductStockReq(1L, 1L, 2);
        DeductStockReq product2 = new DeductStockReq(2L, 3L, 999999);
        DeductStockReq product3 = new DeductStockReq(3L, 4L, 7);
        List<DeductStockReq> request = List.of(product1, product2, product3);

        given(productOptionRepository.decreaseStock(product1.optionIdx(), product1.quantity())).willReturn(1);
        given(productOptionRepository.decreaseStock(product2.optionIdx(), product2.quantity())).willReturn(0);

        // when
        Assertions.assertThatThrownBy(() -> productService.deductStocks(request)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 상품_정보_요청 () {

        // todo
    }

    @Test
    void 상품_정보_리스트_요청 () {

        // Given
        ProductInfoToOrderReq productRequest1 = new ProductInfoToOrderReq(1L, 1L);
        ProductInfoToOrderReq productRequest2 = new ProductInfoToOrderReq(2L, 2L);
        List<ProductInfoToOrderReq> request = List.of(productRequest1, productRequest2);

        ReflectionTestUtils.setField(product, "productsIdx", 1L);
        ReflectionTestUtils.setField(option1, "optionGroupIdx", 1L);
        ReflectionTestUtils.setField(option1, "product", product);

        ProductInfoToOrderRes productResponse1 = new ProductInfoToOrderRes(
                productRequest1.productIdx(),
                productRequest1.optionIdx(),
                product.getSellerIdx(),
                product.getProductsName(),
                option1.getOptionName(),
                option1.getOptionPrice(),
                option1.getStock()
        );

        ProductEntity product2 = ProductEntity.builder()
                .productsName("상품B")
                .productsCode("CODE-002")
                .sellerIdx(1L)
                .price(BigDecimal.valueOf(20000))
                .salePrice(BigDecimal.valueOf(18000))
                .stock(50)
                .status(ProductStatus.ON_SALE)
                .build();

        ReflectionTestUtils.setField(product2, "productsIdx", 2L);

        ProductOptionEntity option3 = ProductOptionEntity.builder()
                .product(product2)
                .optionCode(UUID.randomUUID().toString())
                .optionName("옵션B")
                .sortOrders(1)
                .optionPrice(BigDecimal.valueOf(10000))
                .optionSalePrice(BigDecimal.valueOf(9000))
                .stock(90)
                .status(ProductStatus.ON_SALE.name())
                .build();

        ReflectionTestUtils.setField(option3, "optionGroupIdx", 2L);

        ProductInfoToOrderRes productResponse2 = new ProductInfoToOrderRes(
                productRequest2.productIdx(),
                productRequest2.optionIdx(),
                product2.getSellerIdx(),
                "상품B",
                option3.getOptionName(),
                option3.getOptionPrice(),
                option3.getStock()
        );

        // findAllWithOptions 호출 시 리턴값 설정
        given(productOptionRepository.findAllWithOptions(List.of(productRequest1.optionIdx(), productRequest2.optionIdx())))
                .willReturn(List.of(option1, option3));

        // When
        List<ProductInfoToOrderRes> result = productService.getProductInfoList(request);

        // Then
        Assertions.assertThat(result.get(0)).isEqualTo(productResponse1);
        Assertions.assertThat(result.get(1)).isEqualTo(productResponse2);

        verify(productOptionRepository, times(1))
                .findAllWithOptions(argThat(list ->
                        list.contains(1L) && list.contains(2L) && list.size() == 2
                ));
    }
}