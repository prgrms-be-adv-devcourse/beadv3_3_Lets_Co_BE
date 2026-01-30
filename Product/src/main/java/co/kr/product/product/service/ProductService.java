package co.kr.product.product.service;

import co.kr.product.product.dto.request.DeductStockReq;
import co.kr.product.product.dto.request.ProductIdxsReq;
import co.kr.product.product.dto.request.ProductInfoToOrderReq;
import co.kr.product.product.dto.response.*;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface ProductService {
    ProductListRes getProducts(Pageable pageable);


    ProductDetailRes getProductDetail(String productsCode);


    ProductCheckStockRes getCheckStock(String productsCode);

    void deductStock(DeductStockReq deductStockReq);

    void deductStocks(List<DeductStockReq> deductStockReq);

    ProductInfoToOrderRes getProductInfo(Long productsIdx, Long optionIdx);

    List<ProductInfoToOrderRes> getProductInfoList(List<ProductInfoToOrderReq> requests);

    Map<Long, Long> getSellersByProductIds(List<Long> productIds) ;

    List<ProductInfoRes> getProductInfoForBoard(ProductIdxsReq request);

    ProductSellerRes getSellerIdx(Long productsIdx);
}
