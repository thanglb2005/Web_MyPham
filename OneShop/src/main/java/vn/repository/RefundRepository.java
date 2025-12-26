package vn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.entity.Refund;
import vn.entity.Refund.RefundStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface RefundRepository extends JpaRepository<Refund, Long> {
    
    Optional<Refund> findByOrder_OrderId(Long orderId);
    
    List<Refund> findByUser_UserIdOrderByCreatedAtDesc(Long userId);
    
    List<Refund> findByRefundStatus(RefundStatus status);
    
    @Query("SELECT r FROM Refund r WHERE r.order.orderId = :orderId AND r.user.userId = :userId")
    Optional<Refund> findByOrderIdAndUserId(@Param("orderId") Long orderId, @Param("userId") Long userId);
    
    @Query("SELECT COUNT(r) FROM Refund r WHERE r.order.shop.shopId IN :shopIds AND r.refundStatus = :status")
    Long countByShopIdsAndStatus(@Param("shopIds") List<Long> shopIds, @Param("status") RefundStatus status);
    
    @Query("SELECT r FROM Refund r WHERE r.order.shop.shopId IN :shopIds ORDER BY r.createdAt DESC")
    List<Refund> findByShopIdsOrderByCreatedAtDesc(@Param("shopIds") List<Long> shopIds);
}

