package co.kr.product.product.service;

import co.kr.product.product.dto.response.ProductListRes;
import org.springframework.data.domain.Pageable;

public interface ProductSearchService {

    ProductListRes getProductsList(Pageable pageable, String search);
}
