package co.kr.product.review.service;

import co.kr.product.product.client.OrderServiceClient;
import co.kr.product.product.model.entity.ProductEntity;
import co.kr.product.product.repository.ProductRepository;
import co.kr.product.review.model.entity.Review;
import co.kr.product.review.model.dto.request.ReviewUpsertRequest;
import co.kr.product.review.model.dto.response.ReviewListResponse;
import co.kr.product.review.model.dto.response.ReviewResponse;
import co.kr.product.review.repository.ReviewRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final OrderServiceClient orderServiceClient;

    // 리뷰 목록 조회(비회원/회원 모두)
    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviews(Long productsIdx) {

        return reviewRepository
                .findByProductsIdxAndDelFalseOrderByCreatedAtDesc(productsIdx)
                .stream()
                .map(r -> new ReviewResponse(
                        r.getEvaluation(),
                        r.getContent()
                ))
                .toList();


    }

    // 리뷰 작성(회원 + 구매자만 + 상품당 1개)
    @Transactional
    public ReviewResponse createReview(String productsCode, ReviewUpsertRequest req,Long userIdx ) {

        Long orderItemIdx = orderServiceClient.getOrderItemIdxByCode(productsCode);

        if (orderItemIdx == null) {
            throw new IllegalArgumentException("존재하지 않는 주문 item 코드 입니다.");
        }
        if (req.evaluation() == null || req.evaluation() < 1 || req.evaluation() > 5) {
            throw new IllegalArgumentException("별점은  1 ~ 5 사이의 값이여야 합니다.");
        }
        if (req.content() == null || req.content().trim().isEmpty()) {
            throw new IllegalArgumentException("내용은 비어있으면 안됩니다.");
        }

        ProductEntity product = productRepository.findByProductsCodeAndDelFalse(productsCode)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 상품입니다: " + productsCode));

        // 정책: 같은 유저는 같은 상품에 리뷰 1개만 가능
        if (reviewRepository.existsByProductsIdxAndUsersIdxAndDelFalse(product.getProductsIdx(), userIdx)) {
            throw new IllegalStateException("이미 해당 상품에 리뷰를 작성하셨습니다.");
        }



        // (추가 안전장치) 주문아이템당 리뷰 1개
        if (reviewRepository.existsByOrdersItemIdxAndDelFalse(orderItemIdx)) {
            throw new IllegalStateException("이미 해당 주문상품에 리뷰가 존재합니다.");
        }

        Review review = new Review(
                product.getProductsIdx(),
                userIdx,
                orderItemIdx,
                req.evaluation(),
                req.content()
        );

        try {
            Review saved = reviewRepository.save(review);

            return new ReviewResponse(

                    saved.getEvaluation(),
                    saved.getContent()
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
    public ReviewResponse updateReview(String productsCode, ReviewUpsertRequest req, Long usersIdx) {

        if (req.evaluation() == null || req.evaluation() < 1 || req.evaluation() > 5) {
            throw new IllegalArgumentException("evaluation must be between 1 and 5.");
        }
        if (req.content() == null || req.content().trim().isEmpty()) {
            throw new IllegalArgumentException("content is required.");
        }

        ProductEntity product = productRepository.findByProductsCodeAndDelFalse(productsCode)
                .orElseThrow(() -> new EntityNotFoundException("상품을 찾을 수 없습니다."));

        Review review = reviewRepository.findByProductsIdxAndUsersIdxAndDelFalse(product.getProductsIdx(), usersIdx)
                .orElseThrow(() -> new EntityNotFoundException("리뷰가 없거나 수정 권한이 없습니다."));

        review.update(req.evaluation(), req.content());

        return new ReviewResponse(

                review.getEvaluation(),
                review.getContent()
        );
    }

    // 리뷰 삭제(회원 + 작성자 본인만) - soft delete
    @Transactional
    public void deleteReview(String productsCode, Long usersIdx) {

        ProductEntity product = productRepository.findByProductsCodeAndDelFalse(productsCode)
                .orElseThrow(() -> new EntityNotFoundException("상품을 찾을 수 없습니다."));

        Review review = reviewRepository.findByProductsIdxAndUsersIdxAndDelFalse(product.getProductsIdx(), usersIdx)
                .orElseThrow(() -> new EntityNotFoundException("리뷰가 없거나 수정 권한이 없습니다."));

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

