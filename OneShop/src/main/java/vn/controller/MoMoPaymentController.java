package vn.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import vn.entity.Order;
import vn.entity.User;
import vn.entity.OneXuTransaction;
import vn.repository.UserRepository;
import vn.repository.OneXuTransactionRepository;
import vn.service.CartService;
import vn.service.OrderService;
import vn.payment.gateway.PaymentGatewayPort;
import vn.payment.gateway.PaymentCallbackResult;

@Controller
@RequestMapping("/payment/momo")
public class MoMoPaymentController {

    @Autowired
    @org.springframework.beans.factory.annotation.Qualifier("moMoGatewayAdapter")
    private PaymentGatewayPort paymentGateway;

    @Autowired
    private OrderService orderService;

    @Autowired
    private CartService cartService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private OneXuTransactionRepository oneXuTransactionRepository;

    @Value("${momo.return.url}")
    private String returnUrl;

    @Value("${momo.notify.url}")
    private String notifyUrl;

    @GetMapping("/create")
    public String createPayment(@RequestParam("orderId") Long orderId, HttpServletRequest request, Model model) {
        try {
            User user = (User) request.getSession().getAttribute("user");
            if (user == null) return "redirect:/login";

            Order order = orderService.getOrderById(orderId);
            if (order == null || !order.getUser().getUserId().equals(user.getUserId())) {
                model.addAttribute("error", "Không tìm thấy hoặc không có quyền truy cập đơn hàng!");
                return "web/checkout-error";
            }
            if (order.getPaymentPaid()) {
                model.addAttribute("error", "Đơn hàng đã được thanh toán!");
                return "web/checkout-error";
            }

            String paymentUrl = paymentGateway.createPaymentUrl(order, returnUrl, notifyUrl);
            return "redirect:" + paymentUrl;
        } catch (Exception e) {
            model.addAttribute("error", "Lỗi: " + e.getMessage());
            return "web/checkout-error";
        }
    }

    @GetMapping("/return")
    public String paymentReturn(HttpServletRequest request, Model model) {
        try {
             PaymentCallbackResult result = paymentGateway.processCallback(request);
             User user = (User) request.getSession().getAttribute("user");
             if (user == null) return "redirect:/login";

             if (result.getOrderId() == null) {
                 model.addAttribute("error", result.getMessage());
                 return "web/checkout-error";
             }

             Order order = orderService.getOrderById(result.getOrderId());
             if (order == null) {
                 model.addAttribute("error", "Không tìm thấy đơn hàng!");
                 return "web/checkout-error";
             }

             if (result.isSuccess()) {
                 String orderNote = order.getNote();
                 if (orderNote != null && orderNote.contains("OneXu:")) {
                     try {
                         int xuStartIndex = orderNote.indexOf("OneXu:") + 7;
                         int xuEndIndex = orderNote.indexOf("xu", xuStartIndex);
                         if (xuEndIndex > xuStartIndex) {
                             Integer xuAmount = Integer.parseInt(orderNote.substring(xuStartIndex, xuEndIndex).trim());
                             Double balance = user.getOneXuBalance() != null ? user.getOneXuBalance() : 0.0;
                             Double newBalance = Math.max(0, balance - xuAmount);
                             user.setOneXuBalance(newBalance);
                             userRepository.save(user);

                             OneXuTransaction xuTransaction = new OneXuTransaction(
                                 user.getUserId(), OneXuTransaction.TransactionType.PURCHASE,
                                 -xuAmount.doubleValue(), newBalance, 
                                 "Sử dụng " + xuAmount + " xu cho đơn hàng #" + order.getOrderId() + " (MoMo)",
                                 order.getOrderId()
                             );
                             oneXuTransactionRepository.save(xuTransaction);
                             request.getSession().setAttribute("user", user);
                         }
                     } catch (Exception e) { System.out.println("Failed to parse xu amount"); }
                 }

                 cartService.clearCart(user);
                 request.getSession().removeAttribute("oneVoucher");
                 request.getSession().removeAttribute("oneVoucherDiscount");
                 request.getSession().removeAttribute("shopVoucher");
                 request.getSession().removeAttribute("shopVoucherDiscount");
                 request.getSession().removeAttribute("xuAmount");
                 request.getSession().removeAttribute("xuDiscount");

                 model.addAttribute("message", "Thanh toán thành công!");
                 model.addAttribute("order", order);
                 return "web/checkout-success";
             } else {
                 model.addAttribute("error", "Thanh toán thất bại! " + result.getMessage());
                 return "web/checkout-error";
             }
        } catch (Exception e) {
             model.addAttribute("error", "Lỗi: " + e.getMessage());
             return "web/checkout-error";
        }
    }

    @PostMapping("/notify")
    @ResponseBody
    public String paymentNotify(HttpServletRequest request) {
         try {
             vn.payment.gateway.PaymentWebhookResult result = paymentGateway.processWebhook(request, null);
             
             // Update cart clear after success webhook if not already cleared
             if (result.isSuccess() && result.getOrderId() != null) {
                 Order order = orderService.getOrderById(result.getOrderId());
                 if (order != null) cartService.clearCart(order.getUser());
                 return "SUCCESS";
             }
             return "FAILED";
         } catch (Exception e) {
             return "ERROR: " + e.getMessage();
         }
    }

    @GetMapping("/test")
    @ResponseBody
    public String testMoMoAPI() {
         return "Testing via Gateway Adapter";
    }

    @GetMapping("/status/{orderId}")
    @ResponseBody
    public String checkPaymentStatus(@PathVariable("orderId") Long orderId) {
        Order order = orderService.getOrderById(orderId);
        return (order != null && order.getPaymentPaid()) ? "PAID" : "UNPAID";
    }
}
