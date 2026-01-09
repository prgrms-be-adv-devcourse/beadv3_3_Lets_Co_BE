package co.kr.product.seller.service;

import co.kr.product.seller.model.dto.ProductDetailResponse;
import co.kr.product.seller.model.dto.ProductListReponse;
import co.kr.product.seller.model.dto.ProductListRequest;
import co.kr.product.seller.model.dto.UpsertProductRequest;
import org.springframework.data.domain.Pageable;

public interface ProductManagerService {
    ProductListReponse getLists(Pageable pageable, ProductListRequest request);

    ProductDetailResponse addProduct(String accountCode, UpsertProductRequest request);

    ProductDetailResponse getProductDetail(String accountCode, String code);

    ProductDetailResponse updateProduct(String accountCode, String code, UpsertProductRequest request);

    void deleteProduct(String accountCode, String code);
}
