package co.kr.product.product.service;

import co.kr.product.product.model.document.ProductDocument;
import co.kr.product.product.model.dto.response.ProductListResponse;
import co.kr.product.product.repository.ProductEsRepository;
import co.kr.product.product.service.impl.ProductSearchServiceImpl;
import org.assertj.core.api.Assertions;
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
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ProductSearchServiceTest {

    @InjectMocks ProductSearchServiceImpl productSearchService;
    @Mock ProductEsRepository productEsRepository;

    @Test
    void 제품_리스트_검색X_findAll() {

        // Given
        Pageable pageable = PageRequest.of(0, 10);
        String search = null;

            // ProductDocument 생성
        ProductDocument doc1 = createDocument(1L, "상품A");
        ProductDocument doc2 = createDocument(2L, "상품B");

        Page<ProductDocument> fakeReturn = new PageImpl<>(List.of(doc1, doc2));

            // pageable로 findAll 할려 할 때 fakeReturn을 return 할것이다
        given(productEsRepository.findAll(pageable)).willReturn(fakeReturn);

        // When
            // 실제로 pageable 하고 search 넣어봄
        ProductListResponse response = productSearchService.getProductsList(pageable, search);

        // then
            // 실제로 findAll 했는지?
        verify(productEsRepository).findAll(pageable);

            // 실제로 값이 맞는지 검증함
        Assertions.assertThat(response.items()).hasSize(2);
        Assertions.assertThat(response.items().get(0).name()).isEqualTo("상품A");
        Assertions.assertThat(response.items().get(1).name()).isEqualTo("상품B");
    }

    @Test
    void 제품_리스트_검색O_findBy() {
        // Given
        String search = "노트북";
        Pageable pageable = PageRequest.of(0, 10);

        ProductDocument doc1 = createDocument(10L, "삼성 노트북");
        Page<ProductDocument> mockPage = new PageImpl<>(List.of(doc1));

        given(productEsRepository.findByProductsNameAndDelFalse(eq(search), any(Pageable.class)))
                .willReturn(mockPage);

        // When
        ProductListResponse response = productSearchService.getProductsList(pageable, search);

        // Then
        verify(productEsRepository).findByProductsNameAndDelFalse(eq(search), any(Pageable.class));

        Assertions.assertThat(response.items()).hasSize(1);
        Assertions.assertThat(response.items().get(0).name()).isEqualTo("삼성 노트북");
    }

    // 테스트용 더미 데이터 생성 헬퍼 메서드
    private ProductDocument createDocument(Long idx, String name) {

        ProductDocument document = new ProductDocument();
        document.setProductsIdx(1L);
        document.setProductsCode(UUID.randomUUID().toString());
        document.setProductsName(name);
        document.setPrice(BigDecimal.valueOf(10000));
        document.setSalePrice(BigDecimal.valueOf(9000));
        document.setViewCount(0L);
        document.setDel(false);

        return document;
    }
}