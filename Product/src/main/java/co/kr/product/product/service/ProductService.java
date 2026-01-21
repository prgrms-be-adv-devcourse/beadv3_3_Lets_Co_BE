package co.kr.product.product.service;

import co.kr.product.product.dto.request.DeductStockRequest;
import co.kr.product.product.dto.request.ProductInfoToOrderRequest;
import co.kr.product.product.dto.response.ProductCheckStockResponse;
import co.kr.product.product.dto.response.ProductDetailResponse;
import co.kr.product.product.dto.response.ProductInfoToOrderResponse;
import co.kr.product.product.dto.response.ProductListResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public interface ProductService {
    ProductListResponse getProducts(Pageable pageable);


    ProductDetailResponse getProductDetail(String productsCode);


    ProductCheckStockResponse getCheckStock(String productsCode);

    void deductStock(DeductStockRequest deductStockRequest);

    void deductStocks(List<DeductStockRequest> deductStockRequest);

    /**
     *  상품 재고 체크
     * @param productsCode
     * @return resultCode, isInStock
     * 지금은 boolean을 통해 재고 여부를 알려주지만, 이후 상의해봐야함
     */
    public ProductCheckStockResponse getCheckStock(String productsCode) {
        ProductEntity product = productRepository.findByProductsCodeAndDelFalse(productsCode)
                .orElseThrow(() -> new IllegalArgumentException("존재 하지 않는 상품입니다."));
        if (product.getStock() <= 0 || product.getStatus().equals(ProductStatus.SOLD_OUT)) {
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

    /**
     * 정산용: 상품 ID 목록으로 판매자 ID 조회
     * - Order 서비스에서 정산 생성 시 호출
     *
     * @param productIds 상품 ID 목록
     * @return Map<상품ID, 판매자ID>
     */
    @Transactional(readOnly = true)
    public Map<Long, Long> getSellersByProductIds(List<Long> productIds) {
        return productRepository.findAllById(productIds).stream()
                .collect(Collectors.toMap(
                        ProductEntity::getProductsIdx,
                        ProductEntity::getSellerIdx
                ));
    }
    ProductInfoToOrderResponse getProductInfo(Long productsIdx, Long optionIdx);

    List<ProductInfoToOrderResponse> getProductInfoList(List<ProductInfoToOrderRequest> requests);
}
