package vn.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.entity.Order;
import vn.entity.Refund;
import vn.entity.User;
import vn.repository.OrderRepository;
import vn.service.RefundService;
import vn.service.OneXuService;

@Controller
public class RefundController {
    
    @Autowired
    private RefundService refundService;
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private OneXuService oneXuService;
    
    /**
     * Hiển thị form chọn phương thức hoàn tiền
     */
    @GetMapping("/refund-request/{orderId}")
    public String showRefundRequestForm(@PathVariable Long orderId, 
                                       HttpSession session, 
                                       Model model,
                                       RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        // Get order
        Order order = orderRepository.findById(orderId)
                .orElse(null);
        if (order == null) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy đơn hàng");
            return "redirect:/user/my-orders";
        }
        
        // Validate ownership
        if (!order.getUser().getUserId().equals(user.getUserId())) {
            redirectAttributes.addFlashAttribute("error", "Bạn không có quyền truy cập đơn hàng này");
            return "redirect:/user/my-orders";
        }
        
        // Validate order status
        if (order.getStatus() != Order.OrderStatus.RETURNED) {
            redirectAttributes.addFlashAttribute("error", "Đơn hàng chưa được duyệt hoàn tiền");
            return "redirect:/user/my-orders";
        }
        
        // Get refund
        Refund refund = refundService.getRefundByOrderId(orderId);
        if (refund == null) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy yêu cầu hoàn tiền");
            return "redirect:/user/my-orders";
        }
        
        // Check if already processed
        if (refund.getRefundStatus() != Refund.RefundStatus.PENDING) {
            redirectAttributes.addFlashAttribute("info", "Yêu cầu hoàn tiền đã được xử lý");
            return "redirect:/order-detail/" + orderId;
        }
        
        // Get user's current OneXu balance
        Double currentBalance = oneXuService.getUserBalance(user.getUserId());
        Double balanceAfter = currentBalance + refund.getRefundAmount();
        
        model.addAttribute("order", order);
        model.addAttribute("refund", refund);
        model.addAttribute("currentBalance", currentBalance);
        model.addAttribute("balanceAfter", balanceAfter);
        
        return "web/refund-request";
    }
    
    /**
     * Xử lý submit form hoàn tiền
     */
    @PostMapping("/refund-request/{orderId}/submit")
    public String submitRefundRequest(@PathVariable Long orderId,
                                     @RequestParam String refundMethod,
                                     @RequestParam(required = false) String bankName,
                                     @RequestParam(required = false) String bankAccountNumber,
                                     @RequestParam(required = false) String accountHolderName,
                                     @RequestParam(required = false) String bankBranch,
                                     @RequestParam(required = false) String contactPhone,
                                     HttpSession session,
                                     RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        try {
            // Get refund
            Refund refund = refundService.getRefundByOrderId(orderId);
            if (refund == null) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy yêu cầu hoàn tiền");
                return "redirect:/user/my-orders";
            }
            
            // Validate ownership
            if (!refund.getUser().getUserId().equals(user.getUserId())) {
                redirectAttributes.addFlashAttribute("error", "Bạn không có quyền truy cập refund này");
                return "redirect:/user/my-orders";
            }
            
            // Process based on method
            if ("ONEXU".equals(refundMethod)) {
                refundService.processRefundToOneXu(refund.getRefundId(), user.getUserId());
                redirectAttributes.addFlashAttribute("success", 
                    "Đã hoàn tiền " + refund.getRefundAmount() + " VNĐ vào OneXu của bạn!");
            } else if ("BANK_TRANSFER".equals(refundMethod)) {
                refundService.processRefundToBank(refund.getRefundId(), user.getUserId(), 
                    bankName, bankAccountNumber, accountHolderName, bankBranch, contactPhone);
                redirectAttributes.addFlashAttribute("success", 
                    "Đã gửi thông tin ngân hàng. Vendor sẽ xử lý trong 1-3 ngày làm việc.");
            } else {
                redirectAttributes.addFlashAttribute("error", "Phương thức hoàn tiền không hợp lệ");
                return "redirect:/refund-request/" + orderId;
            }
            
            return "redirect:/order-detail/" + orderId;
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi xử lý: " + e.getMessage());
            return "redirect:/refund-request/" + orderId;
        }
    }
}

