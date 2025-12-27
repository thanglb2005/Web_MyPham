package vn.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.entity.FlashSaleOrder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for FlashSaleOrder entity
 * @author OneShop Team
 */
@Repository
public interface FlashSaleOrderRepository extends JpaRepository<FlashSaleOrder, Long> {
    
    // Find orders by flash sale ID
    List<FlashSaleOrder> findByFlashSaleId(Long flashSaleId);
    
    // Find orders by flash sale ID with pagination
    Page<FlashSaleOrder> findByFlashSaleId(Long flashSaleId, Pageable pageable);
    
    // Find orders by order ID
    List<FlashSaleOrder> findByOrderOrderId(Long orderId);
    
    // Find orders by user ID
    List<FlashSaleOrder> findByUserUserId(Long userId);
    
    // Find orders by user ID with pagination
    Page<FlashSaleOrder> findByUserUserId(Long userId, Pageable pageable);
    
    // Find orders by user and flash sale
    @Query("SELECT fso FROM FlashSaleOrder fso WHERE fso.user.userId = :userId " +
           "AND fso.flashSaleId = :flashSaleId")
    List<FlashSaleOrder> findByUserIdAndFlashSaleId(@Param("userId") Long userId, 
                                                     @Param("flashSaleId") Long flashSaleId);
    
    // Count purchases by user, flash sale, and product (for checking user purchase limit)
    @Query("SELECT COALESCE(SUM(fso.quantity), 0) FROM FlashSaleOrder fso " +
           "WHERE fso.user.userId = :userId " +
           "AND fso.flashSaleId = :flashSaleId " +
           "AND fso.productId = :productId")
    Integer countByFlashSaleIdAndUserIdAndProductId(@Param("flashSaleId") Long flashSaleId,
                                                     @Param("userId") Long userId,
                                                     @Param("productId") Long productId);
    
    // Find orders by product ID
    List<FlashSaleOrder> findByProductId(Long productId);
    
    // Find orders by product ID with pagination
    Page<FlashSaleOrder> findByProductId(Long productId, Pageable pageable);
    
    // Find orders by date range
    @Query("SELECT fso FROM FlashSaleOrder fso WHERE fso.purchasedAt BETWEEN :startDate AND :endDate")
    List<FlashSaleOrder> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                                        @Param("endDate") LocalDateTime endDate);
    
    // Find orders by date range with pagination
    @Query("SELECT fso FROM FlashSaleOrder fso WHERE fso.purchasedAt BETWEEN :startDate AND :endDate")
    Page<FlashSaleOrder> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                                        @Param("endDate") LocalDateTime endDate,
                                        Pageable pageable);
    
    // Count total quantity sold for a product in flash sale
    @Query("SELECT COALESCE(SUM(fso.quantity), 0) FROM FlashSaleOrder fso " +
           "WHERE fso.flashSaleId = :flashSaleId AND fso.productId = :productId")
    Integer countTotalQuantitySold(@Param("flashSaleId") Long flashSaleId, 
                                   @Param("productId") Long productId);
    
    // Count total orders in flash sale
    long countByFlashSaleId(Long flashSaleId);
    
    // Count total orders by user in flash sale
    @Query("SELECT COUNT(fso) FROM FlashSaleOrder fso WHERE fso.user.userId = :userId " +
           "AND fso.flashSaleId = :flashSaleId")
    long countByUserIdAndFlashSaleId(@Param("userId") Long userId, 
                                     @Param("flashSaleId") Long flashSaleId);
}

