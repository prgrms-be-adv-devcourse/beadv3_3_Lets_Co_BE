package co.kr.product.product.service;

import co.kr.product.product.model.dto.response.ProductListResponse;
import org.springframework.data.domain.Pageable;

public interface ProductSearchService {

    ProductListResponse getProductsList(Pageable pageable,String search);
}
