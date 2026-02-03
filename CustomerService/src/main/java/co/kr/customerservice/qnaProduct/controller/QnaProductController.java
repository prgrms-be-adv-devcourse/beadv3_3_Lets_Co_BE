package co.kr.customerservice.qnaProduct.controller;

import co.kr.customerservice.common.model.dto.response.ResultResponse;
import co.kr.customerservice.qnaProduct.model.request.QnaProductUpsertRequest;
import co.kr.customerservice.qnaProduct.model.response.QnaAndProductInfoListResponse;
import co.kr.customerservice.qnaProduct.model.response.QnaProductDetailResponse;
import co.kr.customerservice.qnaProduct.model.response.QnaProductListResponse;
import co.kr.customerservice.qnaProduct.service.QnaProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/qna")
public class QnaProductController {

    private final QnaProductService qnaProductService;

    /**
     * 공개되어 있는 qna 리스트 조회
     * @param pageable
     * @param
     * @return
     */
    @GetMapping("/products/{productsCode}")
    public ResponseEntity<QnaProductListResponse> getProductQnaList(
            @PageableDefault Pageable pageable,
            @PathVariable("productsCode") String productsCode
            ){

        return ResponseEntity.ok(
                qnaProductService.getProductQnaList(productsCode, pageable));
    }


    /**
     * 공개되어 있는 qna 상세 조회
     * @param qnaCode
     * @return
     */
    @GetMapping("/{qnaCode}")
    public ResponseEntity<QnaProductDetailResponse> getProductQnaDetail(
            @RequestHeader("X-USERS-IDX") Long usersIdx,
            //@PathVariable("productsCode") String productsCode,
            @PathVariable("qnaCode") String qnaCode
    ){

        return ResponseEntity.ok(
                qnaProductService.getProductQnaDetail(qnaCode,usersIdx ));
    }


    // 상품 문의 추가
    @PostMapping("/products/{productsCode}")
    public ResponseEntity<QnaProductDetailResponse> addProductQna(
            @RequestHeader("X-USERS-IDX") Long usersIdx,

            @PathVariable("productsCode") String productsCode,
            @RequestBody QnaProductUpsertRequest request
    ){

        return  ResponseEntity.ok(
                qnaProductService.addProductQna(productsCode,request,usersIdx));
    }

    // 상품문의 수정
    @PutMapping("/{qnaCode}")
    public ResponseEntity<QnaProductDetailResponse> updateQna(
            @RequestHeader("X-USERS-IDX") Long usersIdx,
            //@PathVariable("productsCode") String productsCode
            @PathVariable("qnaCode") String qnaCode,
            @RequestBody QnaProductUpsertRequest request
    ){

        return  ResponseEntity.ok(
                qnaProductService.updateQna(qnaCode ,request,usersIdx ));
    }

    // 상품 문의 제거
    @DeleteMapping("/{qnaCode}")
    public ResponseEntity<ResultResponse> deleteQna(
            @RequestHeader("X-USERS-IDX") Long usersIdx,
            @PathVariable("qnaCode") String qnaCode
    ){
        return  ResponseEntity.ok(
                qnaProductService.deleteQna(qnaCode,usersIdx ));
    }

    // 본인 문의 내역 조회
    @GetMapping("/me")
    public ResponseEntity<QnaAndProductInfoListResponse> getMyProductQnaList(
            @RequestHeader("X-USERS-IDX") Long usersIdx,
            @PageableDefault Pageable pageable
    ){
        return ResponseEntity.ok(
                qnaProductService.getMyProductQnaList(usersIdx, pageable ));
    }
}
