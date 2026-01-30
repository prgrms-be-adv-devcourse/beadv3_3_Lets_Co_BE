package co.kr.product.product.service.impl;

import co.kr.product.product.model.dto.request.DeductStockReq;
import co.kr.product.product.model.dto.request.ProductIdxsReq;
import co.kr.product.product.model.dto.request.ProductInfoToOrderReq;
import co.kr.product.product.model.dto.response.*;
import co.kr.product.product.model.entity.ProductEntity;
import co.kr.product.product.model.entity.ProductImageEntity;
import co.kr.product.product.model.entity.ProductOptionEntity;
import co.kr.product.product.repository.ProductImageRepository;
import co.kr.product.product.repository.ProductOptionRepository;
import co.kr.product.product.repository.ProductRepository;
import co.kr.product.product.service.ProductService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static co.kr.product.product.mapper.ProductMapper.toProductDetail;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductOptionRepository productOptionRepository;

    /**
     * 상품 목록 조회 (비회원/회원 모두)
     * 지금은 사용 안함. > Elastic에서 처리
     */
    @Override
    @Transactional(readOnly = true)
    public ProductListRes getProducts(Pageable pageable) {
        Page<ProductEntity> page = productRepository.findByDelFalse(pageable);

        List<ProductRes> items = page.getContent().stream()
                .map(p -> new ProductRes(
                        p.getProductsIdx(),
                        p.getProductsCode(),
                        p.getProductsName(),
                        p.getPrice(),
                        p.getSalePrice(),
                        p.getViewCount()
                ))
                .toList();

        return new ProductListRes("SUCCESS", items);
    }

    /**
     * 상품 상세 조회 (비회원/회원 모두)
     * - 조회수 증가 포함
     * - 이미지/옵션 포함
     */
    @Override
    @Transactional(readOnly = true)
    public ProductDetailRes getProductDetail(String productsCode) {

        ProductEntity productEntity = productRepository.findByProductsCodeAndDelFalse(productsCode)
                .orElseThrow(() -> new EntityNotFoundException("Product not found: " + productsCode));

        // 조회수 증가 (서비스에서 처리)
        // @Modifying 쿼리로 원자적 증가도 가능
        // product.increaseViewCount(); // 메서드 만들었으면 사용
        // 메서드가 주석이라면 아래처럼 직접 증가
        // Long vc = (productEntity.getViewCount() == null ? 0L : productEntity.getViewCount());
        // 리플렉션 없이는 setter가 없으니 "증가 메서드"를 Product에 다시 넣는 걸 권장
        // 여기서는 안전하게 update 쿼리로 처리하도록 아래 방식 추천:
        // -> 아래 5번에서 개선안 제공


        // 아래 코드의 경우 return 한 productEntity에서는 증가된 조회수가 적용 안 됨.
        // 하지만 이를위해 select를 한 번 더 쓰는것 보단 이게 좋다고 봅니다
        productEntity.increaseViewCount();


        // 이미지/옵션 조회
        List<ProductImageEntity> images = productImageRepository
                .findByProductAndDelFalseOrderByIsThumbnailDescSortOrdersAsc(productEntity);

        List<ProductOptionEntity> options = productOptionRepository
                .findByProductAndDelFalseOrderBySortOrdersAsc(productEntity);

        return toProductDetail(
                "success",
                productEntity,
                options,
                images
        );
    }

    /**
     *  상품 재고 체크
     * @param productsCode
     * @return resultCode, isInStock
     * 지금은 boolean을 통해 재고 여부를 알려주지만, 이후 상의해봐야함
     */
    @Override
    @Transactional(readOnly = true)
    public ProductCheckStockRes getCheckStock(String productsCode) {

        // TODO : option id or code 를 받아오는것이 훨 좋음

        ProductEntity product = productRepository.findByProductsCodeAndDelFalse(productsCode)
                .orElseThrow(() -> new IllegalArgumentException("존재 하지 않는 상품입니다."));

        List<ProductOptionEntity> options = productOptionRepository.findByProductAndDelFalse(product);

        List<OptionCheckStockRes> optionStockResponses = options.stream().map(
                option -> new OptionCheckStockRes(
                        option.getOptionCode(),
                        option.getStock())

        ).toList();

        return new ProductCheckStockRes(
                "Success",
                product.getStock(),
                optionStockResponses
        );

    }

    @Override
    @Transactional
    public void deductStock(DeductStockReq request){

        // 쿼리문에서 남은 개수 확인 및 수정까지
        // kafka를 쓰더라도 컨슈머의 개수를 늘리면 동시 접속 문제가 생길 수도 있음.
        // 최대한 수정 과정을 짧게 처리하기위해 쿼리문 하나로 해결해보고자 함
        int affectedRow = productOptionRepository.decreaseStock(request.optionIdx(), request.quantity());

        if (affectedRow == 0){
            throw new IllegalArgumentException("재고가 부족하거나 유효하지 않은 상품입니다. (OptionIdx: " + request.optionIdx() + ")");
        }

    }

    @Override
    @Transactional
    public void deductStocks(List<DeductStockReq> requests){


        for (DeductStockReq request : requests) {


            int affectedRows = productOptionRepository.decreaseStock(
                    request.optionIdx(),
                    request.quantity()
            );

            // 업데이트된 행이 0개면 문제 있는것
            if (affectedRows == 0) {
                throw new IllegalArgumentException("재고가 부족하거나 유효하지 않은 상품입니다. (OptionIdx: " + request.optionIdx() + ")");
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ProductInfoToOrderRes getProductInfo(Long productsIdx, Long optionIdx){
        ProductEntity product = productRepository.findByProductsIdxAndDelFalse(productsIdx)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 상품입니다: " + productsIdx));

        ProductOptionEntity option = productOptionRepository.findByOptionGroupIdxAndDelFalse(optionIdx)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 옵션입니다: " + optionIdx));


        // List<ProductImageEntity> image = productImageRepository.findByProductAndDelFalse(product)

        return new ProductInfoToOrderRes(
                productsIdx,
                optionIdx,
                product.getSellerIdx(),
                product.getProductsName(),
                option.getOptionName(),
                option.getOptionPrice(),
                // option.getOptionSalePrice(),
                option.getStock()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductInfoToOrderRes> getProductInfoList(List<ProductInfoToOrderReq> requests){

        // ** for문을 돌려 List의 길이만큼 select를 사용하는것은 좋지 않음
        // 1) IN 쿼리를 사용해서 한번에 조회
        // 2) Fetch Join으로 가져오기

        // > Fetch Join 쓰는 방법이 더 간단하고 성능적으로 좋다고 함. < 공부 필요

        // 1. List 내 optionIds 만 list 로 추출
        List<Long> optionIds = requests.stream()
                .map(ProductInfoToOrderReq::optionIdx).toList();

        // 2. 조회
        List<ProductOptionEntity> options = productOptionRepository.findAllWithOptions(optionIds);

        // 3. 반환
        return options.stream().map(opt -> new ProductInfoToOrderRes(
                opt.getProduct().getProductsIdx(), // 상품 정보도 이미 들어있음
                opt.getOptionGroupIdx(),
                opt.getProduct().getSellerIdx(),
                opt.getProduct().getProductsName(),
                opt.getOptionName(),
                opt.getOptionPrice(),
                opt.getStock()
        )).toList();

    }


    @Override
    @Transactional(readOnly = true)
    public Map<Long, Long> getSellersByProductIds(List<Long> productIds) {
        return productRepository.findAllById(productIds).stream()
                .collect(Collectors.toMap(
                        ProductEntity::getProductsIdx,
                        ProductEntity::getSellerIdx
                ));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductInfoRes> getProductInfoForBoard(ProductIdxsReq request){
        List<ProductEntity> productInfoEntities = productRepository.findAllByProductsIdxInAndDelFalse(request.productIdxs());

        List<ProductImageEntity> thumbnails = productImageRepository.findAllByProduct_ProductsIdxInAndIsThumbnailTrueAndDelFalse(request.productIdxs());

        // 3. 조회를 빠르게 하기 위해 Map으로 변환 (Key: 상품IDX, Value: 이미지URL)
        Map<Long, String> imageUrlMap = thumbnails.stream()
                .collect(Collectors.toMap(
                        img -> img.getProduct().getProductsIdx(), // ProductImageEntity -> ProductEntity -> IDX
                        ProductImageEntity::getUrl,
                        (existing, replacement) -> existing // 혹시 중복이 있다면 첫 번째 것 유지
                ));
        return productInfoEntities.stream()
                .map(entity -> new ProductInfoRes(
                        entity.getProductsIdx(), // 필드명은 엔티티 설정에 맞춰 확인 필요 (예: getProductIdx)
                        entity.getProductsCode(),
                        entity.getProductsName(),
                        imageUrlMap.get(entity.getProductsIdx())
                ))
                .toList();



    }

    @Override
    @Transactional(readOnly = true)
    public ProductSellerRes getSellerIdx(Long productsIdx){
        ProductEntity product = productRepository.findByProductsIdxAndDelFalse(productsIdx)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 상품입니다: " + productsIdx));
        return new ProductSellerRes(product.getSellerIdx());
    };
}



