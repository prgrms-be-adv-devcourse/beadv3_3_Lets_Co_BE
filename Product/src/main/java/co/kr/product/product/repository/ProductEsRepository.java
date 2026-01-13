package co.kr.product.product.repository;

import co.kr.product.product.document.ProductDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductEsRepository extends ElasticsearchRepository<ProductDocument, String> {
    @Override
    List<ProductDocument> findAll();

    Page<ProductDocument> findByProductsName(String productsName, Pageable pageable);
}
