package vn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.entity.RefundAuditLog;

import java.util.List;

@Repository
public interface RefundAuditLogRepository extends JpaRepository<RefundAuditLog, Long> {
    
    List<RefundAuditLog> findByRefund_RefundIdOrderByCreatedAtDesc(Long refundId);
    
    @Query("SELECT l FROM RefundAuditLog l WHERE l.refund.refundId = :refundId ORDER BY l.createdAt DESC")
    List<RefundAuditLog> getAuditHistory(@Param("refundId") Long refundId);
}

