package co.kr.product.product.repository;

import co.kr.product.product.model.document.ProductDocument;
import co.kr.product.product.model.dto.request.ProductListReq;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductEsCustomRepository {
    Page<ProductDocument> searchProducts(List<Float> queryVector, ProductListReq request , Pageable pageable);
}
