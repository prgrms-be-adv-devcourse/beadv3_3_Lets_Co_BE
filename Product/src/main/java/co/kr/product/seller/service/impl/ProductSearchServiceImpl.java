package co.kr.product.seller.service.impl;

import co.kr.product.seller.document.ProductDocument;
import co.kr.product.seller.repository.ProductEsRepository;
import co.kr.product.seller.service.ProductSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductSearchServiceImpl implements ProductSearchService {

    private final ProductEsRepository productEsRepository;

    public List<ProductDocument> search(){

        return productEsRepository.findAll();
    }

}
