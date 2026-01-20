package co.kr.product.product.service;

import co.kr.product.product.client.AuthServiceClient;
import co.kr.product.product.document.ProductDocument;
import co.kr.product.product.dto.request.ProductImagesRequest;
import co.kr.product.product.dto.request.ProductListRequest;
import co.kr.product.product.dto.request.ProductOptionsRequest;
import co.kr.product.product.dto.request.UpsertProductRequest;
import co.kr.product.product.dto.response.ProductDetailResponse;
import co.kr.product.product.dto.response.ProductListResponse;
import co.kr.product.product.dto.vo.ProductStatus;
import co.kr.product.product.entity.ProductEntity;
import co.kr.product.product.entity.ProductImageEntity;
import co.kr.product.product.entity.ProductOptionEntity;
import co.kr.product.product.repository.ProductEsRepository;
import co.kr.product.product.repository.ProductImageRepository;
import co.kr.product.product.repository.ProductOptionRepository;
import co.kr.product.product.repository.ProductRepository;
import co.kr.product.product.service.impl.ProductManagerServiceImpl;
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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ProductManagerServiceTest {

    @InjectMocks ProductManagerServiceImpl productService;

    @Mock ProductRepository productRepository;
    @Mock ProductOptionRepository optionRepository;
    @Mock ProductImageRepository imageRepository;
    @Mock ProductEsRepository productEsRepository;
    @Mock AuthServiceClient authServiceClient;

    // 귀찮으니 메서드로
    UpsertProductRequest createUpsertRequest() {
        ProductOptionsRequest optionReq = new ProductOptionsRequest(
                null,
                "Option1",
                1,
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(900),
                10,
                "ON_SALE"
        );

        ProductImagesRequest imageReq = new ProductImagesRequest(
                null,
                "http://test.com/img.jpg",
                1,
                true
        );

        return new UpsertProductRequest(
                null,
                "Test Product",
                "Description",
                BigDecimal.valueOf(10000),
                BigDecimal.valueOf(9000),
                100,
                ProductStatus.ON_SALE,
                List.of(optionReq),
                List.of(imageReq)
        );
    }

    ProductEntity createProductEntity() {
        return ProductEntity.builder()
                .sellerIdx(1L)
                .productsCode(UUID.randomUUID().toString())
                .productsName("Test Product")
                .price(BigDecimal.valueOf(10000))
                .salePrice(BigDecimal.valueOf(9000))
                .stock(100)
                .status(ProductStatus.ON_SALE)
                .build();
    }

    @Test
    @DisplayName("상품 등록 성공 - SELLER 권한")
    void addProduct_Success() {
        // Given
        Long userIdx = 1L;
        UpsertProductRequest request = createUpsertRequest();
        ProductEntity savedProduct = createProductEntity();

        given(authServiceClient.getUserRole(userIdx)).willReturn("SELLER");
        given(productRepository.save(any(ProductEntity.class))).willReturn(savedProduct);
        given(optionRepository.saveAll(any())).willReturn(List.of());
        given(imageRepository.saveAll(any())).willReturn(List.of());

        // When
        ProductDetailResponse response = productService.addProduct(userIdx, request);

        // Then
        assertThat(response).isNotNull();
        verify(productRepository, times(1)).save(any(ProductEntity.class));
        verify(optionRepository, times(1)).saveAll(any());
        verify(imageRepository, times(1)).saveAll(any());
    }

    @Test
    @DisplayName("상품 등록 실패 - 권한 없음")
    void addProduct_Fail_Unauthorized() {
        // Given
        Long userIdx = 1L;
        UpsertProductRequest request = createUpsertRequest();
        given(authServiceClient.getUserRole(userIdx)).willReturn("USER"); // SELLER가 아님

        // When & Then
        assertThatThrownBy(() -> productService.addProduct(userIdx, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("판매자 권한이 없습니다.");
    }

    @Test
    @DisplayName("상품 상세 조회 성공 - 본인(SELLER)")
    void getManagerProductDetail_Success() {
        // Given
        Long userIdx = 1L;
        String productCode = "some-uuid";
        ProductEntity product = createProductEntity();

        given(authServiceClient.getUserRole(userIdx)).willReturn("SELLER");
        given(productRepository.findByProductsCodeAndDelFalse(productCode)).willReturn(Optional.of(product));
        given(imageRepository.findByProductAndDelFalseOrderByIsThumbnailDescSortOrdersAsc(product)).willReturn(List.of());
        given(optionRepository.findByProductAndDelFalseOrderBySortOrdersAsc(product)).willReturn(List.of());

        // When
        ProductDetailResponse response = productService.getManagerProductDetail(userIdx, productCode);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.name()).isEqualTo(product.getProductsName());
    }

    @Test
    @DisplayName("상품 수정 성공 - 옵션 추가 및 기존 이미지 삭제 후 재생성")
    void updateProduct_Success() {
        // Given
        Long userIdx = 1L;
        String productCode = "some-uuid";
        UpsertProductRequest request = createUpsertRequest();

        ProductEntity product = createProductEntity();

        List<ProductOptionEntity> existingOptions = new ArrayList<>();
        List<ProductImageEntity> existingImages = new ArrayList<>();
        existingImages.add(ProductImageEntity.builder().product(product).url("old.jpg").build());

        given(authServiceClient.getUserRole(userIdx)).willReturn("SELLER");
        given(productRepository.findByProductsCodeAndDelFalse(productCode)).willReturn(Optional.of(product));
        given(optionRepository.findByProductAndDelFalseOrderBySortOrdersAsc(product)).willReturn(existingOptions);
        given(imageRepository.findByProductAndDelFalse(product)).willReturn(existingImages);

        // When
        ProductDetailResponse response = productService.updateProduct(userIdx, productCode, request);

        // Then
        assertThat(product.getProductsName()).isEqualTo(request.name());
        verify(optionRepository, times(1)).saveAll(any());

        assertThat(existingImages.get(0).getDel()).isTrue();
        verify(imageRepository, times(1)).saveAll(any());
    }

    @Test
    @DisplayName("상품 수정 실패 - 존재하지 않는 상품")
    void updateProduct_Fail_NotFound() {
        // Given
        Long userIdx = 1L;
        String productCode = "unknown-code";
        UpsertProductRequest request = createUpsertRequest();

        given(authServiceClient.getUserRole(userIdx)).willReturn("SELLER");
        given(productRepository.findByProductsCodeAndDelFalse(productCode)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> productService.updateProduct(userIdx, productCode, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재 하지 않는 상품입니다.");
    }

    @Test
    @DisplayName("상품 삭제 성공 - Soft Delete 확인")
    void deleteProduct_Success() {
        // Given
        Long userIdx = 1L;
        String productCode = "some-uuid";
        ProductEntity product = createProductEntity();

        ProductOptionEntity option = ProductOptionEntity.builder().product(product).build();
        ProductImageEntity image = ProductImageEntity.builder().product(product).build();

        given(authServiceClient.getUserRole(userIdx)).willReturn("SELLER");
        given(productRepository.findByProductsCodeAndDelFalse(productCode)).willReturn(Optional.of(product));
        given(optionRepository.findByProductAndDelFalse(product)).willReturn(List.of(option));
        given(imageRepository.findByProductAndDelFalse(product)).willReturn(List.of(image));

        // When
        productService.deleteProduct(userIdx, productCode);

        // Then
        assertThat(product.getDel()).isTrue();
        assertThat(option.getDel()).isTrue();
        assertThat(image.getDel()).isTrue();
    }

    @Test
    @DisplayName("판매자 상품 리스트 조회 (ES) - 검색어 없음")
    void getListsBySeller_Success_NoSearch() {
        // Given
        Long userIdx = 1L;
        ProductListRequest request = new ProductListRequest(null); // 검색어 없음
        Pageable pageable = PageRequest.of(0, 10);

        ProductDocument doc = new ProductDocument();
        doc.setProductsIdx(10L);
        doc.setProductsCode("TEST-CODE-001");
        doc.setProductsName("테스트 상품");
        doc.setPrice(BigDecimal.valueOf(20000));
        doc.setSalePrice(BigDecimal.valueOf(18000));
        doc.setViewCount(0L);
        doc.setSellerIdx(userIdx);
        doc.setStatus("ON_SALE");

        Page<ProductDocument> esPage = new PageImpl<>(List.of(doc));

        given(authServiceClient.getUserRole(userIdx)).willReturn("SELLER");
        given(productEsRepository.findAll(pageable)).willReturn(esPage);

        // When
        ProductListResponse response = productService.getListsBySeller(userIdx, pageable, request);

        // Then
        assertThat(response.items()).hasSize(1);
        assertThat(response.items().get(0).name()).isEqualTo("테스트 상품");
        assertThat(response.items().get(0).price()).isEqualTo(BigDecimal.valueOf(20000));

        verify(productEsRepository).findAll(pageable);
    }

    @Test
    @DisplayName("판매자 상품 리스트 조회 (ES) - 검색어 존재")
    void getListsBySeller_Success_WithSearch() {
        // Given
        Long userIdx = 1L;
        String searchKeyword = "노트북";
        ProductListRequest request = new ProductListRequest(searchKeyword);
        Pageable pageable = PageRequest.of(0, 10);

        ProductDocument doc = new ProductDocument();
        doc.setProductsIdx(20L);
        doc.setProductsCode("NOTEBOOK-001");
        doc.setProductsName("게이밍 노트북");
        doc.setPrice(BigDecimal.valueOf(1500000));
        doc.setSalePrice(BigDecimal.valueOf(1400000));
        doc.setViewCount(50L);
        doc.setSellerIdx(userIdx);

        Page<ProductDocument> esPage = new PageImpl<>(List.of(doc));

        given(authServiceClient.getUserRole(userIdx)).willReturn("SELLER");
        given(productEsRepository.findAllBySellerIdxAndProductsNameAndDelFalse(userIdx, searchKeyword, pageable))
                .willReturn(esPage);

        // When
        ProductListResponse response = productService.getListsBySeller(userIdx, pageable, request);

        // Then
        assertThat(response.items()).hasSize(1);
        assertThat(response.items().get(0).name()).isEqualTo("게이밍 노트북");

        verify(productEsRepository).findAllBySellerIdxAndProductsNameAndDelFalse(userIdx, searchKeyword, pageable);
    }
}