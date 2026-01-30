package co.kr.product.product.mapper;

import co.kr.product.product.dto.response.ProductImageRes;
import co.kr.product.product.dto.response.ProductOptionsRes;
import co.kr.product.product.dto.response.ProductDetailRes;
import co.kr.product.product.entity.ProductEntity;
import co.kr.product.product.entity.ProductImageEntity;
import co.kr.product.product.entity.ProductOptionEntity;

import java.util.List;

public class ProductMapper {

    public static ProductDetailRes toProductDetail(
             String resultCode, ProductEntity product,
             List<ProductOptionEntity> options,
             List<ProductImageEntity> images) {


        return new ProductDetailRes(
                resultCode,
                product.getProductsIdx(),
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
                images.stream()
                        .map(ProductMapper::toImageMapper)
                        .toList()

        );


    }

    public static ProductOptionsRes toOptMapper(ProductOptionEntity options){
        return new ProductOptionsRes(
                options.getOptionGroupIdx(),
                options.getOptionCode(),
                options.getOptionName(),
                options.getSortOrders(),
                options.getOptionPrice(),
                options.getOptionSalePrice(),
                options.getStock(),
                options.getStatus()

        );
    }

    public static ProductImageRes toImageMapper(ProductImageEntity image) {
        return new ProductImageRes(
                image.getImageIdx(),
                image.getUrl(),
                image.getSortOrders(),
                image.getIsThumbnail()
        );
    }



}
