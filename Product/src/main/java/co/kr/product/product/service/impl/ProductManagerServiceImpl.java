package co.kr.product.product.service.impl;


import co.kr.product.product.client.AuthServiceClient;
import co.kr.product.product.model.document.ProductDocument;
import co.kr.product.product.model.dto.request.ProductImagesRequest;
import co.kr.product.product.model.dto.request.ProductListRequest;
import co.kr.product.product.model.dto.request.ProductOptionsRequest;
import co.kr.product.product.model.dto.request.UpsertProductRequest;
import co.kr.product.product.model.dto.response.ProductDetailResponse;
import co.kr.product.product.model.dto.response.ProductListResponse;
import co.kr.product.product.model.dto.response.ProductResponse;
import co.kr.product.product.model.entity.ProductEntity;
import co.kr.product.product.model.entity.ProductImageEntity;
import co.kr.product.product.model.entity.ProductOptionEntity;
import co.kr.product.product.repository.ProductEsRepository;
import co.kr.product.product.repository.ProductImageRepository;
import co.kr.product.product.repository.ProductOptionRepository;
import co.kr.product.product.repository.ProductRepository;
import co.kr.product.product.service.ProductManagerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import static co.kr.product.product.mapper.ProductMapper.toProductDetail;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductManagerServiceImpl implements ProductManagerService {

    private final ProductRepository productRepository;
    private final ProductOptionRepository optionRepository;
    private final ProductImageRepository imageRepository;
    private final ProductEsRepository productEsRepository;
    private final AuthServiceClient authServiceClient;

    @Override
    @Transactional
    public ProductDetailResponse addProduct(Long usersIdx, UpsertProductRequest request){


        // 2. 본인확인
        String role = authServiceClient.getUserRole(usersIdx).getBody();
        if (!"SELLER".equals(role) && !"ADMIN".equals(role)) {
            throw new RuntimeException("판매자 권한이 없습니다.");
        }

        // 3. 상품 저장
        ProductEntity item = ProductEntity.builder()
                .sellerIdx(usersIdx)
                .productsCode(UUID.randomUUID().toString())
                .productsName(request.name())
                .description(request.description())
                .price(request.price())
                .salePrice(request.salePrice())
                .stock(request.stock())
                .status(request.status())
                .build();

        ProductEntity savedItem = productRepository.save(item);


        // 4. Option 저장
        List<ProductOptionEntity> options = request.options().stream()
                .map(requestOpt ->  ProductOptionEntity.builder()
                                .product(savedItem)
                                .optionCode(UUID.randomUUID().toString())
                                .optionName(requestOpt.name())
                                .sortOrders(requestOpt.sortOrder())
                                .optionPrice(requestOpt.price())
                                .optionSalePrice(requestOpt.salePrice())
                                .stock(requestOpt.stock())
                                .status(requestOpt.status())
                                .build()

                        ).toList();

        // 5. 이미지 저장
        List<ProductImageEntity> images = request.images().stream()
                .map(requestImg -> ProductImageEntity.builder()
                        .product(savedItem)
                        .url(requestImg.url())
                        .sortOrders(requestImg.sortOrder())
                        .isThumbnail(requestImg.isThumbnail())
                        .build()
                ).toList();


        List<ProductOptionEntity> savedOpt = optionRepository.saveAll(options);
        List<ProductImageEntity> savedImg = imageRepository.saveAll(images);

        // 반환 데이터 생성
        ProductDetailResponse result = toProductDetail(
                        savedItem,
                        savedOpt,
                        savedImg);
        return result;
    }


    @Override
    @Transactional
    public ProductDetailResponse getManagerProductDetail(Long usersIdx, String code){

        // 본인 확인
        String role = authServiceClient.getUserRole(usersIdx).getBody();
        if (!"SELLER".equals(role) && !"ADMIN".equals(role)) {
            throw new RuntimeException("권한이 없습니다.");
        }

        // 상품 조회
        ProductEntity product =  productRepository.findByProductsCodeAndDelFalse(code)
                .orElseThrow(() -> new IllegalArgumentException("존재 하지 않는 상품입니다."));
        
        // 이미지/옵션 조회
        List<ProductImageEntity> images = imageRepository
                .findByProductAndDelFalseOrderByIsThumbnailDescSortOrdersAsc(product);

        List<ProductOptionEntity> options = optionRepository
                .findByProductAndDelFalseOrderBySortOrdersAsc(product);

        // mapper 사용
        return toProductDetail(
                product,
                options,
                images
        );
    }

    /**
     * 상품 정보 업데이트
     * @param usersIdx
     * @param code
     * @param request
     * @return
     */
    @Override
    @Transactional
    public ProductDetailResponse updateProduct(
            Long usersIdx,
            String code,
            UpsertProductRequest request){

        // 본인 확인
        String role = authServiceClient.getUserRole(usersIdx).getBody();
        if (!"SELLER".equals(role) && !"ADMIN".equals(role)) {
            throw new RuntimeException("권한이 없습니다.");
        }

        // Entity 가져오기
        ProductEntity product = productRepository.findByProductsCodeAndDelFalse(code)
                .orElseThrow(() -> new IllegalArgumentException("존재 하지 않는 상품입니다."));

        List<ProductOptionEntity> options = optionRepository.findByProductAndDelFalseOrderBySortOrdersAsc(product);

        Map<String, ProductOptionEntity> optionMap = options.stream()
                .collect(Collectors.toMap(ProductOptionEntity::getOptionCode, Function.identity()));

        // 빈 리스트 생성 (추가 옵션,이미지를 담기 위함)
        List<ProductOptionEntity> newOptions = new ArrayList<>();
        List<ProductImageEntity> newImages = new ArrayList<>();

        // 상품 update 진행
        product.update(request.name(),request.description(),request.price(),
                request.salePrice(),request.stock(),request.status());


        // 옵션 update 진행
        // 추가 된 옵션, 삭제 된 옵션, 수정 된 옵션 전부 처리
        for(ProductOptionsRequest dto : request.options()){
            // 추가 된 옵션
            if(dto.code() == null){
                newOptions.add( ProductOptionEntity.builder()
                                .product(product)
                                .optionCode(UUID.randomUUID().toString())
                                .optionName(dto.name())
                                .sortOrders(dto.sortOrder())
                                .optionPrice(dto.price())
                                .optionSalePrice(dto.salePrice())
                                .stock(dto.stock())
                                .status(dto.status())
                                .build()

                        );



            }
            // 수정 된 옵션
            else{
                ProductOptionEntity entity = optionMap.get(dto.code());
                if (entity != null){
                    entity.update(dto.name(), dto.sortOrder(), dto.price(), dto.salePrice(),dto.stock(),dto.status());

                    optionMap.remove(dto.code());
                }

            }
        }
        // 제거 된 옵션(요청에 없음)
        for (ProductOptionEntity remain : optionMap.values()){
            remain.delete();
        }
        // 반환 데이터 생성을 위해 리스트에서도 제거
        options.removeAll(optionMap.values());

        // 위에서 찾은 추가 옵션 저장
        if(!newOptions.isEmpty()){
            List<ProductOptionEntity> newOptionsList = optionRepository.saveAll(newOptions);
            // 반환 데이터 생성을 위해 리스트 연결
            options.addAll(newOptionsList);
        }



        // 이미지 수정
        // 이미지는 옵션과 달리 product 에만 이어져 있기에 삭제 후 다시 생성해도 큰 문제가 없을것이라 생각.
        List<ProductImageEntity> currentImages = imageRepository.findByProductAndDelFalse(product);
        // softDelete
        for(ProductImageEntity imageEntity : currentImages){
            imageEntity.delete();
        }
        if (request.images() != null) {
            // 추가
            for (ProductImagesRequest imageDto : request.images()) {
                newImages.add(ProductImageEntity.builder()
                        .product(product)
                        .url(imageDto.url())
                        .sortOrders(imageDto.sortOrder())
                        .isThumbnail(imageDto.isThumbnail())
                        .build());
            }
        }
        List<ProductImageEntity> images = imageRepository.saveAll(newImages);

        // mapper 사용
        return toProductDetail(
                product,
                options,
                images
        );
    }


    @Override
    @Transactional
    public void deleteProduct(Long usersIdx, String code){

        // 본인 확인
        String role = authServiceClient.getUserRole(usersIdx).getBody();
        if (!"SELLER".equals(role) && !"ADMIN".equals(role)) {
            throw new RuntimeException("권한이 없습니다.");
        }

        // 엔티티 가져오기
        ProductEntity product = productRepository.findByProductsCodeAndDelFalse(code)
                .orElseThrow(() -> new IllegalArgumentException("존재 하지 않는 상품입니다."));
        List<ProductOptionEntity> options = optionRepository.findByProductAndDelFalse(product);
        List<ProductImageEntity> images = imageRepository.findByProductAndDelFalse(product);

        if(product.getDel())
        {
            log.info("이미 삭제된 상품입니다 productCode:" + code);
        }
        else{
            product.delete();
        }
        // 옵션 삭제
        try{
            options.forEach(ProductOptionEntity::delete);
        } catch (Exception e) {
            throw new IllegalArgumentException("옵션 삭제 실패.");
        }

        // 이미지 삭제
        try{
            images.forEach(ProductImageEntity::delete);
        } catch (Exception e) {
            throw new IllegalArgumentException("이미지 삭제 실패.");
        }

    }

    @Override
    @Transactional(readOnly = true)
    public ProductListResponse getListsBySeller(Long usersIdx, Pageable pageable, ProductListRequest requests){


        String role = authServiceClient.getUserRole(usersIdx).getBody();
        if (!"SELLER".equals(role)) {
            throw new RuntimeException("판매자가 아닙니다.");
        }

        // 검색어 존재 시 검색 진행
        Page<ProductDocument> pageResult = (requests.search() == null || requests.search().isBlank())
                ? productEsRepository.findAll(pageable)
                : productEsRepository.findAllBySellerIdxAndProductsNameAndDelFalse(usersIdx,requests.search() ,pageable);
        
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
                items

        );
    }
}
