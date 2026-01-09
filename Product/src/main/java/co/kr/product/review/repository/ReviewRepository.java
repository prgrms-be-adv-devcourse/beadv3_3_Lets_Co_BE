package co.kr.product.review.repository;

import co.kr.product.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByProductsIdxAndDelFalseOrderByCreatedAtDesc(Long productsIdx);

    Optional<Review> findByReviewIdxAndDelFalse(Long reviewIdx);

    Optional<Review> findByReviewIdxAndUserIdxAndDelFalse(Long reviewIdx, Long userIdx);

    boolean existsByOrderItemIdxAndDelFalse(Long orderItemIdx);

    boolean existsByProductsIdxAndUserIdxAndDelFalse(Long productsIdx, Long userIdx);

}

