package vn.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "refund_audit_log")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RefundAuditLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long logId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "refund_id", nullable = false)
    private Refund refund;
    
    @Column(name = "action", nullable = false, length = 50)
    private String action;  // CREATED, STATUS_CHANGED, METHOD_SELECTED, COMPLETED, FAILED
    
    @Column(name = "old_status", length = 20)
    private String oldStatus;
    
    @Column(name = "new_status", length = 20)
    private String newStatus;
    
    @Column(name = "old_method", length = 20)
    private String oldMethod;
    
    @Column(name = "new_method", length = 20)
    private String newMethod;
    
    @Column(name = "performed_by", nullable = false, length = 100)
    private String performedBy;  // user_id hoặc 'SYSTEM'
    
    @Column(name = "performed_by_type", nullable = false, length = 20)
    private String performedByType;  // 'USER', 'VENDOR', 'SYSTEM'
    
    @Column(name = "description", length = 500)
    private String description;
    
    @Column(name = "metadata", columnDefinition = "NVARCHAR(MAX)")
    private String metadata;  // JSON string
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}

