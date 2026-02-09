package co.kr.product.product.service;

import co.kr.product.common.vo.UserRole;
import co.kr.product.product.model.dto.response.ProductDetailRes;
import co.kr.product.product.model.dto.request.ProductListReq;
import co.kr.product.product.model.dto.request.UpsertProductReq;
import co.kr.product.product.model.dto.response.ProductListRes;
import org.springframework.data.domain.Pageable;

public interface ProductManagerService {

    ProductDetailRes addProduct(Long usersIdx, UpsertProductReq request);

    ProductDetailRes getManagerProductDetail(Long usersIdx, String code);

    ProductDetailRes updateProduct(Long usersIdx, String code, UpsertProductReq request, UserRole inputRole);

    void deleteProduct(Long usersIdx, String code , UserRole inputRole);

    ProductListRes getListsBySeller(Long usersIdx, Pageable pageable, ProductListReq requests);
}
