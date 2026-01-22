package co.kr.product.product.service;

import co.kr.product.product.dto.request.DeductStockRequest;
import co.kr.product.product.dto.request.ProductInfoToOrderRequest;
import co.kr.product.product.dto.response.ProductCheckStockResponse;
import co.kr.product.product.dto.response.ProductDetailResponse;
import co.kr.product.product.dto.response.ProductInfoToOrderResponse;
import co.kr.product.product.dto.response.ProductListResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductService {
    ProductListResponse getProducts(Pageable pageable);


    ProductDetailResponse getProductDetail(String productsCode);


    ProductCheckStockResponse getCheckStock(String productsCode);

    void deductStock(DeductStockRequest deductStockRequest);

    void deductStocks(List<DeductStockRequest> deductStockRequest);

    ProductInfoToOrderResponse getProductInfo(Long productsIdx, Long optionIdx);

    List<ProductInfoToOrderResponse> getProductInfoList(List<ProductInfoToOrderRequest> requests);

    Map<Long, Long> getSellersByProductIds(List<Long> productIds) ;
}
