package vn.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import vn.entity.Order;
import vn.entity.User;
import vn.service.CartService;
import vn.service.MoMoPaymentService;
import vn.service.OrderService;

@Controller
@RequestMapping("/payment/momo")
public class MoMoPaymentController {

    @Autowired
    private MoMoPaymentService moMoPaymentService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private CartService cartService;

    @Value("${momo.return.url}")
    private String returnUrl;

    @Value("${momo.notify.url}")
    private String notifyUrl;

    /**
     * Tạo payment request với MoMo
     */
    @GetMapping("/create")
    public String createPayment(@RequestParam("orderId") Long orderId,
                               HttpServletRequest request,
                               Model model) {
        try {
            User user = (User) request.getSession().getAttribute("user");
            if (user == null) {
                return "redirect:/login";
            }

            Order order = orderService.getOrderById(orderId);
            if (order == null) {
                model.addAttribute("error", "Không tìm thấy đơn hàng!");
                return "web/checkout-error";
            }

            // Kiểm tra quyền truy cập đơn hàng
            if (!order.getUser().getUserId().equals(user.getUserId())) {
                model.addAttribute("error", "Bạn không có quyền truy cập đơn hàng này!");
                return "web/checkout-error";
            }

            // Kiểm tra trạng thái đơn hàng
            if (order.getPaymentPaid()) {
                model.addAttribute("error", "Đơn hàng đã được thanh toán!");
                return "web/checkout-error";
            }

            // Tạo payment URL
            String paymentUrl = moMoPaymentService.createPaymentRequest(order, returnUrl, notifyUrl);
            
            return "redirect:" + paymentUrl;

        } catch (Exception e) {
            model.addAttribute("error", "Lỗi khi tạo thanh toán MoMo: " + e.getMessage());
            return "web/checkout-error";
        }
    }

    /**
     * Xử lý callback từ MoMo khi thanh toán thành công
     */
    @GetMapping("/return")
    public String paymentReturn(@RequestParam(value = "orderId", required = false) String orderId,
                               @RequestParam(value = "resultCode", required = false) String resultCode,
                               @RequestParam(value = "transId", required = false) String transId,
                               @RequestParam(value = "amount", required = false) String amount,
                               HttpServletRequest request,
                               Model model) {
        try {
            User user = (User) request.getSession().getAttribute("user");
            if (user == null) {
                return "redirect:/login";
            }

            if (orderId == null) {
                model.addAttribute("error", "Thiếu thông tin đơn hàng!");
                return "web/checkout-error";
            }

            Long orderIdLong = Long.parseLong(orderId);
            Order order = orderService.getOrderById(orderIdLong);
            
            if (order == null) {
                model.addAttribute("error", "Không tìm thấy đơn hàng!");
                return "web/checkout-error";
            }

            // Xử lý kết quả thanh toán
            boolean paymentSuccess = false;
            if (resultCode != null && transId != null && amount != null) {
                Double amountDouble = Double.parseDouble(amount); // Giữ nguyên VND
                paymentSuccess = moMoPaymentService.processPaymentCallback(orderIdLong, resultCode, transId, amountDouble);
            }

            if (paymentSuccess) {
                // Thanh toán thành công - clear cart và hiển thị success
                cartService.clearCart(user);
                model.addAttribute("message", "Thanh toán thành công!");
                model.addAttribute("order", order);
                return "web/checkout-success";
            } else {
                // Thanh toán thất bại - xóa order tạm và giữ nguyên cart
                orderService.deleteOrder(orderIdLong);
                model.addAttribute("error", "Thanh toán thất bại! Giỏ hàng của bạn vẫn được giữ nguyên.");
                return "web/checkout-error";
            }

        } catch (Exception e) {
            model.addAttribute("error", "Lỗi khi xử lý kết quả thanh toán: " + e.getMessage());
            return "web/checkout-error";
        }
    }

    /**
     * Xử lý IPN (Instant Payment Notification) từ MoMo
     */
    @PostMapping("/notify")
    @ResponseBody
    public String paymentNotify(@RequestParam(value = "orderId", required = false) String orderId,
                               @RequestParam(value = "resultCode", required = false) String resultCode,
                               @RequestParam(value = "transId", required = false) String transId,
                               @RequestParam(value = "amount", required = false) String amount) {
        try {
            if (orderId == null || resultCode == null || transId == null || amount == null) {
                return "ERROR: Missing parameters";
            }

            Long orderIdLong = Long.parseLong(orderId);
            Double amountDouble = Double.parseDouble(amount); // Giữ nguyên VND
            
            boolean success = moMoPaymentService.processPaymentCallback(orderIdLong, resultCode, transId, amountDouble);
            
            if (success) {
                // Thanh toán thành công - clear cart
                Order order = orderService.getOrderById(orderIdLong);
                if (order != null) {
                    cartService.clearCart(order.getUser());
                }
                return "SUCCESS";
            } else {
                // Thanh toán thất bại - xóa order tạm
                orderService.deleteOrder(orderIdLong);
                return "FAILED";
            }

        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    }

    /**
     * Test MoMo API connection
     */
    @GetMapping("/test")
    @ResponseBody
    public String testMoMoAPI() {
        try {
            // Tạo order test
            Order testOrder = new Order();
            testOrder.setOrderId(999999L);
            testOrder.setTotalAmount(10000.0); // 10,000 VND
            
            String testReturnUrl = "http://localhost:8080/payment/momo/return";
            String testNotifyUrl = "http://localhost:8080/payment/momo/notify";
            
            String paymentUrl = moMoPaymentService.createPaymentRequest(testOrder, testReturnUrl, testNotifyUrl);
            
            return "MoMo API Test Success! Payment URL: " + paymentUrl;
            
        } catch (Exception e) {
            return "MoMo API Test Failed: " + e.getMessage();
        }
    }

    /**
     * Kiểm tra trạng thái thanh toán
     */
    @GetMapping("/status/{orderId}")
    @ResponseBody
    public String checkPaymentStatus(@PathVariable("orderId") Long orderId) {
        try {
            boolean isPaid = moMoPaymentService.checkPaymentStatus(orderId);
            return isPaid ? "PAID" : "UNPAID";
        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    }
}
