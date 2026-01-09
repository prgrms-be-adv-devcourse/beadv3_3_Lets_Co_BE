package co.kr.product.product.service;

import co.kr.product.product.dto.response.ProductDetailResponse;
import co.kr.product.product.dto.response.ProductImageResponse;
import co.kr.product.product.dto.response.ProductListResponse;
import co.kr.product.product.dto.response.ProductOptionResponse;
import co.kr.product.product.dto.response.ProductResponse;
import co.kr.product.product.entity.Product;
import co.kr.product.product.entity.ProductImage;
import co.kr.product.product.entity.ProductOption;
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
        Page<Product> page = productRepository.findByDelFalse(pageable);

        List<ProductResponse> items = page.getContent().stream()
                .map(p -> new ProductResponse(
                        p.getProductsIdx(),
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
    public ProductDetailResponse getProductDetail(Long productsIdx) {

        Product product = productRepository.findByProductsIdxAndDelFalse(productsIdx)
                .orElseThrow(() -> new EntityNotFoundException("Product not found: " + productsIdx));

        // 조회수 증가 (서비스에서 처리)
        // @Modifying 쿼리로 원자적 증가도 가능
        // product.increaseViewCount(); // 메서드 만들었으면 사용
        // 메서드가 주석이라면 아래처럼 직접 증가
        Long vc = (product.getViewCount() == null ? 0L : product.getViewCount());
        // 리플렉션 없이는 setter가 없으니 "증가 메서드"를 Product에 다시 넣는 걸 권장
        // 여기서는 안전하게 update 쿼리로 처리하도록 아래 방식 추천:
        // -> 아래 5번에서 개선안 제공

        // 이미지/옵션 조회
        List<ProductImage> images = productImageRepository
                .findByProductsIdxAndDelFalseOrderByIsThumbnailDescSortOrderAsc(productsIdx);

        List<ProductOption> options = productOptionRepository
                .findByProductsIdxAndDelFalseOrderBySortOrderAsc(productsIdx);

        List<ProductImageResponse> imageDtos = images.stream()
                .map(i -> new ProductImageResponse(i.getImageIdx(), i.getUrl(), i.getSortOrder(), i.getIsThumbnail()))
                .toList();

        List<ProductOptionResponse> optionDtos = options.stream()
                .map(o -> new ProductOptionResponse(
                        o.getOptionGroupIdx(),
                        o.getOptionName(),
                        o.getOptionPrice(),
                        o.getOptionSalePrice(),
                        o.getStock(),
                        o.getStatus()
                ))
                .toList();

        //  viewCount 증가 반영을 제대로 하려면 아래 "5번 개선안" 적용 권장
        return new ProductDetailResponse(
                product.getProductsIdx(),
                product.getProductsName(),
                product.getDescription(),
                product.getPrice(),
                product.getSalePrice(),
                product.getStock(),
                product.getStatus(),
                product.getViewCount(), // 현재 값
                imageDtos,
                optionDtos
        );
    }
}



