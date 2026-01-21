package co.kr.product.product.repository;

import co.kr.product.product.document.ProductDocument;
import co.kr.product.product.entity.ProductEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductEsRepository extends ElasticsearchRepository<ProductDocument, String> {


    Page<ProductDocument> findByProductsNameAndDelFalse(String productsName, Pageable pageable);

    Page<ProductDocument> findAllBySellerIdxAndProductsNameAndDelFalse(Long sellerIdx, String search, Pageable pageable);
}
