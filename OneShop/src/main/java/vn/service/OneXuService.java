package vn.service;

import org.springframework.data.domain.Page;
import vn.entity.OneXuTransaction;
import vn.entity.OneXuWeeklySchedule;
import java.util.List;

public interface OneXuService {
    
    // Check-in hàng ngày
    OneXuTransaction dailyCheckin(Long userId);
    
    // Kiểm tra đã check-in hôm nay chưa
    boolean hasCheckedInToday(Long userId);
    
    // Lấy lịch sử check-in
    List<OneXuTransaction> getCheckinHistory(Long userId);
    
    // Lấy lịch trình tuần
    List<OneXuWeeklySchedule> getWeeklySchedule();
    
    // Lấy số dư One Xu
    Double getUserBalance(Long userId);
    
    // Cập nhật số dư One Xu
    void updateUserBalance(Long userId, Double newBalance);
    
    // Thêm xu cho user (check-in, thưởng đơn hàng)
    OneXuTransaction addXu(Long userId, OneXuTransaction.TransactionType type, Double amount, String description, Long orderId);
    
    // Trừ xu cho user (mua sắm)
    OneXuTransaction deductXu(Long userId, Double amount, String description);
    
    // Lấy lịch sử giao dịch
    Page<OneXuTransaction> getTransactionHistory(Long userId, int page, int size);
    
    // Thưởng xu từ đơn hàng hoàn thành (1% giá trị đơn hàng)
    OneXuTransaction rewardFromOrder(Long userId, Long orderId, Double orderAmount);
    
    // Thưởng xu từ đánh giá sản phẩm lần đầu (300 xu)
    OneXuTransaction rewardFromReview(Long userId, Long productId);
    
    // Trừ xu khi xóa đánh giá sản phẩm (300 xu)
    OneXuTransaction deductFromReviewDeletion(Long userId, Long productId);
    
    // Lấy thống kê One Xu
    OneXuStats getOneXuStats(Long userId);
    
    // Lấy tổng xu từ check-in
    Double getTotalXuFromCheckin(Long userId);
    
    // Đồng bộ hóa số dư user với transactions
    void syncUserBalance(Long userId);
    
    // DTO cho thống kê
    class OneXuStats {
        private Double currentBalance;
        private Double totalEarned;
        private Double totalSpent;
        private Long totalCheckins;
        private Long consecutiveCheckins;
        
        // Constructors, getters, setters
        public OneXuStats() {}
        
        public OneXuStats(Double currentBalance, Double totalEarned, Double totalSpent, 
                         Long totalCheckins, Long consecutiveCheckins) {
            this.currentBalance = currentBalance;
            this.totalEarned = totalEarned;
            this.totalSpent = totalSpent;
            this.totalCheckins = totalCheckins;
            this.consecutiveCheckins = consecutiveCheckins;
        }
        
        // Getters and Setters
        public Double getCurrentBalance() { return currentBalance; }
        public void setCurrentBalance(Double currentBalance) { this.currentBalance = currentBalance; }
        
        public Double getTotalEarned() { return totalEarned; }
        public void setTotalEarned(Double totalEarned) { this.totalEarned = totalEarned; }
        
        public Double getTotalSpent() { return totalSpent; }
        public void setTotalSpent(Double totalSpent) { this.totalSpent = totalSpent; }
        
        public Long getTotalCheckins() { return totalCheckins; }
        public void setTotalCheckins(Long totalCheckins) { this.totalCheckins = totalCheckins; }
        
        public Long getConsecutiveCheckins() { return consecutiveCheckins; }
        public void setConsecutiveCheckins(Long consecutiveCheckins) { this.consecutiveCheckins = consecutiveCheckins; }
    }
}
