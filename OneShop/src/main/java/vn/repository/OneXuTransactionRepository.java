package vn.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.entity.OneXuTransaction;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OneXuTransactionRepository extends JpaRepository<OneXuTransaction, Long> {
    
    // Lấy lịch sử giao dịch của user
    @Query("SELECT t FROM OneXuTransaction t WHERE t.userId = :userId ORDER BY t.createdAt DESC")
    Page<OneXuTransaction> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId, Pageable pageable);
    
    // Lấy lịch sử giao dịch theo loại
    @Query("SELECT t FROM OneXuTransaction t WHERE t.userId = :userId AND t.transactionType = :type ORDER BY t.createdAt DESC")
    List<OneXuTransaction> findByUserIdAndTransactionType(@Param("userId") Long userId, @Param("type") OneXuTransaction.TransactionType type);
    
    // Lấy giao dịch theo đơn hàng
    @Query("SELECT t FROM OneXuTransaction t WHERE t.orderId = :orderId")
    List<OneXuTransaction> findByOrderId(@Param("orderId") Long orderId);
    
    // Thống kê tổng xu đã nhận
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM OneXuTransaction t WHERE t.userId = :userId AND t.transactionType IN ('CHECKIN', 'ORDER_REWARD', 'REVIEW_REWARD', 'REVIEW_IMAGE', 'REVIEW_VIDEO')")
    Double getTotalEarnedXu(@Param("userId") Long userId);
    
    // Thống kê tổng xu từ check-in
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM OneXuTransaction t WHERE t.userId = :userId AND t.transactionType = :type")
    Double getTotalXuByType(@Param("userId") Long userId, @Param("type") OneXuTransaction.TransactionType type);
    
    // Thống kê tổng xu đã sử dụng
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM OneXuTransaction t WHERE t.userId = :userId AND t.transactionType IN ('PURCHASE', 'REFUND')")
    Double getTotalSpentXu(@Param("userId") Long userId);
    
    // Lấy giao dịch trong khoảng thời gian
    @Query("SELECT t FROM OneXuTransaction t WHERE t.userId = :userId AND t.createdAt BETWEEN :startDate AND :endDate ORDER BY t.createdAt DESC")
    List<OneXuTransaction> findByUserIdAndCreatedAtBetween(@Param("userId") Long userId, 
                                                          @Param("startDate") LocalDateTime startDate, 
                                                          @Param("endDate") LocalDateTime endDate);
    
    // Lấy lịch sử check-in từ transactions
    @Query("SELECT t FROM OneXuTransaction t WHERE t.userId = :userId AND t.transactionType = :type ORDER BY t.createdAt DESC")
    List<OneXuTransaction> findCheckinHistory(@Param("userId") Long userId, @Param("type") OneXuTransaction.TransactionType type);
    
    // Kiểm tra đã check-in hôm nay chưa
    @Query("SELECT COUNT(t) > 0 FROM OneXuTransaction t WHERE t.userId = :userId AND t.transactionType = :type AND t.createdAt >= :startOfDay AND t.createdAt < :endOfDay")
    boolean hasCheckedInToday(@Param("userId") Long userId, @Param("type") OneXuTransaction.TransactionType type, @Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);
    
    // Đếm tổng số check-in
    @Query("SELECT COUNT(t) FROM OneXuTransaction t WHERE t.userId = :userId AND t.transactionType = :type")
    Long countTotalCheckins(@Param("userId") Long userId, @Param("type") OneXuTransaction.TransactionType type);
}
