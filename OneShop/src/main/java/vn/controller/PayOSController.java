package vn.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import vn.entity.Order;
import vn.entity.User;
import vn.service.OrderService;
import vn.service.CartService;
import vn.payment.gateway.PaymentGatewayPort;
import vn.payment.gateway.PaymentCallbackResult;
import vn.payment.gateway.PaymentWebhookResult;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PayOSController {

    @Value("${payos.return-url:http://localhost:8080/payos/return}")
    private String payosReturnUrl;

    private final OrderService orderService;
    private final CartService cartService;
    private final PaymentGatewayPort paymentGateway;

    public PayOSController(OrderService orderService, CartService cartService,
                           @org.springframework.beans.factory.annotation.Qualifier("payOSGatewayAdapter") PaymentGatewayPort paymentGateway) {
        this.orderService = orderService;
        this.cartService = cartService;
        this.paymentGateway = paymentGateway;
    }

    @GetMapping("/payos/create-payment")
    public String createPayment(@RequestParam Long orderId, HttpServletRequest request) {
        try {
            Order order = orderService.findById(orderId).orElse(null);
            if (order == null) return "redirect:/checkout?error=Khong tim thay don hang";
             
            String paymentUrl = paymentGateway.createPaymentUrl(order, payosReturnUrl, payosReturnUrl.replace("/return", "/cancel"));
            return "redirect:" + paymentUrl;
        } catch (Exception e) {
            String encodedParams = "";
            try {
                encodedParams = java.net.URLEncoder.encode("Lỗi tạo thanh toán: " + (e.getMessage() == null ? "Null" : e.getMessage()), "UTF-8");
            } catch (Exception ex) {}
            return "redirect:/checkout?error=" + encodedParams;
        }
    }

    @GetMapping("/payos/return")
    public String paymentReturn(HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute("user");
        if (user == null) return "redirect:/login";

        PaymentCallbackResult result = paymentGateway.processCallback(request);
        if (result.getOrderId() == null) return "redirect:/checkout-error?message=Loi xu ly thanh toan";

        Order order = orderService.findById(result.getOrderId()).orElse(null);
        if (order != null && order.getUser().getUserId().equals(user.getUserId())) {
            if (result.isSuccess()) {
                order.setPaymentPaid(true);
                order.setStatus(Order.OrderStatus.CONFIRMED);
                orderService.updateOrder(order);
                
                cartService.clearCart(user);
                request.getSession().removeAttribute("oneVoucher");
                request.getSession().removeAttribute("oneVoucherDiscount");
                request.getSession().removeAttribute("shopVoucher");
                request.getSession().removeAttribute("shopVoucherDiscount");
                request.getSession().removeAttribute("xuAmount");
                request.getSession().removeAttribute("xuDiscount");
                
                return "redirect:/checkout-success?orderId=" + result.getOrderId();
            } else if (result.isCancel()) {
                order.setPaymentPaid(false);
                order.setStatus(Order.OrderStatus.CANCELLED);
                orderService.updateOrder(order);
                return "redirect:/checkout-error?message=Ban da huy thanh toan PayOS";
            } else {
                order.setPaymentPaid(false);
                order.setStatus(Order.OrderStatus.CANCELLED);
                orderService.updateOrder(order);
                return "redirect:/checkout-error?message=Thanh toan that bai";
            }
        }
        return "redirect:/checkout-error?message=Khong tim thay don hang";
    }

    @GetMapping("/payos/cancel")
    public String paymentCancel(HttpServletRequest request) {
        return paymentReturn(request); 
    }

    @PostMapping("/payos/webhook")
    @ResponseBody
    public Map<String, Object> webhook(@RequestBody String payload, HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            PaymentWebhookResult result = paymentGateway.processWebhook(request, payload);
            if (!result.isValidSignature()) {
                response.put("success", false);
                response.put("message", "Invalid signature");
                return response;
            }

            if (result.getOrderId() != null) {
                Order order = orderService.findById(result.getOrderId()).orElse(null);
                if (order != null) {
                    if ("PAID".equals(result.getStatus())) {
                        order.setPaymentPaid(true);
                        order.setStatus(Order.OrderStatus.CONFIRMED);
                        orderService.updateOrder(order);
                    } else if ("CANCELLED".equals(result.getStatus())) {
                        order.setPaymentPaid(false);
                        order.setStatus(Order.OrderStatus.CANCELLED);
                        orderService.updateOrder(order);
                    }
                }
            }
            response.put("success", true);
            response.put("message", "Webhook processed successfully");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Webhook error: " + e.getMessage());
        }
        return response;
    }
}
