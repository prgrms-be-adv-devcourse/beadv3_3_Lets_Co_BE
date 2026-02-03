package co.kr.customerservice.qnaProduct.controller;


import co.kr.customerservice.qnaProduct.model.request.QnaAnswerUpsertReq;
import co.kr.customerservice.qnaProduct.model.response.QnaAndProductInfoListRes;
import co.kr.customerservice.qnaProduct.model.response.QnaProductDetailRes;
import co.kr.customerservice.qnaProduct.service.QnaSellerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/seller/qna")
public class QnaSellerController {
    private final QnaSellerService qnaSellerService;

    // 본인상품에 온 모든 문의 조회(상품이 달라도)
    @GetMapping
    public ResponseEntity<QnaAndProductInfoListRes> getMyQnaList(
            @RequestHeader("X-USERS-IDX") Long usersIdx,
            @PageableDefault Pageable pageable
            ){

        return ResponseEntity.ok(
                qnaSellerService.getMyQnaList(usersIdx, pageable));

    }

    // 본인 상품에 온 문의에 대한 답변
    @PostMapping("/{qnaCode}")
    public ResponseEntity<QnaProductDetailRes> addAnswer(
            @RequestHeader("X-USERS-IDX") Long usersIdx,
            @PathVariable("qnaCode") String qnaCode,
            @RequestBody QnaAnswerUpsertReq request
            ){

        return ResponseEntity.ok(
                qnaSellerService.addAnswer(qnaCode,usersIdx, request));
    }

}
