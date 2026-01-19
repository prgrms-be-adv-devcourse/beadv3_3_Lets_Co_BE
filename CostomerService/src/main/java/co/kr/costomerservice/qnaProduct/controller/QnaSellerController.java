package co.kr.costomerservice.qnaProduct.controller;


import co.kr.costomerservice.qnaProduct.model.request.QnaAnswerUpsertRequest;
import co.kr.costomerservice.qnaProduct.model.response.QnaAndProductInfoListResponse;
import co.kr.costomerservice.qnaProduct.model.response.QnaProductDetailResponse;
import co.kr.costomerservice.qnaProduct.model.response.QnaProductForSellerListResponse;
import co.kr.costomerservice.qnaProduct.model.response.QnaProductListResponse;
import co.kr.costomerservice.qnaProduct.service.QnaSellerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/seller/product_qna")
public class QnaSellerController {
    private final QnaSellerService qnaSellerService;

    // 본인상품에 온 모든 문의 조회(상품이 달라도)
    @GetMapping
    public ResponseEntity<QnaAndProductInfoListResponse> getMyQnaList(
            @RequestHeader("X-USERS-IDX") Long usersIdx,
            @PageableDefault Pageable pageable
            ){
        return ResponseEntity.ok(qnaSellerService.getMyQnaList(usersIdx, pageable));

    }

    @PostMapping("/{qnaCode}")
    public ResponseEntity<QnaProductDetailResponse> addAnswer(
            @RequestHeader("X-USERS-IDX") Long usersIdx,
            @PathVariable("qnaCode") String qnaCode,
            @RequestBody QnaAnswerUpsertRequest request
            ){
        return ResponseEntity.ok(qnaSellerService.addAnswer(qnaCode,usersIdx, request));
    }

}
