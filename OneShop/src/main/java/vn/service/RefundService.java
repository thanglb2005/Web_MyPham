package vn.service;

import vn.entity.Refund;
import vn.entity.RefundAuditLog;

import java.util.List;

public interface RefundService {
    
    /**
     * Tạo refund request khi vendor approve return
     */
    Refund createRefundRequest(Long orderId, Long userId, Double refundAmount, String createdBy);
    
    /**
     * Tạo refund request từ user khi yêu cầu trả hàng (với phương thức hoàn tiền đã chọn)
     */
    Refund createRefundRequestFromUser(Long orderId, Long userId, String refundMethod, 
                                      String bankName, String bankAccountNumber, 
                                      String accountHolderName, String bankBranch, String contactPhone);
    
    /**
     * User chọn phương thức hoàn tiền vào OneXu
     */
    Refund processRefundToOneXu(Long refundId, Long userId);
    
    /**
     * User chọn phương thức hoàn tiền vào ngân hàng
     */
    Refund processRefundToBank(Long refundId, Long userId, String bankName, 
                               String bankAccountNumber, String accountHolderName, 
                               String bankBranch, String contactPhone);
    
    /**
     * Vendor đánh dấu đã hoàn tiền vào ngân hàng
     */
    Refund markRefundCompleted(Long refundId, Long vendorId);
    
    /**
     * Đánh dấu refund thất bại
     */
    Refund markRefundFailed(Long refundId, String reason, String performedBy);
    
    /**
     * Lấy refund theo orderId
     */
    Refund getRefundByOrderId(Long orderId);
    
    /**
     * Lấy refund theo refundId và userId (validate ownership)
     */
    Refund getRefundByIdAndUserId(Long refundId, Long userId);
    
    /**
     * Lấy refund theo refundId và shopIds (validate vendor ownership)
     */
    Refund getRefundByIdAndShopIds(Long refundId, List<Long> shopIds);
    
    /**
     * Kiểm tra xem order đã có refund chưa
     */
    boolean hasRefund(Long orderId);
    
    /**
     * Cập nhật refundAmount khi vendor approve (nếu refund đã tồn tại từ user)
     */
    Refund updateRefundAmount(Long refundId, Double refundAmount, String updatedBy);
    
    /**
     * Lấy lịch sử audit của refund
     */
    List<RefundAuditLog> getAuditHistory(Long refundId);
}

