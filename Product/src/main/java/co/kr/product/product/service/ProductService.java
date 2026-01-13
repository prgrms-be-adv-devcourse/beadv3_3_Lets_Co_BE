package co.kr.product.product.service;

import co.kr.product.product.dto.response.*;
import co.kr.product.product.dto.vo.ProductStatus;
import co.kr.product.product.entity.ProductEntity;
import co.kr.product.product.entity.ProductImageEntity;
import co.kr.product.product.entity.ProductOptionEntity;
import co.kr.product.product.repository.ProductImageRepository;
import co.kr.product.product.repository.ProductOptionRepository;
import co.kr.product.product.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static co.kr.product.product.mapper.ProductMapper.toProductDetail;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductOptionRepository productOptionRepository;

    /**
     * 상품 목록 조회 (비회원/회원 모두)
     */
    @Transactional(readOnly = true)
    public ProductListResponse getProducts(Pageable pageable) {
        Page<ProductEntity> page = productRepository.findByDelFalse(pageable);

        List<ProductResponse> items = page.getContent().stream()
                .map(p -> new ProductResponse(
                        p.getProductsIdx(),
                        p.getProductsCode(),
                        p.getProductsName(),
                        p.getPrice(),
                        p.getSalePrice(),
                        p.getViewCount()
                ))
                .toList();

        return new ProductListResponse("SUCCESS", items);
    }

    /**
     * 상품 상세 조회 (비회원/회원 모두)
     * - 조회수 증가 포함
     * - 이미지/옵션 포함
     */
    @Transactional
    public ProductDetailResponse getProductDetail(String productsCode) {

        ProductEntity productEntity = productRepository.findByProductsCodeAndDelFalse(productsCode)
                .orElseThrow(() -> new EntityNotFoundException("Product not found: " + productsCode));

        // 조회수 증가 (서비스에서 처리)
        // @Modifying 쿼리로 원자적 증가도 가능
        // product.increaseViewCount(); // 메서드 만들었으면 사용
        // 메서드가 주석이라면 아래처럼 직접 증가
        // Long vc = (productEntity.getViewCount() == null ? 0L : productEntity.getViewCount());
        // 리플렉션 없이는 setter가 없으니 "증가 메서드"를 Product에 다시 넣는 걸 권장
        // 여기서는 안전하게 update 쿼리로 처리하도록 아래 방식 추천:
        // -> 아래 5번에서 개선안 제공

        // 해당 경우 return 한 productEntity에서는 증가된 조회수가 적용 안 됨.
        // 하지만 이를위해 select를 한 번 더 쓰는것 보단 이게 좋다고 봅니다.
        productEntity.increaseViewCount();

        // 이미지/옵션 조회
        List<ProductImageEntity> images = productImageRepository
                .findByProductAndDelFalseOrderByIsThumbnailDescSortOrdersAsc(productEntity);

        List<ProductOptionEntity> options = productOptionRepository
                .findByProductAndDelFalseOrderBySortOrdersAsc(productEntity);

        return toProductDetail(
                "success",
                productEntity,
                options,
                images
        );
    }

    public ProductCheckStockResponse getCheckStock(String productsCode) {
        ProductEntity product = productRepository.findByProductsCodeAndDelFalse(productsCode)
                .orElseThrow(() -> new IllegalArgumentException("존재 하지 않는 상품입니다."));
        if (product.getStock() <= 0 && product.getStatus().equals(ProductStatus.SOLD_OUT.name())) {
            return new ProductCheckStockResponse(
                    "Success",
                    false
            );
        }
        else {
            return new ProductCheckStockResponse(
                    "Success",
                    true
            );
        }
    }
}



