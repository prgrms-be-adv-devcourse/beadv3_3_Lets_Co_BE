package co.kr.product.product.service.impl;

import co.kr.product.product.document.ProductDocument;
import co.kr.product.product.dto.request.ProductListRequest;
import co.kr.product.product.dto.response.ProductListResponse;
import co.kr.product.product.dto.response.ProductResponse;
import co.kr.product.product.repository.ProductEsRepository;
import co.kr.product.product.service.ProductSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductSearchServiceImpl implements ProductSearchService {

    private final ProductEsRepository productEsRepository;

    public List<ProductDocument> search(){
        return productEsRepository.findAll();
    }

    public ProductListResponse getProductsList(Pageable pageable, String search){
        Page<ProductDocument> pageResult = (search == null || search.isBlank())
                ? productEsRepository.findAll(pageable)
                : productEsRepository.findByProductsName(search, pageable);

        // 2. Document -> Response DTO 변환
        List<ProductResponse> items = pageResult.stream()
                .map(doc -> new ProductResponse(
                        doc.getProductsIdx(),
                        doc.getProductsCode(),
                        doc.getProductsName(),
                        doc.getPrice(),     // Double -> BigDecimal 변환
                        doc.getSalePrice(), // Double -> BigDecimal 변환
                        doc.getViewCount()
                ))
                .toList();
        return new ProductListResponse(
                "success",items

        );
    }

}
