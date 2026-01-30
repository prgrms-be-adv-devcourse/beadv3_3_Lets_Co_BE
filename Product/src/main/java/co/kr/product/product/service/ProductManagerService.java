package co.kr.product.product.service;

import co.kr.product.product.model.dto.response.ProductDetailResponse;
import co.kr.product.product.model.dto.request.ProductListRequest;
import co.kr.product.product.model.dto.request.UpsertProductRequest;
import co.kr.product.product.model.dto.response.ProductListResponse;
import org.springframework.data.domain.Pageable;

public interface ProductManagerService {

    ProductDetailResponse addProduct(Long usersIdx, UpsertProductRequest request);

    ProductDetailResponse getManagerProductDetail(Long usersIdx, String code);

    ProductDetailResponse updateProduct(Long usersIdx, String code, UpsertProductRequest request);

    void deleteProduct(Long usersIdx, String code);

    ProductListResponse getListsBySeller(Long usersIdx, Pageable pageable, ProductListRequest requests);
}
