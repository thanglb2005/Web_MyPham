package vn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.entity.Comment;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("SELECT c FROM Comment c WHERE c.orderDetail.orderDetailId = :orderDetailId AND c.user.userId = :userId")
    Optional<Comment> findByOrderDetailAndUser(@Param("orderDetailId") Long orderDetailId,
                                               @Param("userId") Long userId);

    @Query("SELECT c FROM Comment c WHERE c.product.productId = :productId ORDER BY c.rateDate DESC")
    List<Comment> findByProduct(@Param("productId") Long productId);

    // Lấy đánh giá gần nhất của user cho 1 sản phẩm (fallback khi thiếu theo orderDetail)
    @Query("SELECT c FROM Comment c WHERE c.product.productId = :productId AND c.user.userId = :userId ORDER BY c.rateDate DESC")
    Optional<Comment> findTopByProduct_ProductIdAndUser_UserIdOrderByRateDateDesc(@Param("productId") Long productId, @Param("userId") Long userId);
}


