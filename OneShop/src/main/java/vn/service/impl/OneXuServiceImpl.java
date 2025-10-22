package vn.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.entity.OneXuTransaction;
import vn.entity.OneXuWeeklySchedule;
import vn.entity.User;
import vn.repository.OneXuTransactionRepository;
import vn.repository.OneXuWeeklyScheduleRepository;
import vn.repository.UserRepository;
import vn.service.OneXuService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class OneXuServiceImpl implements OneXuService {
    
    @Autowired
    private OneXuTransactionRepository transactionRepository;
    
    @Autowired
    private OneXuWeeklyScheduleRepository scheduleRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Override
    public OneXuTransaction dailyCheckin(Long userId) {
        LocalDate today = LocalDate.now();
        
        // Kiểm tra đã check-in hôm nay chưa
        if (hasCheckedInToday(userId)) {
            throw new RuntimeException("Bạn đã check-in hôm nay rồi!");
        }
        
        // Lấy phần thưởng theo ngày trong tuần
        int dayOfWeek = today.getDayOfWeek().getValue(); // 1=Monday, 7=Sunday
        OneXuWeeklySchedule schedule = scheduleRepository.findByDayOfWeek(dayOfWeek);
        
        if (schedule == null) {
            throw new RuntimeException("Không tìm thấy lịch trình phần thưởng cho ngày này!");
        }
        
        Double xuReward = schedule.getXuReward();
        
        // Tạo transaction record (addXu sẽ tự động cập nhật user balance)
        OneXuTransaction checkinTransaction = addXu(userId, OneXuTransaction.TransactionType.CHECKIN, xuReward, 
              "Check-in " + schedule.getDescription(), null);
        
        return checkinTransaction;
    }
    
    @Override
    public boolean hasCheckedInToday(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = now.toLocalDate().atTime(23, 59, 59);
        return transactionRepository.hasCheckedInToday(userId, OneXuTransaction.TransactionType.CHECKIN, startOfDay, endOfDay);
    }
    
    @Override
    public List<OneXuTransaction> getCheckinHistory(Long userId) {
        return transactionRepository.findCheckinHistory(userId, OneXuTransaction.TransactionType.CHECKIN);
    }
    
    @Override
    public List<OneXuWeeklySchedule> getWeeklySchedule() {
        return scheduleRepository.findAllByOrderByDayOfWeek();
    }
    
    @Override
    public Double getUserBalance(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User không tồn tại!"));
        return user.getOneXuBalance();
    }
    
    @Override
    public void updateUserBalance(Long userId, Double newBalance) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User không tồn tại!"));
        user.setOneXuBalance(newBalance);
        userRepository.save(user);
    }
    
    @Override
    public OneXuTransaction addXu(Long userId, OneXuTransaction.TransactionType type, Double amount, String description, Long orderId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User không tồn tại!"));
        
        Double newBalance = user.getOneXuBalance() + amount;
        user.setOneXuBalance(newBalance);
        userRepository.save(user);
        
        OneXuTransaction transaction = new OneXuTransaction(userId, type, amount, newBalance, description, orderId);
        return transactionRepository.save(transaction);
    }
    
    @Override
    public OneXuTransaction deductXu(Long userId, Double amount, String description) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User không tồn tại!"));
        
        if (user.getOneXuBalance() < amount) {
            throw new RuntimeException("Số dư One Xu không đủ!");
        }
        
        Double newBalance = user.getOneXuBalance() - amount;
        user.setOneXuBalance(newBalance);
        userRepository.save(user);
        
        OneXuTransaction transaction = new OneXuTransaction(userId, OneXuTransaction.TransactionType.PURCHASE, 
                                                          -amount, newBalance, description, null);
        return transactionRepository.save(transaction);
    }
    
    @Override
    public Page<OneXuTransaction> getTransactionHistory(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return transactionRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }
    
    @Override
    public OneXuTransaction rewardFromOrder(Long userId, Long orderId, Double orderAmount) {
        // Thưởng 1% giá trị đơn hàng
        Double rewardAmount = (double) Math.round(orderAmount * 0.01);
        String description = "Thưởng từ đơn hàng #" + orderId + " (1% giá trị đơn hàng)";
        
        return addXu(userId, OneXuTransaction.TransactionType.ORDER_REWARD, rewardAmount, description, orderId);
    }
    
    @Override
    public Double getTotalXuFromCheckin(Long userId) {
        Double totalXu = transactionRepository.getTotalXuByType(userId, OneXuTransaction.TransactionType.CHECKIN);
        return totalXu != null ? totalXu : 0.0;
    }
    
    @Override
    public OneXuStats getOneXuStats(Long userId) {
        // Đồng bộ hóa số dư trước khi tính toán
        syncUserBalance(userId);
        
        Double currentBalance = getUserBalance(userId);
        Double totalEarned = transactionRepository.getTotalEarnedXu(userId);
        Double totalSpent = transactionRepository.getTotalSpentXu(userId);
        Long totalCheckins = transactionRepository.countTotalCheckins(userId, OneXuTransaction.TransactionType.CHECKIN);
        
        // Xử lý null values
        if (totalEarned == null) totalEarned = 0.0;
        if (totalSpent == null) totalSpent = 0.0;
        if (totalCheckins == null) totalCheckins = 0L;
        
        // Tính số ngày check-in liên tiếp
        Long consecutiveCheckins = 0L;
        LocalDate today = LocalDate.now();
        LocalDate checkDate = today;
        
        while (true) {
            LocalDateTime startOfDay = checkDate.atStartOfDay();
            LocalDateTime endOfDay = checkDate.atTime(23, 59, 59);
            
            if (transactionRepository.hasCheckedInToday(userId, OneXuTransaction.TransactionType.CHECKIN, startOfDay, endOfDay)) {
                consecutiveCheckins++;
                checkDate = checkDate.minusDays(1);
            } else {
                break;
            }
        }
        
        return new OneXuStats(currentBalance, totalEarned, totalSpent, totalCheckins, consecutiveCheckins);
    }
    
    /**
     * Đồng bộ hóa số dư user với tổng transactions
     */
    @Override
    public void syncUserBalance(Long userId) {
        try {
            // Tính toán số dư thực tế từ transactions
            // Lấy tổng tất cả transactions (CHECKIN/ORDER_REWARD là dương, PURCHASE là âm)
            Double totalEarned = transactionRepository.getTotalEarnedXu(userId);
            Double totalSpent = transactionRepository.getTotalSpentXu(userId);
            
            if (totalEarned == null) totalEarned = 0.0;
            if (totalSpent == null) totalSpent = 0.0;
            
            // PURCHASE amount đã là số âm, nên cộng trực tiếp (totalSpent sẽ là số âm)
            Double actualBalance = totalEarned + totalSpent;
            
            // Lấy user hiện tại
            User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User không tồn tại!"));
            Double currentBalance = user.getOneXuBalance();
            
            // Nếu số dư không khớp, cập nhật lại
            if (!actualBalance.equals(currentBalance)) {
                System.out.println("Syncing balance for user " + userId + ": " + currentBalance + " -> " + actualBalance);
                user.setOneXuBalance(actualBalance);
                userRepository.save(user);
            }
        } catch (Exception e) {
            System.err.println("Error syncing balance for user " + userId + ": " + e.getMessage());
        }
    }

    @Override
    public OneXuTransaction rewardFromReview(Long userId, Long productId) {
        // Thưởng 300 xu cho đánh giá sản phẩm lần đầu
        Double rewardAmount = 300.0;
        String description = "Thưởng từ đánh giá sản phẩm #" + productId;
        
        return addXu(userId, OneXuTransaction.TransactionType.REVIEW_REWARD, rewardAmount, description, null);
    }

    @Override
    public OneXuTransaction deductFromReviewDeletion(Long userId, Long productId) {
        // Trừ 300 xu khi xóa đánh giá sản phẩm
        Double deductAmount = 300.0;
        String description = "Trừ xu do xóa đánh giá sản phẩm #" + productId;
        
        return deductXu(userId, deductAmount, description);
    }
}
