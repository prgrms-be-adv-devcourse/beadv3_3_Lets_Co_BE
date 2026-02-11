package co.kr.product.product.controller;

import co.kr.product.common.service.S3Service;
import co.kr.product.product.model.dto.request.DeductStockReq;
import co.kr.product.product.model.dto.request.ProductIdxsReq;
import co.kr.product.product.model.dto.request.ProductInfoToOrderReq;
import co.kr.product.product.model.dto.request.ProductListReq;
import co.kr.product.product.model.dto.response.*;
import co.kr.product.product.service.ProductSearchService;
import co.kr.product.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;
    private final ProductSearchService productSearchService;

    private final S3Service s3Service;
/*

    // S3 연결 테스트 용
    // consumes을 명시적으로 표기한다고함 생략가능
    @PostMapping(value = "test/connect/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> testS3Connect(
            //@RequestParam("file") MultipartFile file
            // 파일과 json을 같이 보낼때는 이게 표준이라고 함
            @RequestPart("file") MultipartFile file){

        // s3에 업로드
        String key = s3Service.uploadFile(file);
    
        // s3에서 이미지 가져오기 (presigned URL 방식)
        String url = s3Service.getFileUrl(key);


        return ResponseEntity.ok("테스트 성공. URL :  "+url);
    }

*/

    /**
     * 상품 목록 조회 (비회원/회원)
     * @param pageable
     * 상품 목록 검색, ElasticSearch에 연결
     */
    @GetMapping
    public ResponseEntity<ProductListRes> getProducts(
            @PageableDefault(size = 20) Pageable pageable,
            @ModelAttribute ProductListReq request) {

        //return productService.getProducts(pageable);

        return ResponseEntity.ok(
                productSearchService.getProductsList(pageable,request));
    }



    /**
     * 상품 상세 조회 (비회원/회원)
     * @param productsCode
     *  상품 코드를 통한 상품 정보 조회
     */
    @GetMapping("/{productsCode}")
    public ResponseEntity<ProductDetailRes> getProductDetail(
            @PathVariable String productsCode) {

        return ResponseEntity.ok(
                productService.getProductDetail(productsCode));
    }


    /**
     * 상품 재고 검사
     * @param productsCode
     * 상품 재고 여부 확인 후 boolean 반환
     */
    @GetMapping("/{productsCode}/checkStock")
    public ProductCheckStockRes getCheckStock(
            @PathVariable String productsCode){

        return productService.getCheckStock(productsCode);
    }



}



