package vn.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "one_xu_transactions")
public class OneXuTransaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id")
    private Long transactionId;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType transactionType;
    
    @Column(name = "amount", nullable = false)
    private Double amount;
    
    @Column(name = "balance_after", nullable = false)
    private Double balanceAfter;
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "order_id")
    private Long orderId;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    public enum TransactionType {
        CHECKIN,        // Check-in hàng ngày
        ORDER_REWARD,   // Thưởng từ đơn hàng hoàn thành
        PURCHASE,       // Mua sắm bằng One Xu
        REFUND,         // Hoàn xu
        REVIEW_REWARD   // Thưởng từ đánh giá sản phẩm lần đầu
    }
    
    // Constructors
    public OneXuTransaction() {
        this.createdAt = LocalDateTime.now();
    }
    
    public OneXuTransaction(Long userId, TransactionType transactionType, Double amount, 
                           Double balanceAfter, String description, Long orderId) {
        this();
        this.userId = userId;
        this.transactionType = transactionType;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.description = description;
        this.orderId = orderId;
    }
    
    // Getters and Setters
    public Long getTransactionId() {
        return transactionId;
    }
    
    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public TransactionType getTransactionType() {
        return transactionType;
    }
    
    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }
    
    public Double getAmount() {
        return amount;
    }
    
    public void setAmount(Double amount) {
        this.amount = amount;
    }
    
    public Double getBalanceAfter() {
        return balanceAfter;
    }
    
    public void setBalanceAfter(Double balanceAfter) {
        this.balanceAfter = balanceAfter;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Long getOrderId() {
        return orderId;
    }
    
    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
