package co.kr.product.product.service;

import co.kr.product.product.dto.response.ProductDetailRes;
import co.kr.product.product.dto.request.ProductListReq;
import co.kr.product.product.dto.request.UpsertProductReq;
import co.kr.product.product.dto.response.ProductListRes;
import org.springframework.data.domain.Pageable;

public interface ProductManagerService {

    ProductDetailRes addProduct(Long usersIdx, UpsertProductReq request);

    ProductDetailRes getManagerProductDetail(Long usersIdx, String code);

    ProductDetailRes updateProduct(Long usersIdx, String code, UpsertProductReq request);

    void deleteProduct(Long usersIdx, String code);

    ProductListRes getListsBySeller(Long usersIdx, Pageable pageable, ProductListReq requests);
}
