package co.kr.product.seller.mapper;

import co.kr.product.seller.entity.OptionEntity;
import co.kr.product.seller.entity.ProductEntity;
import co.kr.product.seller.model.dto.ProductOptionsResponse;
import co.kr.product.seller.model.dto.ProductDetailResponse;
import co.kr.product.seller.model.dto.ProductResponse;

import java.util.List;

public class ProductMapper {

    public static ProductDetailResponse toProductDetail(String resultCode, ProductEntity product, List<OptionEntity> options) {


        return new ProductDetailResponse(
                resultCode,
                product.getName(),
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

    public static ProductOptionsResponse toOptMapper(OptionEntity options){
        return new ProductOptionsResponse(
                options.getCode(),
                options.getName(),
                options.getSortOrder(),
                options.getPrice(),
                options.getSalePrice(),
                options.getStock(),
                options.getStatus()

        );
    }

    public static ProductResponse toProductEach(){
        
        return null;
    }


}
