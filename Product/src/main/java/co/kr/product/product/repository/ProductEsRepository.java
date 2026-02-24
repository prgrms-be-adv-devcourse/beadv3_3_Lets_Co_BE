package co.kr.product.product.repository;

import co.kr.product.product.model.document.ProductDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductEsRepository extends ElasticsearchRepository<ProductDocument, String> {


    Page<ProductDocument> findByProductsNameAndDelFalse(String productsName, Pageable pageable);

    Page<ProductDocument> findAllBySellerIdxAndProductsNameAndDelFalse(Long sellerIdx, String search, Pageable pageable);

    Page<ProductDocument> findAllByCategoryNamesAndDelFalse(String categoryNames, Pageable pageable);

    Page<ProductDocument> findAllByProductsNameAndCategoryNamesAndDelFalse(String search, String categoryNames, Pageable pageable);
}
