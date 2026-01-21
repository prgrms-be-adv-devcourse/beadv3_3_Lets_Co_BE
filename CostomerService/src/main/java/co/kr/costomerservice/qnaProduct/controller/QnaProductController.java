package co.kr.costomerservice.qnaProduct.controller;

import co.kr.costomerservice.common.dto.response.ResultResponse;
import co.kr.costomerservice.qnaProduct.model.request.QnaProductListRequest;
import co.kr.costomerservice.qnaProduct.model.request.QnaProductUpsertRequest;
import co.kr.costomerservice.qnaProduct.model.response.QnaAndProductInfoListResponse;
import co.kr.costomerservice.qnaProduct.model.response.QnaProductDetailResponse;
import co.kr.costomerservice.qnaProduct.model.response.QnaProductListResponse;
import co.kr.costomerservice.qnaProduct.service.QnaProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/qna/products")
public class QnaProductController {

    private final QnaProductService qnaProductService;

    /**
     * 공개되어 있는 qna 리스트 조회
     * @param pageable
     * @param request
     * @return
     */
    @GetMapping("/{productsCode}")
    public ResponseEntity<QnaProductListResponse> getProductQnaList(
            @PageableDefault Pageable pageable,
            //@PathVariable("productsCode") String productsCode
            @RequestBody QnaProductListRequest request
            ){
        // 상품 상세 페이지에서 상품 idx를 들고있기에 RequestBody로 idx를 받아오는 방법을 택함
        // 이는 후에 문제가 있다고 판단되면 product 서비스에 요청을 보내서 idx 받아오는 방법으로 고쳐볼것

        return ResponseEntity.ok(qnaProductService.getProductQnaList(request.productsIdx(), pageable));
    }


    /**
     * 공개되어 있는 qna 상세 조회
     * @param qnaCode
     * @return
     */
    @GetMapping("/{productsCode}/{qnaCode}")
    public ResponseEntity<QnaProductDetailResponse> getProductQnaDetail(
        //@PathVariable("productsCode") String productsCode,
        @PathVariable("qnaCode") String qnaCode
    ){
        // 임시
        Long userIdx = 2L;
        return ResponseEntity.ok(qnaProductService.getProductQnaDetail(qnaCode,userIdx ));
    }


    @PostMapping("/{productsCode}")
    public ResponseEntity<QnaProductDetailResponse> addProductQna(
            @RequestHeader("X-USERS-IDX") Long usersIdx,

            //@PathVariable("productsCode") String productsCode
            @RequestBody QnaProductUpsertRequest request
    ){
        return  ResponseEntity.ok(qnaProductService.addProductQna(request,usersIdx));
    }

    @PutMapping("/{productsCode}/{qnaCode}")
    public ResponseEntity<QnaProductDetailResponse> updateQna(
            @RequestHeader("X-USERS-IDX") Long usersIdx,
            //@PathVariable("productsCode") String productsCode
            @PathVariable("qnaCode") String qnaCode,
            @RequestBody QnaProductUpsertRequest request
    ){

        return  ResponseEntity.ok(qnaProductService.updateQna(qnaCode ,request,usersIdx ));
    }

    @DeleteMapping("/{productsCode}/{qnaCode}")
    public ResponseEntity<ResultResponse> deleteQna(
            @RequestHeader("X-USERS-IDX") Long usersIdx,
            @PathVariable("qnaCode") String qnaCode
    ){
        return  ResponseEntity.ok(qnaProductService.deleteQna(qnaCode,usersIdx ));
    }

    // 본인 문의 내역 조회
    @GetMapping("/me")
    public ResponseEntity<QnaAndProductInfoListResponse> getMyProductQnaList(
            @RequestHeader("X-USERS-IDX") Long usersIdx,
            @PageableDefault Pageable pageable
    ){
        return ResponseEntity.ok(qnaProductService.getMyProductQnaList(usersIdx, pageable ));
    }
}
