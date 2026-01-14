package co.kr.product.product.mapper;

import co.kr.product.product.dto.response.ProductImageResponse;
import co.kr.product.product.dto.response.ProductOptionsResponse;
import co.kr.product.product.dto.response.ProductDetailResponse;
import co.kr.product.product.dto.response.ProductResponse;
import co.kr.product.product.entity.ProductEntity;
import co.kr.product.product.entity.ProductImageEntity;
import co.kr.product.product.entity.ProductOptionEntity;

import java.util.List;

public class ProductMapper {

    public static ProductDetailResponse toProductDetail(
             String resultCode, ProductEntity product,
             List<ProductOptionEntity> options,
             List<ProductImageEntity> images) {


        return new ProductDetailResponse(
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

    public static ProductOptionsResponse toOptMapper(ProductOptionEntity options){
        return new ProductOptionsResponse(
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

    public static ProductImageResponse toImageMapper(ProductImageEntity image) {
        return new ProductImageResponse(
                image.getImageIdx(),
                image.getUrl(),
                image.getSortOrders(),
                image.getIsThumbnail()
        );
    }



}
