package co.kr.product.product.service.impl;

import co.kr.product.common.service.S3Service;
import co.kr.product.product.model.document.ProductDocument;
import co.kr.product.product.model.dto.request.ProductListReq;
import co.kr.product.product.model.dto.response.ProductListRes;
import co.kr.product.product.model.dto.response.ProductRes;
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
    private final S3Service s3Service;

    // 상품 리스트 전체/검색
    @Transactional(readOnly = true)
    public ProductListRes getProductsList(Pageable pageable, ProductListReq request){

        // TODO 벡터검색 까지 하려면 지금 못건듬....어차피 싹 고쳐야함...
        // Category 별 조회, ip별 조회까지 여기서 처리

        String search = request.search();
        String category = request.category();
        String ip = request.ip();

        // 1. 검색
        Page<ProductDocument> pageResult;


        pageResult = productEsRepository.findByProductsNameAndDelFalse(search, pageable);



        // 2. Document -> Response DTO 변환
        List<ProductRes> items = pageResult.stream()
                .map(doc -> new ProductRes(
                        doc.getProductsCode(),
                        doc.getProductsName(),
                        doc.getPrice(),
                        doc.getSalePrice(),
                        doc.getViewCount(),
                        doc.getStatus(),
                        doc.getCategoryNames(),
                        s3Service.getFileUrl(doc.getThumbnailKey())
                ))
                .toList();

        return new ProductListRes(
                items

        );
    }

}
