package co.kr.product.seller.service;

import co.kr.product.seller.document.ProductDocument;

import java.util.List;

public interface ProductSearchService {
    List<ProductDocument> search();
}
