package co.kr.product.product.mapper;

import co.kr.product.product.model.dto.response.ProductImageRes;
import co.kr.product.product.model.dto.response.ProductOptionsRes;
import co.kr.product.product.model.dto.response.ProductDetailRes;
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


}
