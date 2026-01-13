package co.kr.product.product.service;

import co.kr.product.product.document.ProductDocument;
import co.kr.product.product.dto.request.ProductListRequest;
import co.kr.product.product.dto.response.ProductListResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductSearchService {
    List<ProductDocument> search();

    ProductListResponse getProductsList(Pageable pageable,String search);
}
