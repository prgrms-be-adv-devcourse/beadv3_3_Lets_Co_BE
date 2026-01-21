package co.kr.product.review.service;

import co.kr.product.product.client.AuthServiceClient;
import co.kr.product.review.entity.Review;
import co.kr.product.review.dto.request.ReviewUpsertRequest;
import co.kr.product.review.dto.response.ReviewListResponse;
import co.kr.product.review.dto.response.ReviewResponse;
import co.kr.product.review.repository.ReviewRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;

    // 리뷰 목록 조회(비회원/회원 모두)
    @Transactional(readOnly = true)
    public ReviewListResponse getReviews(Long productsIdx) {

        List<ReviewResponse> items = reviewRepository
                .findByProductsIdxAndDelFalseOrderByCreatedAtDesc(productsIdx)
                .stream()
                .map(r -> new ReviewResponse(
                        r.getReviewIdx(),
                        r.getProductsIdx(),
                        r.getUsersIdx(),
                        r.getEvaluation(),
                        r.getContent(),
                        r.getCreatedAt()
                ))
                .toList();

        return new ReviewListResponse("SUCCESS", items);
    }

    // 리뷰 작성(회원 + 구매자만 + 상품당 1개)
    @Transactional
    public ReviewResponse createReview(Long productsIdx, ReviewUpsertRequest req,Long userIdx ) {

        if (req.getOrderItemIdx() == null) {
            throw new IllegalArgumentException("orderItemIdx is required.");
        }
        if (req.getEvaluation() == null || req.getEvaluation() < 1 || req.getEvaluation() > 5) {
            throw new IllegalArgumentException("evaluation must be between 1 and 5.");
        }
        if (req.getContent() == null || req.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("content is required.");
        }

        // 정책: 같은 유저는 같은 상품에 리뷰 1개만 가능
        if (reviewRepository.existsByProductsIdxAndUsersIdxAndDelFalse(productsIdx, userIdx)) {
            throw new IllegalStateException("이미 해당 상품에 리뷰를 작성하셨습니다.");
        }

        // (추가 안전장치) 주문아이템당 리뷰 1개
        if (reviewRepository.existsByOrdersItemIdxAndDelFalse(req.getOrderItemIdx())) {
            throw new IllegalStateException("이미 해당 주문상품에 리뷰가 존재합니다.");
        }

        Review review = new Review(
                productsIdx,
                userIdx,
                req.getOrderItemIdx(),
                req.getEvaluation(),
                req.getContent()
        );

        try {
            Review saved = reviewRepository.save(review);

            return new ReviewResponse(
                    saved.getReviewIdx(),
                    saved.getProductsIdx(),
                    saved.getUsersIdx(),
                    saved.getEvaluation(),
                    saved.getContent(),
                    saved.getCreatedAt()
            );

        } catch (DataIntegrityViolationException e) {
            // 구매자 검증 실패(트리거 SIGNAL SQLSTATE '45000') -> 메시지 변환
            if (hasSqlState(e, "45000")) {
                throw new IllegalStateException("구매하시지 않았습니다.");
            }
            throw e;
        }
    }

    // 리뷰 수정(회원 + 작성자 본인만)
    @Transactional
    public ReviewResponse updateReview(Long reviewIdx, ReviewUpsertRequest req, Long usersIdx) {

        if (req.getEvaluation() == null || req.getEvaluation() < 1 || req.getEvaluation() > 5) {
            throw new IllegalArgumentException("evaluation must be between 1 and 5.");
        }
        if (req.getContent() == null || req.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("content is required.");
        }

        Review review = reviewRepository.findByReviewIdxAndUsersIdxAndDelFalse(reviewIdx, usersIdx)
                .orElseThrow(() -> new EntityNotFoundException("리뷰가 없거나 수정 권한이 없습니다."));

        review.update(req.getEvaluation(), req.getContent());

        return new ReviewResponse(
                review.getReviewIdx(),
                review.getProductsIdx(),
                review.getUsersIdx(),
                review.getEvaluation(),
                review.getContent(),
                review.getCreatedAt()
        );
    }

    // 리뷰 삭제(회원 + 작성자 본인만) - soft delete
    @Transactional
    public void deleteReview(Long reviewIdx, Long usersIdx) {

        Review review = reviewRepository.findByReviewIdxAndUsersIdxAndDelFalse(reviewIdx, usersIdx)
                .orElseThrow(() -> new EntityNotFoundException("리뷰가 없거나 삭제 권한이 없습니다."));

        review.softDelete();
    }

    private boolean hasSqlState(Throwable t, String targetSqlState) {
        Throwable cur = t;
        while (cur != null) {
            if (cur instanceof SQLException sqlEx) {
                if (targetSqlState.equals(sqlEx.getSQLState())) {
                    return true;
                }
            }
            cur = cur.getCause();
        }
        return false;
    }
}

