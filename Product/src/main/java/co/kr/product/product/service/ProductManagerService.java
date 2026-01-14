package co.kr.product.product.service;

import co.kr.product.product.dto.response.ProductDetailResponse;
import co.kr.product.product.dto.request.ProductListRequest;
import co.kr.product.product.dto.request.UpsertProductRequest;
import co.kr.product.product.dto.response.ProductListResponse;
import org.springframework.data.domain.Pageable;

public interface ProductManagerService {

    ProductDetailResponse addProduct(String accountCode, UpsertProductRequest request);

    ProductDetailResponse getManagerProductDetail(String accountCode, String code);

    ProductDetailResponse updateProduct(String accountCode, String code, UpsertProductRequest request);

    void deleteProduct(String accountCode, String code);

    ProductListResponse getListsBySeller(String accountCode, Pageable pageable, ProductListRequest requests);
}
