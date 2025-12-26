package vn.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.entity.*;
import vn.repository.*;
import vn.service.RefundService;
import vn.service.OneXuService;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RefundServiceImpl implements RefundService {
    
    @Autowired
    private RefundRepository refundRepository;
    
    @Autowired
    private RefundAuditLogRepository auditLogRepository;
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private OneXuService oneXuService;
    
    /**
     * Tính finalAmount từ order (số tiền khách hàng thực sự đã thanh toán)
     */
    private Double calculateFinalAmount(Order order) {
        if (order.getFinalAmount() != null && order.getFinalAmount() > 0) {
            return order.getFinalAmount();
        }
        // Tính toán nếu chưa có finalAmount
        double subtotal = order.getTotalAmount() != null ? order.getTotalAmount() : 0.0;
        double shippingFee = order.getShippingFee() != null ? order.getShippingFee() : 0.0;
        double discountAmount = order.getDiscountAmount() != null ? order.getDiscountAmount() : 0.0;
        return subtotal + shippingFee - discountAmount;
    }
    
    @Override
    @Transactional
    public Refund createRefundRequest(Long orderId, Long userId, Double refundAmount, String createdBy) {
        // Validate order exists and belongs to user
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));
        
        if (!order.getUser().getUserId().equals(userId)) {
            throw new RuntimeException("Đơn hàng không thuộc về user này");
        }
        
        // Check if refund already exists
        if (refundRepository.findByOrder_OrderId(orderId).isPresent()) {
            throw new RuntimeException("Đơn hàng này đã có yêu cầu hoàn tiền");
        }
        
        // Validate refund amount - chỉ hoàn số tiền khách đã thanh toán (finalAmount)
        Double finalAmount = calculateFinalAmount(order);
        if (refundAmount <= 0 || refundAmount > finalAmount) {
            throw new RuntimeException("Số tiền hoàn không hợp lệ. Số tiền tối đa có thể hoàn: " + finalAmount + " VNĐ");
        }
        
        // Create refund
        Refund refund = new Refund();
        refund.setOrder(order);
        refund.setUser(order.getUser());
        refund.setRefundAmount(refundAmount);
        refund.setRefundStatus(Refund.RefundStatus.PENDING);
        refund.setCreatedBy(createdBy);
        refund.setCreatedAt(LocalDateTime.now());
        refund.setUpdatedAt(LocalDateTime.now());
        
        Refund savedRefund = refundRepository.save(refund);
        
        // Log audit
        logAudit(savedRefund, "CREATED", null, null, null, null, 
                createdBy, "VENDOR", "Vendor đã duyệt hoàn tiền. Số tiền: " + refundAmount + " VNĐ");
        
        return savedRefund;
    }
    
    @Override
    @Transactional
    public Refund createRefundRequestFromUser(Long orderId, Long userId, String refundMethod,
                                              String bankName, String bankAccountNumber,
                                              String accountHolderName, String bankBranch, String contactPhone) {
        // Validate order exists and belongs to user
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));
        
        if (!order.getUser().getUserId().equals(userId)) {
            throw new RuntimeException("Đơn hàng không thuộc về user này");
        }
        
        // Check if refund already exists
        if (refundRepository.findByOrder_OrderId(orderId).isPresent()) {
            throw new RuntimeException("Đơn hàng này đã có yêu cầu hoàn tiền");
        }
        
        // Validate refund method
        if (!"ONEXU".equals(refundMethod) && !"BANK_TRANSFER".equals(refundMethod)) {
            throw new RuntimeException("Phương thức hoàn tiền không hợp lệ");
        }
        
        // Validate bank info if BANK_TRANSFER
        if ("BANK_TRANSFER".equals(refundMethod)) {
            if (bankName == null || bankName.trim().isEmpty()) {
                throw new RuntimeException("Vui lòng chọn ngân hàng");
            }
            if (bankAccountNumber == null || bankAccountNumber.trim().isEmpty()) {
                throw new RuntimeException("Vui lòng nhập số tài khoản");
            }
            if (accountHolderName == null || accountHolderName.trim().isEmpty()) {
                throw new RuntimeException("Vui lòng nhập tên chủ tài khoản");
            }
        }
        
        // Create refund với refundAmount = finalAmount (số tiền khách đã thanh toán, vendor có thể điều chỉnh sau)
        Double finalAmount = calculateFinalAmount(order);
        Refund refund = new Refund();
        refund.setOrder(order);
        refund.setUser(order.getUser());
        refund.setRefundAmount(finalAmount); // Mặc định = finalAmount (số tiền khách đã thanh toán)
        refund.setRefundMethod(Refund.RefundMethod.valueOf(refundMethod));
        refund.setRefundStatus(Refund.RefundStatus.PENDING);
        refund.setCreatedBy(userId.toString());
        refund.setCreatedAt(LocalDateTime.now());
        refund.setUpdatedAt(LocalDateTime.now());
        
        // Set bank info if BANK_TRANSFER
        if ("BANK_TRANSFER".equals(refundMethod)) {
            refund.setBankName(bankName);
            refund.setBankAccountNumber(bankAccountNumber);
            refund.setAccountHolderName(accountHolderName);
            refund.setBankBranch(bankBranch);
            refund.setContactPhone(contactPhone);
        }
        
        Refund savedRefund = refundRepository.save(refund);
        
        // Log audit
        String description = "User đã yêu cầu trả hàng và chọn phương thức hoàn tiền: " + refundMethod;
        if ("BANK_TRANSFER".equals(refundMethod)) {
            description += " - Ngân hàng: " + bankName;
        }
        logAudit(savedRefund, "CREATED", null, "PENDING", null, refundMethod, 
                userId.toString(), "USER", description);
        
        return savedRefund;
    }
    
    @Override
    @Transactional
    public Refund processRefundToOneXu(Long refundId, Long userId) {
        Refund refund = getRefundByIdAndUserId(refundId, userId);
        
        if (refund.getRefundStatus() != Refund.RefundStatus.PENDING) {
            throw new RuntimeException("Refund không ở trạng thái PENDING");
        }
        
        // Process OneXu refund (1 VNĐ = 1 OneXu)
        Double xuAmount = refund.getRefundAmount();
        OneXuTransaction transaction = oneXuService.addXu(
            userId,
            OneXuTransaction.TransactionType.REFUND,
            xuAmount,
            "Hoàn tiền đơn hàng #" + refund.getOrder().getOrderId() + " - " + refund.getRefundAmount() + " VNĐ",
            refund.getOrder().getOrderId()
        );
        
        // Update refund
        refund.setRefundMethod(Refund.RefundMethod.ONEXU);
        refund.setRefundStatus(Refund.RefundStatus.COMPLETED);
        refund.setOneXuTransactionId(transaction.getTransactionId());
        refund.setProcessedAt(LocalDateTime.now());
        refund.setCompletedAt(LocalDateTime.now());
        refund.setUpdatedBy(userId.toString());
        refund.setUpdatedAt(LocalDateTime.now());
        
        Refund savedRefund = refundRepository.save(refund);
        
        // Log audit
        logAudit(savedRefund, "METHOD_SELECTED", null, null, null, "ONEXU", 
                userId.toString(), "USER", "User đã chọn hoàn tiền vào OneXu");
        logAudit(savedRefund, "COMPLETED", "PENDING", "COMPLETED", null, null, 
                "SYSTEM", "SYSTEM", "Hoàn tiền vào OneXu thành công");
        
        return savedRefund;
    }
    
    @Override
    @Transactional
    public Refund processRefundToBank(Long refundId, Long userId, String bankName, 
                                      String bankAccountNumber, String accountHolderName, 
                                      String bankBranch, String contactPhone) {
        Refund refund = getRefundByIdAndUserId(refundId, userId);
        
        if (refund.getRefundStatus() != Refund.RefundStatus.PENDING) {
            throw new RuntimeException("Refund không ở trạng thái PENDING");
        }
        
        // Validate bank info
        if (bankName == null || bankName.trim().isEmpty()) {
            throw new RuntimeException("Vui lòng chọn ngân hàng");
        }
        if (bankAccountNumber == null || bankAccountNumber.trim().isEmpty()) {
            throw new RuntimeException("Vui lòng nhập số tài khoản");
        }
        if (accountHolderName == null || accountHolderName.trim().isEmpty()) {
            throw new RuntimeException("Vui lòng nhập tên chủ tài khoản");
        }
        
        // Update refund
        refund.setRefundMethod(Refund.RefundMethod.BANK_TRANSFER);
        refund.setRefundStatus(Refund.RefundStatus.PROCESSING);
        refund.setBankName(bankName);
        refund.setBankAccountNumber(bankAccountNumber);
        refund.setAccountHolderName(accountHolderName);
        refund.setBankBranch(bankBranch);
        refund.setContactPhone(contactPhone);
        refund.setProcessedAt(LocalDateTime.now());
        refund.setUpdatedBy(userId.toString());
        refund.setUpdatedAt(LocalDateTime.now());
        
        Refund savedRefund = refundRepository.save(refund);
        
        // Log audit
        logAudit(savedRefund, "METHOD_SELECTED", null, null, null, "BANK_TRANSFER", 
                userId.toString(), "USER", "User đã chọn hoàn tiền vào ngân hàng: " + bankName);
        logAudit(savedRefund, "STATUS_CHANGED", "PENDING", "PROCESSING", null, null, 
                "SYSTEM", "SYSTEM", "Đã nhận thông tin ngân hàng, chờ vendor xử lý");
        
        return savedRefund;
    }
    
    @Override
    @Transactional
    public Refund markRefundCompleted(Long refundId, Long vendorId) {
        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy refund"));
        
        if (refund.getRefundMethod() != Refund.RefundMethod.BANK_TRANSFER) {
            throw new RuntimeException("Chỉ có thể đánh dấu hoàn thành cho BANK_TRANSFER");
        }
        
        // Special case: If refund status is PENDING but has bank info, auto-update to PROCESSING first
        if (refund.getRefundStatus() == Refund.RefundStatus.PENDING) {
            if (refund.getBankName() != null && !refund.getBankName().trim().isEmpty() &&
                refund.getBankAccountNumber() != null && !refund.getBankAccountNumber().trim().isEmpty()) {
                // Has bank info but status is still PENDING - auto-update to PROCESSING
                System.out.println("DEBUG: Refund has bank info but status is PENDING, auto-updating to PROCESSING before marking completed");
                refund.setRefundStatus(Refund.RefundStatus.PROCESSING);
                refund.setProcessedAt(LocalDateTime.now());
                refund.setUpdatedBy(vendorId.toString());
                refund.setUpdatedAt(LocalDateTime.now());
                refund = refundRepository.save(refund);
                
                // Log audit for status change
                logAudit(refund, "STATUS_CHANGED", "PENDING", "PROCESSING", null, null, 
                        vendorId.toString(), "VENDOR", "Tự động chuyển status từ PENDING sang PROCESSING (đã có bank info)");
            } else {
                throw new RuntimeException("Refund không ở trạng thái PROCESSING và chưa có đầy đủ thông tin ngân hàng");
            }
        } else if (refund.getRefundStatus() != Refund.RefundStatus.PROCESSING) {
            throw new RuntimeException("Refund không ở trạng thái PROCESSING (current: " + refund.getRefundStatus() + ")");
        }
        
        // Update refund to COMPLETED
        String oldStatus = refund.getRefundStatus().name();
        refund.setRefundStatus(Refund.RefundStatus.COMPLETED);
        refund.setCompletedAt(LocalDateTime.now());
        refund.setUpdatedBy(vendorId.toString());
        refund.setUpdatedAt(LocalDateTime.now());
        
        Refund savedRefund = refundRepository.save(refund);
        
        // Log audit
        logAudit(savedRefund, "COMPLETED", oldStatus, "COMPLETED", null, null, 
                vendorId.toString(), "VENDOR", "Vendor đã xác nhận hoàn tiền vào ngân hàng");
        
        return savedRefund;
    }
    
    @Override
    @Transactional
    public Refund markRefundFailed(Long refundId, String reason, String performedBy) {
        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy refund"));
        
        String oldStatus = refund.getRefundStatus().toString();
        refund.setRefundStatus(Refund.RefundStatus.FAILED);
        refund.setFailedAt(LocalDateTime.now());
        refund.setFailureReason(reason);
        refund.setUpdatedBy(performedBy);
        refund.setUpdatedAt(LocalDateTime.now());
        
        Refund savedRefund = refundRepository.save(refund);
        
        // Log audit
        logAudit(savedRefund, "FAILED", oldStatus, "FAILED", null, null, 
                performedBy, "SYSTEM", "Hoàn tiền thất bại: " + reason);
        
        return savedRefund;
    }
    
    @Override
    public Refund getRefundByOrderId(Long orderId) {
        return refundRepository.findByOrder_OrderId(orderId)
                .orElse(null);
    }
    
    @Override
    public Refund getRefundByIdAndUserId(Long refundId, Long userId) {
        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy refund"));
        
        if (!refund.getUser().getUserId().equals(userId)) {
            throw new RuntimeException("Bạn không có quyền truy cập refund này");
        }
        
        return refund;
    }
    
    @Override
    public Refund getRefundByIdAndShopIds(Long refundId, List<Long> shopIds) {
        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy refund"));
        
        if (refund.getOrder().getShop() == null || 
            !shopIds.contains(refund.getOrder().getShop().getShopId())) {
            throw new RuntimeException("Bạn không có quyền truy cập refund này");
        }
        
        return refund;
    }
    
    @Override
    public boolean hasRefund(Long orderId) {
        return refundRepository.findByOrder_OrderId(orderId).isPresent();
    }
    
    @Override
    @Transactional
    public Refund updateRefundAmount(Long refundId, Double refundAmount, String updatedBy) {
        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy refund"));
        
        // Validate refund amount - chỉ hoàn số tiền khách đã thanh toán (finalAmount)
        Double finalAmount = calculateFinalAmount(refund.getOrder());
        if (refundAmount <= 0 || refundAmount > finalAmount) {
            throw new RuntimeException("Số tiền hoàn không hợp lệ. Số tiền tối đa có thể hoàn: " + finalAmount + " VNĐ");
        }
        
        Double oldAmount = refund.getRefundAmount();
        refund.setRefundAmount(refundAmount);
        refund.setUpdatedBy(updatedBy);
        refund.setUpdatedAt(LocalDateTime.now());
        
        Refund savedRefund = refundRepository.save(refund);
        
        // Log audit
        logAudit(savedRefund, "AMOUNT_UPDATED", null, null, null, null, 
                updatedBy, "VENDOR", "Vendor đã cập nhật số tiền hoàn từ " + oldAmount + " VNĐ thành " + refundAmount + " VNĐ");
        
        return savedRefund;
    }
    
    @Override
    public List<RefundAuditLog> getAuditHistory(Long refundId) {
        return auditLogRepository.getAuditHistory(refundId);
    }
    
    /**
     * Helper method để log audit
     */
    private void logAudit(Refund refund, String action, String oldStatus, String newStatus,
                          String oldMethod, String newMethod, String performedBy, 
                          String performedByType, String description) {
        RefundAuditLog log = new RefundAuditLog();
        log.setRefund(refund);
        log.setAction(action);
        log.setOldStatus(oldStatus);
        log.setNewStatus(newStatus);
        log.setOldMethod(oldMethod);
        log.setNewMethod(newMethod);
        log.setPerformedBy(performedBy);
        log.setPerformedByType(performedByType);
        log.setDescription(description);
        log.setCreatedAt(LocalDateTime.now());
        auditLogRepository.save(log);
    }
}

