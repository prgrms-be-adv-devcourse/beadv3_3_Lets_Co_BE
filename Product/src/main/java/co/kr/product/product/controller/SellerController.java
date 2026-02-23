package co.kr.product.product.controller;

import co.kr.product.common.vo.UserRole;
import co.kr.product.product.model.dto.request.ProductListReq;
import co.kr.product.product.model.dto.request.UpsertProductReq;
import co.kr.product.product.model.dto.response.ProductDetailRes;
import co.kr.product.product.model.dto.response.ProductListRes;
import co.kr.product.product.model.dto.response.ResultRes;
import co.kr.product.product.service.ProductManagerService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/seller")
public class SellerController {

    private final ProductManagerService productManagerService;

    /**
     * // @param accountCode
     * @param pageable
     * @param requests
     * @return 판매자가 올린 상품 리스트 반환
     */
    @GetMapping("/products")
    public ResponseEntity<ProductListRes> getLists(
            @RequestHeader("X-USERS-IDX") Long usersIdx,
            @PageableDefault Pageable pageable,
            @ModelAttribute @Valid ProductListReq requests
            ){

        return ResponseEntity.ok(
                productManagerService.getListsBySeller(usersIdx, pageable, requests));
    }


/*
    */
/**
     * 상품 등록(판매자)
     * @param usersIdx
     * @param request
     * @return 등록 된 상품의 상세 정보
     *//*

    @PostMapping("/products")
    public  ResponseEntity<ProductDetailRes> addProduct(
            @RequestHeader("X-USERS-IDX") Long usersIdx,
            @RequestBody @Valid UpsertProductReq request
            ){


        return ResponseEntity.ok(
                productManagerService.addProduct(usersIdx,request));
    }
*/
    /**
     * 상품 등록(판매자)
     * @param usersIdx
     * @param request
     * @return 등록 된 상품의 상세 정보
     */
    @PostMapping(value = "/products" ,consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public  ResponseEntity<ProductDetailRes> addProduct(
            @RequestHeader("X-USERS-IDX") Long usersIdx,

            // swagger 인식용
            @Parameter(content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
            @RequestPart("request") @Valid UpsertProductReq request,

            @RequestPart(value = "images") @NotEmpty(message = "최소 1개의 이미지를 등록해야 합니다.") List<MultipartFile> images
            ){


        // 허용할 이미지  타입 목록
        List<String> allowedMimeTypes = Arrays.asList("image/jpeg", "image/png", "image/gif", "image/webp");

        for (MultipartFile file : images) {
            // 1. 파일이 비어있는지 확인 (이름만 있고 내용이 없는 깡통 파일)
            if (file.isEmpty()) {
                throw new IllegalArgumentException("비어있는 파일이 포함되어 있습니다.");
            }

            // 2. MIME 타입 검사 (단순 확장자 검사보다 안전함)
            String contentType = file.getContentType();
            if (contentType == null || !allowedMimeTypes.contains(contentType)) {
                throw new IllegalArgumentException("지원하지 않는 이미지 형식입니다. (JPEG, PNG, GIF, WEBP만 허용)");
            }

            // 3.  파일 이름 길이 제한
            String fileName = file.getOriginalFilename();
            if (fileName != null && fileName.length() > 100) {
                throw new IllegalArgumentException("파일 이름이 너무 깁니다.");
            }
        }

        return ResponseEntity.ok(
                productManagerService.addProduct(usersIdx,request, images));
    }


    /**
     * 상품 상세 정보(판매자)
     * @param usersIdx
     * @param productCode
     * @return 상품 상세 정보
     */
    @GetMapping("/products/{code}")
    public ResponseEntity<ProductDetailRes> getProductDetail(
            @RequestHeader("X-USERS-IDX") Long usersIdx,
            @PathVariable("code") String productCode
    ){


        return ResponseEntity.ok(
                productManagerService.getManagerProductDetail(usersIdx,productCode));
    }

    /**
     * 상품 정보 수정(판매자)
     * @param usersIdx
     * @param productCode
     * @param request
     * @return 상품 상세 정보
     */
    @PutMapping("/products/{code}")
    public ResponseEntity<ProductDetailRes> updateProduct(
            @RequestHeader("X-USERS-IDX") Long usersIdx,
            @PathVariable("code") String productCode,
            @RequestBody @Valid UpsertProductReq request
    ){

        return ResponseEntity.ok(
                productManagerService.updateProduct(usersIdx,productCode, request, UserRole.SELLER ));
    }

    /**
     * 상품 제거(판매자)
     * @param usersIdx
     * @param productCode
     * @return resultCode
     */
    @DeleteMapping("/products/{code}")
    public ResponseEntity<ResultRes> deleteProduct(
            @RequestHeader("X-USERS-IDX") Long usersIdx,
            @PathVariable("code") String productCode
    ){
        
        productManagerService.deleteProduct(usersIdx,productCode, UserRole.SELLER);
        return ResponseEntity.ok(new ResultRes("ok"));
    }
}
