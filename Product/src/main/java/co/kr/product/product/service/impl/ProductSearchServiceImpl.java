package co.kr.product.product.service.impl;

import co.kr.product.product.document.ProductDocument;
import co.kr.product.product.dto.response.ProductListResponse;
import co.kr.product.product.dto.response.ProductResponse;
import co.kr.product.product.repository.ProductEsRepository;
import co.kr.product.product.service.ProductSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductSearchServiceImpl implements ProductSearchService {

    private final ProductEsRepository productEsRepository;


    // 상품 리스트 전체/검색
    @Transactional(readOnly = true)
    public ProductListResponse getProductsList(Pageable pageable, String search){
        
        // 1. 검색 (search 없을 시 전체 리스트 반환)
        Page<ProductDocument> pageResult = (search == null || search.isBlank())
                ? productEsRepository.findAll(pageable)
                : productEsRepository.findByProductsNameAndDelFalse(search, pageable);

        // 2. Document -> Response DTO 변환
        List<ProductResponse> items = pageResult.stream()
                .map(doc -> new ProductResponse(
                        doc.getProductsIdx(),
                        doc.getProductsCode(),
                        doc.getProductsName(),
                        doc.getPrice(),
                        doc.getSalePrice(),
                        doc.getViewCount()
                ))
                .toList();

        return new ProductListResponse(
                "ok",items

        );
    }

}
