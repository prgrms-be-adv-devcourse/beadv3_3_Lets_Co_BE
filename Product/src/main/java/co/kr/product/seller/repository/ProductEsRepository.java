package co.kr.product.seller.repository;

import co.kr.product.seller.document.ProductDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductEsRepository extends ElasticsearchRepository<ProductDocument, String> {
    @Override
    List<ProductDocument> findAll();
}
