package co.kr.product.review.controller;

import co.kr.product.review.dto.request.ReviewUpsertRequest;
import co.kr.product.review.dto.response.CommonResponse;
import co.kr.product.review.dto.response.ReviewListResponse;
import co.kr.product.review.dto.response.ReviewResponse;
import co.kr.product.review.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/products")
public class ReviewController {

    private final ReviewService reviewService;

    // 리뷰 목록 조회 (비회원/회원)
    @GetMapping("/{productsIdx}/reviews")
    public ReviewListResponse getReviews(@PathVariable Long productsIdx) {
        return reviewService.getReviews(productsIdx);
    }

    // 리뷰 작성 (회원)
    @PostMapping("/{productsIdx}/reviews")
    public ReviewResponse createReview(@PathVariable Long productsIdx,
                                       @RequestBody @Valid ReviewUpsertRequest req,
                                       @RequestHeader("X-USERS-IDX") Long usersIdx) {
        return reviewService.createReview(productsIdx, req, usersIdx);
    }

    // 리뷰 수정 (회원 + 본인)
    @PatchMapping("/reviews/{reviewIdx}")
    public ReviewResponse updateReview(@PathVariable Long reviewIdx,
                                       @RequestBody @Valid ReviewUpsertRequest req,
                                       @RequestHeader("X-USERS-IDX") Long usersIdx) {
        return reviewService.updateReview(reviewIdx, req, usersIdx);
    }

    // 리뷰 삭제 (회원 + 본인)
    @DeleteMapping("/reviews/{reviewIdx}")
    public CommonResponse deleteReview(@PathVariable Long reviewIdx,
                                       @RequestHeader("X-USERS-IDX") Long usersIdx) {
        reviewService.deleteReview(reviewIdx, usersIdx);
        return new CommonResponse("SUCCESS");
    }


    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> handleIllegalState(IllegalStateException e) {
        if ("구매좀 하세요".equals(e.getMessage())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
        return ResponseEntity.badRequest().body(e.getMessage());
    }

}


