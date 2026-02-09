package co.kr.product.product.service.impl;


import co.kr.product.common.auth.AuthAdapter;
import co.kr.product.common.exceptionHandler.ForbiddenException;
import co.kr.product.common.service.S3Service;
import co.kr.product.common.vo.UserRole;
import co.kr.product.product.mapper.ProductMapper;
import co.kr.product.product.model.document.ProductDocument;
import co.kr.product.product.model.dto.request.CategoryParentGroup;
import co.kr.product.product.model.dto.request.ProductListReq;
import co.kr.product.product.model.dto.request.ProductOptionsReq;
import co.kr.product.product.model.dto.request.UpsertProductReq;
import co.kr.product.product.model.dto.response.*;
import co.kr.product.product.model.entity.FileEntity;
import co.kr.product.product.model.entity.ProductCategoryEntity;
import co.kr.product.product.model.entity.ProductEntity;
import co.kr.product.product.model.entity.ProductOptionEntity;
import co.kr.product.product.model.vo.CategoryType;
import co.kr.product.product.repository.*;
import co.kr.product.product.service.ProductManagerService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static co.kr.product.product.mapper.ProductMapper.toProductDetail;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductManagerServiceImpl implements ProductManagerService {

    private final ProductRepository productRepository;
    private final ProductOptionRepository optionRepository;
    private final ProductEsRepository productEsRepository;
    private final ProductCategoryRepository categoryRepository;
    private final S3Service s3Service;
    private final AuthAdapter authAdapter;
    private final FileRepository fileRepository;

    @Value("${custom.aws.s3.product-prefix}")
    private String productPrefix;


    @Override
    @Transactional
    public ProductDetailRes addProduct(Long usersIdx, UpsertProductReq request, List<MultipartFile> images){
/*

        // 1. 본인확인
        String role = authAdapter.getUserRole(usersIdx);
        // SELLER 또는 ADMIN이 아닌경우
        if (!UserRole.isStaff(role)) {
            throw new ForbiddenException("권한이 없습니다.");
        }
*/

        // 2. 카테고리 및 ip 불러오기
        // 2.1. in 쿼리로 카테고리, ip 동시 조회

        List<String> codes = List.of(request.categoryCode(), request.ipCode());
        List<ProductCategoryEntity> categoryEntities = categoryRepository.findAllByCategoryCodeInAndDelFalse(codes);

        // 2.2. Category, Ip 구분
        ProductCategoryEntity category = ProductMapper.findByType(categoryEntities,CategoryType.CATEGORY).stream()
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("존재 하지 않는 카테고리입니다."));
        ProductCategoryEntity ip = ProductMapper.findByType(categoryEntities,CategoryType.IP).stream()
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("존재 하지 않는 ip입니다."));


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
                .category(category)
                .ip(ip)
                .build();

        ProductEntity savedItem = productRepository.save(item);


        // 4. Option 저장
        List<ProductOptionEntity> options = request.options().stream()
                .map(requestOpt ->  ProductOptionEntity.builder()
                        .optionCode(UUID.randomUUID().toString())
                        .optionName(requestOpt.name())
                        .sortOrders(requestOpt.sortOrder())
                        .optionPrice(requestOpt.price())
                        .optionSalePrice(requestOpt.salePrice())
                        .stock(requestOpt.stock())
                        .status(requestOpt.status())
                        .build()

                ).toList();


        List<ProductOptionEntity> savedOpt = optionRepository.saveAll(options);

        // 5. 이미지 저장
        // 5-1. S3에 이미지 업로드
        List<ImageUploadRes> imageInfos = s3Service.uploadFiles(images);

        // 5-2. DB 저장 용 이미지 정보 생성
        List<FileEntity> imageEntities = imageInfos.stream()
                .map(image -> FileEntity.builder()
                        .refIndex(savedItem.getProductsIdx())
                        .filePath(image.filePath())
                        .fileType(image.fileType())
                        .storedFileName(image.storedFileName())
                        .originalFileName(image.originalFileName())
                        .build())
                .toList();

        // 5-3. DB에 저장
        List<FileEntity> savedImage = fileRepository.saveAll(imageEntities);



        // 6. 반환 데이터 생성

        // 6-1 category
        // 6-1.1 Category, ip 구분 없이 모든 상위 카테고리를 List로
        List<Long> parentsIdx = ProductMapper.splitAllPath(categoryEntities);
        // 6-1.2 부모 조회
        List<ProductCategoryEntity> parentsEntities = categoryRepository
                .findAllByCategoryIdxInAndDelFalse(parentsIdx);

        // 6-1.3 위 데이터를 정렬 및 구분 후  반환 데이터 생성
        CategoryParentGroup parents = ProductMapper.sortAndDivParents(parentsEntities, category.getPath(), ip.getPath());


        // 6-2. Image
        // S3 조회용 키
        List<String> keys = imageInfos.stream()
                .map(ImageUploadRes::key)
                .toList();
        // S3 조회
        List<String> fileUrls = s3Service.getFileUrls(keys);

        List<ImageInfoRes> imagesInfo = ProductMapper.mapToImageInfos(imageEntities, fileUrls);

        return toProductDetail(
                savedItem,
                savedOpt,
                imagesInfo,
                parents);
    }





    @Override
    @Transactional
    public ProductDetailRes getManagerProductDetail(Long usersIdx, String code){

        // 본인 확인
        String role = authAdapter.getUserRole(usersIdx);
        // SELLER 또는 ADMIN이 아닌경우
        if (!UserRole.isStaff(role)) {
            throw new ForbiddenException("권한이 없습니다.");
        }

        // 1. 상품 조회
        ProductEntity product =  productRepository.findByProductsCodeAndDelFalse(code)
                .orElseThrow(() -> new EntityNotFoundException("존재 하지 않는 상품입니다."));
        
        // 2. 옵션 조회
        List<ProductOptionEntity> options = optionRepository
                .findByProductAndDelFalseOrderBySortOrdersAsc(product);


        // 3. 카테고리/ip 부모 조회
        ProductCategoryEntity category = product.getCategory();
        ProductCategoryEntity ip = product.getIp();

        // 3.1 모든 부모 idx를 리스트로
        List<Long> parentsIdx = ProductMapper.splitAllPath(List.of(category,ip));

        // 3.2 부모 조회
        List<ProductCategoryEntity> parentsEntities = categoryRepository
                .findAllByCategoryIdxInAndDelFalse(parentsIdx);

        // 3.3 위 데이터를 정렬 및 구분 후  반환 데이터 생성
        CategoryParentGroup parents = ProductMapper.sortAndDivParents(parentsEntities, category.getPath(), ip.getPath());



        // 4. Image
        // 4.1 해당 상품에 대한 이미지 조회
        List<FileEntity> images = fileRepository.findAllByRefTableAndRefIndexAndDelFalse("Products",product.getProductsIdx());
        // 4.2 S3 조회용 키
        List<String> keys = images.stream()
                .map( image -> productPrefix + "/" + image.getStoredFileName())
                .toList();
        // 4.3 S3 조회
        List<String> fileUrls = s3Service.getFileUrls(keys);

        // 4.4 사진 이름 + url 반환
        List<ImageInfoRes> imageInfo = ProductMapper.mapToImageInfos(images, fileUrls);

        // mapper 사용
        return toProductDetail(
                product,
                options,
                imageInfo,
                parents
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
    public ProductDetailRes updateProduct(
            Long usersIdx,
            String code,
            UpsertProductReq request,
            UserRole inputRole
            ){

        // 1.  권한 확인
        String role = authAdapter.getUserRole(usersIdx);
        // SELLER 또는 ADMIN이 아닌경우
        if (!UserRole.isStaff(role)) {
            throw new ForbiddenException("권한이 없습니다.");
        }

        // 2.  Entity 가져오기
        ProductEntity product = productRepository.findByProductsCodeAndDelFalse(code)
                .orElseThrow(() -> new EntityNotFoundException("존재 하지 않는 상품입니다."));

        // 2.1 판매자 본인인지 확인(판매자일 경우, 관리자의 경우 스킵)
        if(!usersIdx.equals(product.getSellerIdx()) && inputRole.equals(UserRole.SELLER)){
            throw new ForbiddenException("해당 상품의 판매자 본인이 아닙니다.");
        }

        // 2.2 option 가져오기
        List<ProductOptionEntity> options = optionRepository.findByProductAndDelFalseOrderBySortOrdersAsc(product);

        // 2.2.1 option map 생성
        Map<String, ProductOptionEntity> optionMap = options.stream()
                .collect(Collectors.toMap(ProductOptionEntity::getOptionCode, Function.identity()));

        // 2.2.2 빈 리스트 생성 (추가 옵션,이미지를 담기 위함)
        List<ProductOptionEntity> newOptions = new ArrayList<>();


        // 2.3 변경 될 카테고리/ ip 가져오기
        List<String> codes = List.of(request.categoryCode(), request.ipCode());
        List<ProductCategoryEntity> categoryEntities = categoryRepository.findAllByCategoryCodeInAndDelFalse(codes);

        // 2.3.1 Category, Ip 구분
        ProductCategoryEntity category = ProductMapper.findByType(categoryEntities,CategoryType.CATEGORY).stream()
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("존재 하지 않는 카테고리입니다."));
        ProductCategoryEntity ip = ProductMapper.findByType(categoryEntities,CategoryType.IP).stream()
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("존재 하지 않는 ip입니다."));
        // -----------------------------------------------------------------------

        // 3. 상품 update 진행
        product.update(request.name(),request.description(),request.price(),
                request.salePrice(),request.stock(),request.status(),
                category, ip);

        // ------------------------------------------------------------------------

        // 4. 옵션 update 진행
        // 추가 된 옵션, 삭제 된 옵션, 수정 된 옵션 전부 처리
        for(ProductOptionsReq dto : request.options()){
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


        // ------------------------------------------------------------------------

        // TODO 5. 이미지 수정

        // 어케함??????????????????????????????

        // ------------------------------------------------------------------------

        // 6. 반환 용 데이터 생성
        // 6.1모든 부모 idx를 리스트로
        List<Long> parentsIdx = ProductMapper.splitAllPath(List.of(category,ip));

        // 6.2 부모 조회
        List<ProductCategoryEntity> parentsEntities = categoryRepository
                .findAllByCategoryIdxInAndDelFalse(parentsIdx);

        // 6.3 위 데이터를 정렬 및 구분 후  반환 데이터 생성
        CategoryParentGroup parents = ProductMapper.sortAndDivParents(parentsEntities, category.getPath(), ip.getPath());



        //  Image
        // 6.4 해당 상품에 대한 이미지 조회
        List<FileEntity> images = fileRepository.findAllByRefTableAndRefIndexAndDelFalse("Products",product.getProductsIdx());
        // 6.5 S3 조회용 키
        List<String> keys = images.stream()
                .map( image -> productPrefix + "/" + image.getStoredFileName())
                .toList();
        // 6.6 S3 조회
        List<String> fileUrls = s3Service.getFileUrls(keys);

        // 6.7 사진 이름 + url 반환
        List<ImageInfoRes> imageInfo = ProductMapper.mapToImageInfos(images, fileUrls);

        // mapper 사용
        return toProductDetail(
                product,
                options,
                imageInfo,
                parents
        );
    }


    @Override
    @Transactional
    public void deleteProduct(Long usersIdx, String code, UserRole inputRole){

        // 1. 본인 확인
        String role = authAdapter.getUserRole(usersIdx);
        // SELLER 또는 ADMIN이 아닌경우
        if (!UserRole.isStaff(role)) {
            throw new ForbiddenException("권한이 없습니다.");
        }

        // 2.1 엔티티 가져오기
        ProductEntity product = productRepository.findByProductsCodeAndDelFalse(code)
                .orElseThrow(() -> new EntityNotFoundException("존재 하지 않는 상품입니다."));

        // 2.2 판매자 본인인지 확인(판매자일 경우, 관리자의 경우 스킵)
        if (!product.getSellerIdx().equals(usersIdx) && inputRole.equals(UserRole.SELLER) ){
            throw new ForbiddenException("판매자 본인이 아닙니다.");
        }

        // 2.3  옵션 가져오기
        List<ProductOptionEntity> options = optionRepository.findByProductAndDelFalse(product);

        // 2.4 삭제 시도
        try{
            product.delete();
        } catch (Exception e){
            throw new IllegalArgumentException("상품 삭제 과정 중 문제가 발생했습니다.");
        }


        // 2.5 옵션 삭제
        try{
            options.forEach(ProductOptionEntity::delete);
        } catch (Exception e) {
            throw new IllegalArgumentException("옵션 삭제 과정 중 문제가 발생했습니다.");
        }

        // 3. 이미지 삭제
        // 3.1 이미지 조회
        // 전부 삭제 or 하나 만 남기고 삭제 고민
        // 주문 목록 등에서 썸네일 보여주기 용
        List<FileEntity> images = fileRepository.findAllByRefTableAndRefIndexAndDelFalse("Products",product.getProductsIdx());

        List<String> keys = images.stream()
                .map( image -> productPrefix + "/" + image.getStoredFileName())
                .toList();

        // 3.2 case 1) 전부 삭제
        s3Service.deleteFiles(keys);

        // 3.2 case 2) 썸넬 제외
/*
        List<String> excludeThumbnail = keys.stream()
                .skip(1)
                .toList();

        s3Service.deleteFiles(keys);
        */
    }

    @Override
    @Transactional(readOnly = true)
    public ProductListRes getListsBySeller(Long usersIdx, Pageable pageable, ProductListReq requests){


        String role = authAdapter.getUserRole(usersIdx);
        // SELLER가 아닌경우
        if (!UserRole.isSeller(role)) {
            throw new ForbiddenException("판매자가 아닙니다.");
        }

        // 검색어 존재 시 검색 진행
        Page<ProductDocument> pageResult = (requests.search() == null || requests.search().isBlank())
                ? productEsRepository.findAll(pageable)
                : productEsRepository.findAllBySellerIdxAndProductsNameAndDelFalse(usersIdx,requests.search() ,pageable);
        
        // 2. Document -> Response DTO 변환
        List<ProductRes> items = pageResult.stream()
                .map(doc -> new ProductRes(
                        doc.getProductsCode(),
                        doc.getProductsName(),
                        doc.getPrice(),     // Double -> BigDecimal 변환
                        doc.getSalePrice(), // Double -> BigDecimal 변환
                        doc.getViewCount()
                ))
                .toList();
        return new ProductListRes(
                items

        );
    }
}
