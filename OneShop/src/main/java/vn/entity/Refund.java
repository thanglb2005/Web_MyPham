package vn.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "refunds")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Refund {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "refund_id")
    private Long refundId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "refund_amount", nullable = false)
    private Double refundAmount;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "refund_method", nullable = false, length = 20)
    private RefundMethod refundMethod;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "refund_status", nullable = false, length = 20)
    private RefundStatus refundStatus = RefundStatus.PENDING;
    
    @Column(name = "one_xu_transaction_id")
    private Long oneXuTransactionId;
    
    @Column(name = "bank_name", length = 255)
    private String bankName;
    
    @Column(name = "bank_account_number", length = 50)
    private String bankAccountNumber;
    
    @Column(name = "account_holder_name", length = 255)
    private String accountHolderName;
    
    @Column(name = "bank_branch", length = 255)
    private String bankBranch;
    
    @Column(name = "contact_phone", length = 20)
    private String contactPhone;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "processed_at")
    private LocalDateTime processedAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @Column(name = "failed_at")
    private LocalDateTime failedAt;
    
    @Column(name = "failure_reason", length = 500)
    private String failureReason;
    
    @Column(name = "notes", length = 1000)
    private String notes;
    
    @Column(name = "created_by", length = 100)
    private String createdBy;
    
    @Column(name = "updated_by", length = 100)
    private String updatedBy;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    public enum RefundMethod {
        ONEXU,           // Hoàn tiền vào OneXu
        BANK_TRANSFER    // Hoàn tiền vào tài khoản ngân hàng
    }
    
    public enum RefundStatus {
        PENDING,         // Chờ user chọn phương thức
        PROCESSING,     // Đang xử lý (BANK_TRANSFER đã nhận thông tin, chờ vendor chuyển khoản)
        COMPLETED,      // Đã hoàn thành
        FAILED          // Thất bại
    }
}

