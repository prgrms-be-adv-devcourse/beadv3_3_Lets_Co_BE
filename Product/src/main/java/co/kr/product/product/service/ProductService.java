package co.kr.product.product.service;

import co.kr.product.product.dto.response.ProductCheckStockResponse;
import co.kr.product.product.dto.response.ProductDetailResponse;
import co.kr.product.product.dto.response.ProductListResponse;
import org.springframework.data.domain.Pageable;

public interface ProductService {
    ProductListResponse getProducts(Pageable pageable);


    ProductDetailResponse getProductDetail(String productsCode);


    ProductCheckStockResponse getCheckStock(String productsCode);
}
