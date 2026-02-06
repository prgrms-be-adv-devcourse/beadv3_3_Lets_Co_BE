package co.kr.product.product.mapper;

import co.kr.product.product.model.dto.response.*;
import co.kr.product.product.model.entity.ProductCategoryEntity;
import co.kr.product.product.model.entity.ProductEntity;
import co.kr.product.product.model.entity.ProductOptionEntity;

import java.util.List;

public class ProductMapper {

    public static ProductDetailRes toProductDetail(
             ProductEntity product,
             List<ProductOptionEntity> options) {


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
                        .toList()

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

}
