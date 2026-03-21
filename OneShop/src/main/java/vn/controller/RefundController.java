package vn.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.entity.User;
import vn.facade.refund.RefundFacade;
import vn.facade.refund.dto.RefundRequestViewResult;
import vn.facade.refund.dto.RefundSubmitResult;

@Controller
public class RefundController {

    @Autowired
    private RefundFacade refundFacade;
    
    /**
     * Hiển thị form chọn phương thức hoàn tiền
     */
    @GetMapping("/refund-request/{orderId}")
    public String showRefundRequestForm(@PathVariable Long orderId, 
                                       HttpSession session, 
                                       Model model,
                                       RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        // ===== CODE CŨ (chưa Facade) =====
        // if (user == null) {
        //     return "redirect:/login";
        // }
        // Order order = orderRepository.findById(orderId).orElse(null);
        // if (order == null) {
        //     redirectAttributes.addFlashAttribute("error", "Không tìm thấy đơn hàng");
        //     return "redirect:/user/my-orders";
        // }
        // if (!order.getUser().getUserId().equals(user.getUserId())) {
        //     redirectAttributes.addFlashAttribute("error", "Bạn không có quyền truy cập đơn hàng này");
        //     return "redirect:/user/my-orders";
        // }
        // if (order.getStatus() != Order.OrderStatus.RETURNED) {
        //     redirectAttributes.addFlashAttribute("error", "Đơn hàng chưa được duyệt hoàn tiền");
        //     return "redirect:/user/my-orders";
        // }
        // Refund refund = refundService.getRefundByOrderId(orderId);
        // if (refund == null) {
        //     redirectAttributes.addFlashAttribute("error", "Không tìm thấy yêu cầu hoàn tiền");
        //     return "redirect:/user/my-orders";
        // }
        // if (refund.getRefundStatus() != Refund.RefundStatus.PENDING) {
        //     redirectAttributes.addFlashAttribute("info", "Yêu cầu hoàn tiền đã được xử lý");
        //     return "redirect:/order-detail/" + orderId;
        // }
        // Double currentBalance = oneXuService.getUserBalance(user.getUserId());
        // Double balanceAfter = currentBalance + refund.getRefundAmount();
        // model.addAttribute("order", order);
        // model.addAttribute("refund", refund);
        // model.addAttribute("currentBalance", currentBalance);
        // model.addAttribute("balanceAfter", balanceAfter);
        // return "web/refund-request";
        // ===== HẾT CODE CŨ =====

        // Code mới: dùng RefundFacade làm một "cửa vào" duy nhất cho luồng refund request view
        RefundRequestViewResult result = refundFacade.prepareRefundRequestView(orderId, user);
        if (!result.isSuccess()) {
            if (result.getErrorMessage() != null) {
                redirectAttributes.addFlashAttribute("error", result.getErrorMessage());
            } else if (result.getInfoMessage() != null) {
                redirectAttributes.addFlashAttribute("info", result.getInfoMessage());
            }
            return "redirect:" + result.getRedirectUrl();
        }

        model.addAttribute("order", result.getOrder());
        model.addAttribute("refund", result.getRefund());
        model.addAttribute("currentBalance", result.getCurrentBalance());
        model.addAttribute("balanceAfter", result.getBalanceAfter());
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
        // ===== CODE CŨ (chưa Facade) =====
        // if (user == null) {
        //     return "redirect:/login";
        // }
        // try {
        //     Refund refund = refundService.getRefundByOrderId(orderId);
        //     if (refund == null) {
        //         redirectAttributes.addFlashAttribute("error", "Không tìm thấy yêu cầu hoàn tiền");
        //         return "redirect:/user/my-orders";
        //     }
        //     if (!refund.getUser().getUserId().equals(user.getUserId())) {
        //         redirectAttributes.addFlashAttribute("error", "Bạn không có quyền truy cập refund này");
        //         return "redirect:/user/my-orders";
        //     }
        //     if ("ONEXU".equals(refundMethod)) {
        //         refundService.processRefundToOneXu(refund.getRefundId(), user.getUserId());
        //         redirectAttributes.addFlashAttribute("success",
        //                 "Đã hoàn tiền " + refund.getRefundAmount() + " VNĐ vào OneXu của bạn!");
        //     } else if ("BANK_TRANSFER".equals(refundMethod)) {
        //         refundService.processRefundToBank(refund.getRefundId(), user.getUserId(),
        //                 bankName, bankAccountNumber, accountHolderName, bankBranch, contactPhone);
        //         redirectAttributes.addFlashAttribute("success",
        //                 "Đã gửi thông tin ngân hàng. Vendor sẽ xử lý trong 1-3 ngày làm việc.");
        //     } else {
        //         redirectAttributes.addFlashAttribute("error", "Phương thức hoàn tiền không hợp lệ");
        //         return "redirect:/refund-request/" + orderId;
        //     }
        //     return "redirect:/order-detail/" + orderId;
        // } catch (Exception e) {
        //     redirectAttributes.addFlashAttribute("error", "Lỗi xử lý: " + e.getMessage());
        //     return "redirect:/refund-request/" + orderId;
        // }
        // ===== HẾT CODE CŨ =====

        // Code mới: dùng RefundFacade để điều phối toàn bộ submit refund
        RefundSubmitResult result = refundFacade.submitRefundRequest(
                orderId, user, refundMethod, bankName, bankAccountNumber, accountHolderName, bankBranch, contactPhone
        );
        if (result.isSuccess() && result.getMessage() != null) {
            redirectAttributes.addFlashAttribute("success", result.getMessage());
        } else if (!result.isSuccess() && result.getMessage() != null) {
            redirectAttributes.addFlashAttribute("error", result.getMessage());
        }
        return "redirect:" + result.getRedirectUrl();
    }
}

