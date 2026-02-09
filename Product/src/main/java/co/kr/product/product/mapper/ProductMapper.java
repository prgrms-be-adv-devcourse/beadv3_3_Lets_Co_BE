package co.kr.product.product.mapper;

import co.kr.product.product.model.dto.request.CategoryParentGroup;
import co.kr.product.product.model.dto.response.*;
import co.kr.product.product.model.entity.FileEntity;
import co.kr.product.product.model.entity.ProductCategoryEntity;
import co.kr.product.product.model.entity.ProductEntity;
import co.kr.product.product.model.entity.ProductOptionEntity;
import co.kr.product.product.model.vo.CategoryType;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ProductMapper {

    public static ProductDetailRes toProductDetail(
             ProductEntity product,
             List<ProductOptionEntity> options,
             List<ImageInfoRes> images,
             CategoryParentGroup parents) {


        return new ProductDetailRes(
                product.getProductsCode(),
                product.getProductsName(),
                product.getDescription(),
                product.getPrice(),
                product.getSalePrice(),
                product.getViewCount(),
                product.getStock(),
                product.getStatus(),
                options.stream()
                        .map(ProductMapper::toOptMapper)
                        .toList(),
                parents.categoryParents(),
                parents.ipParents(),
                images
                );


    }

    public static ProductOptionsRes toOptMapper(ProductOptionEntity options){
        return new ProductOptionsRes(
                options.getOptionCode(),
                options.getOptionName(),
                options.getSortOrders(),
                options.getOptionPrice(),
                options.getOptionSalePrice(),
                options.getStock(),
                options.getStatus()

        );
    }



    public static CategoryInfoRes toCategoryInfo(ProductCategoryEntity category){
        return new CategoryInfoRes(
                category.getCategoryCode(),
                category.getCategoryName()
        );
    }


    public static CategoryFamilyRes toCategoryFamilyMapper(List<ProductCategoryEntity> parents, List<ProductCategoryEntity> childs){
        List<CategoryInfoRes> parentsRes =  parents.stream()
                .map(ProductMapper::toCategoryInfo)
                .toList();

        List<CategoryInfoRes> childsRes =  childs.stream()
                .map(ProductMapper::toCategoryInfo)
                .toList();

        return new CategoryFamilyRes(parentsRes,childsRes);
    }

    /**
     * path를 split 하여 상위 카테고리의 idx를 List 형태로 반환.
     * 순서 정렬 되어있음
     * @param path
     * @return List<Long> categoryIdxs
     */
    public static List<Long> splitPath(String path){

        return Arrays.stream(path.split("/"))
                .filter(str -> !str.isBlank())  // 혹시 모를 공백 등 제거
                .map(Long::parseLong)
                .toList();
    }

    /**
     * 정렬되어 있는 sortedParentsIdx 기반, parentsEntities를 정렬 및 Map<code,name> 형태가 되도록 출력
     * @param sortedParentsIdx
     * @param parentsEntities
     * @return
     */
    public static List<CategoryInfoRes> sortCategory(List<Long> sortedParentsIdx, List<ProductCategoryEntity> parentsEntities){

        // 빠른 검색을 위해 Entity를 Map 형태로 변환
        Map<Long, ProductCategoryEntity> parentsMap = parentsEntities.stream()
                .collect(Collectors.toMap(
                                ProductCategoryEntity::getCategoryIdx,
                                Function.identity()
                        )
                );


        return sortedParentsIdx.stream()
                // .map(idx -> parentsMap.get(idx))
                .map(parentsMap::get)
                .filter(Objects::nonNull)
                .map(entity ->
                        new CategoryInfoRes(entity.getCategoryCode(),entity.getCategoryName()))
                .toList();
    }

    /**
     * Category, Ip 구분 없이 일단 모든 부모를 찾기 위해 둘의 path를 순서 없이 풀어 해치기.
     * @param entities
     * @return
     */
    public static List<Long> splitAllPath(List<ProductCategoryEntity> entities ){
        return entities.stream()
                .map(entity -> entity.getPath())
                .map(path -> splitPath(path))
                .flatMap(idxs -> idxs.stream())
                .toList();

    }

    public static List<ProductCategoryEntity> findByType(List<ProductCategoryEntity> entities, CategoryType type){

        return entities.stream()
                .filter(entity -> entity.getType() == type)
                .toList();

    }


    public static CategoryParentGroup sortAndDivParents(
            List<ProductCategoryEntity> parents,
            String categoryPath,
            String ipPath){

        //  데이터 구분
        List<ProductCategoryEntity> parentCategories = findByType(parents, CategoryType.CATEGORY);
        List<ProductCategoryEntity> parentIps = findByType(parents, CategoryType.IP);

        List<Long> sortedCategoryIdxs = splitPath(categoryPath);
        List<Long> sortedIpIdxs = splitPath(ipPath);

        //  정렬 및 반환 데이터 생성
        // 이미 정렬되어 있는 idx 기반으로 엔티티 정렬 후 CategoryInfoRes 반환
        List<CategoryInfoRes> categoryRes = ProductMapper.sortCategory(sortedCategoryIdxs, parentCategories);
        List<CategoryInfoRes> ipRes = ProductMapper.sortCategory(sortedIpIdxs, parentIps);

        return new CategoryParentGroup(categoryRes, ipRes);

    }

    public static List<ImageInfoRes> mapToImageInfos(
            List<FileEntity> imageEntities,
            List<String> fileUrls
    ){
        List<ImageInfoRes> imagesInfo = new ArrayList<>();
        for(int i = 0 ; i < imageEntities.size() ; i++){
            imagesInfo.add(
                    new ImageInfoRes(
                            imageEntities.get(i).getStoredFileName(),
                            fileUrls.get(i)
                    )
            );
        }
        return imagesInfo;
    }
}
