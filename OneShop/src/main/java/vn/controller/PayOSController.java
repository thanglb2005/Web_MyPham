package vn.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import vn.entity.Order;
import vn.entity.User;
import vn.service.OrderService;
import vn.service.CartService;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PayOSController {

    @Value("${payos.client-id:}")
    private String payosClientId;
    
    @Value("${payos.api-key:}")
    private String payosApiKey;
    
    @Value("${payos.checksum-key:}")
    private String payosChecksumKey;
    
    @Value("${payos.return-url:http://localhost:8080/payos/return}")
    private String payosReturnUrl;
    
    @Value("${payos.cancel-url:http://localhost:8080/payos/cancel}")
    private String payosCancelUrl;

    private final OrderService orderService;
    private final CartService cartService;

    public PayOSController(OrderService orderService, CartService cartService) {
        this.orderService = orderService;
        this.cartService = cartService;
    }

    /**
     * Tạo link thanh toán PayOS và redirect
     */
    @GetMapping("/payos/create-payment")
    public String createPayment(@RequestParam Long orderId, HttpServletRequest request) {
        try {
            // Lấy thông tin đơn hàng
            Order order = orderService.findById(orderId).orElse(null);
            if (order == null) {
                return "redirect:/checkout?error=Không tìm thấy đơn hàng";
            }

            // Gọi PayOS API thực tế để tạo payment link
            String paymentUrl = createPayOSPaymentLink(order);
            
            // Redirect đến PayOS
            return "redirect:" + paymentUrl;
            
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/checkout?error=Lỗi tạo thanh toán: " + e.getMessage();
        }
    }

    /**
     * Xử lý callback từ PayOS khi thanh toán thành công
     */
    @GetMapping("/payos/return")
    public String paymentReturn(@RequestParam(required = false) String status,
                               @RequestParam(required = false) String orderId,
                               @RequestParam(required = false) String orderCode,
                               @RequestParam(required = false) String code,
                               HttpServletRequest request) {
        
        User user = (User) request.getSession().getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        
        // Xác định orderId từ các parameters
        String actualOrderId = orderId != null ? orderId : orderCode;
        
        if ("success".equals(status) || "00".equals(code)) {
            // Cập nhật trạng thái đơn hàng
            try {
                Long orderIdLong = Long.parseLong(actualOrderId);
                Order order = orderService.findById(orderIdLong).orElse(null);
                if (order != null && order.getUser().getUserId().equals(user.getUserId())) {
                    // Cập nhật trạng thái đơn hàng thành "PAID"
                    order.setPaymentPaid(true);
                    order.setStatus(Order.OrderStatus.CONFIRMED);
                    orderService.updateOrder(order);
                    
                    // Clear cart và session data
                    cartService.clearCart(user);
                    request.getSession().removeAttribute("oneVoucher");
                    request.getSession().removeAttribute("oneVoucherDiscount");
                    request.getSession().removeAttribute("shopVoucher");
                    request.getSession().removeAttribute("shopVoucherDiscount");
                    request.getSession().removeAttribute("xuAmount");
                    request.getSession().removeAttribute("xuDiscount");
                    
                    // Redirect đến checkout success
                    return "redirect:/checkout-success?orderId=" + actualOrderId;
                } else {
                    return "redirect:/checkout-error?message=Không tìm thấy đơn hàng";
                }
            } catch (NumberFormatException e) {
                return "redirect:/checkout-error?message=Lỗi xử lý thanh toán";
            }
        } else {
            // Thanh toán thất bại - set order thành CANCELLED
            try {
                Long orderIdLong = Long.parseLong(actualOrderId);
                Order order = orderService.findById(orderIdLong).orElse(null);
                if (order != null && order.getUser().getUserId().equals(user.getUserId())) {
                    order.setPaymentPaid(false);
                    order.setStatus(Order.OrderStatus.CANCELLED);
                    orderService.updateOrder(order);
                    System.out.println("Order " + actualOrderId + " marked as CANCELLED due to failed payment");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid orderId in failed payment: " + actualOrderId);
            }
            return "redirect:/checkout-error?message=Thanh toán thất bại";
        }
    }

    /**
     * Xử lý khi người dùng hủy thanh toán
     */
    @GetMapping("/payos/cancel")
    public String paymentCancel(@RequestParam(required = false) String code,
                               @RequestParam(required = false) String id,
                               @RequestParam(required = false) String cancel,
                               @RequestParam(required = false) String status,
                               @RequestParam(required = false) String orderCode,
                               HttpServletRequest request) {
        
        System.out.println("=== PAYOS CANCEL DEBUG ===");
        System.out.println("PayOS Cancel - Code: " + code + ", Status: " + status + ", OrderCode: " + orderCode);
        System.out.println("Full URL: " + request.getRequestURL() + "?" + request.getQueryString());
        System.out.println("==========================");
        
        // Nếu có orderCode, cập nhật trạng thái đơn hàng thành CANCELLED
        if (orderCode != null && !orderCode.isEmpty()) {
            try {
                Long orderIdLong = Long.parseLong(orderCode);
                Order order = orderService.findById(orderIdLong).orElse(null);
                if (order != null) {
                    order.setPaymentPaid(false);
                    order.setStatus(Order.OrderStatus.CANCELLED);
                    orderService.updateOrder(order);
                    System.out.println("Order " + orderCode + " marked as CANCELLED from cancel URL");
                } else {
                    System.out.println("Order not found: " + orderCode);
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid orderCode in cancel URL: " + orderCode);
            }
        }
        
        System.out.println("Redirecting to: /checkout-error?message=Bạn đã hủy thanh toán PayOS");
        return "redirect:/checkout-error?message=Bạn đã hủy thanh toán PayOS";
    }

    /**
     * Test method để kiểm tra PayOS cancel flow
     */
    @GetMapping("/test-payos-cancel")
    public String testPayOSCancel() {
        System.out.println("=== TEST PAYOS CANCEL ===");
        System.out.println("Testing PayOS cancel redirect...");
        return "redirect:/checkout-error?message=Test PayOS Cancel";
    }


    /**
     * Webhook nhận thông báo từ PayOS theo đúng tài liệu
     */
    @PostMapping("/payos/webhook")
    @ResponseBody
    public Map<String, Object> webhook(@RequestBody Map<String, Object> payload,
                                      @RequestHeader(value = "x-payos-signature", required = false) String signature) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            System.out.println("PayOS Webhook received: " + payload);
            System.out.println("PayOS Webhook signature: " + signature);
            
            // Xác thực webhook signature theo PayOS docs
            if (!verifyWebhookSignature(payload, signature)) {
                System.out.println("PayOS Webhook signature verification failed");
                response.put("success", false);
                response.put("message", "Invalid signature");
                return response;
            }
            
            // Xử lý webhook data theo PayOS format
            if (payload.containsKey("data")) {
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) payload.get("data");
                
                // Lấy thông tin đơn hàng
                Integer orderCode = (Integer) data.get("orderCode");
                String status = (String) data.get("status");
                
                System.out.println("PayOS Webhook - Order Code: " + orderCode + ", Status: " + status);
                
                if (orderCode != null) {
                    // Tìm đơn hàng theo orderCode
                    Order order = orderService.findById(orderCode.longValue()).orElse(null);
                    
                    if (order != null) {
                        // Cập nhật trạng thái đơn hàng theo PayOS status
                        if ("PAID".equals(status)) {
                            // Thanh toán thành công
                            order.setPaymentPaid(true);
                            order.setStatus(Order.OrderStatus.CONFIRMED);
                            orderService.updateOrder(order);
                            
                            System.out.println("Order " + orderCode + " marked as PAID");
                        } else if ("CANCELLED".equals(status)) {
                            // Thanh toán bị hủy
                            order.setPaymentPaid(false);
                            order.setStatus(Order.OrderStatus.CANCELLED);
                            orderService.updateOrder(order);
                            
                            System.out.println("Order " + orderCode + " marked as CANCELLED");
                        }
                    } else {
                        System.out.println("Order not found: " + orderCode);
                    }
                }
            }
            
            response.put("success", true);
            response.put("message", "Webhook processed successfully");
            
        } catch (Exception e) {
            System.out.println("PayOS Webhook error: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Webhook error: " + e.getMessage());
        }
        
        return response;
    }
    
    /**
     * Xác thực webhook signature theo PayOS docs
     */
    private boolean verifyWebhookSignature(Map<String, Object> payload, String signature) {
        try {
            if (signature == null || signature.isEmpty()) {
                return false;
            }
            
            // Tạo data string để verify signature
            // PayOS webhook signature format: HMAC_SHA256(data, checksumKey)
            String dataString = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(payload);
            
            // Tạo HMAC_SHA256 signature
            javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
            javax.crypto.spec.SecretKeySpec secretKeySpec = new javax.crypto.spec.SecretKeySpec(
                payosChecksumKey.getBytes("UTF-8"), "HmacSHA256");
            mac.init(secretKeySpec);
            
            byte[] signatureBytes = mac.doFinal(dataString.getBytes("UTF-8"));
            String expectedSignature = bytesToHex(signatureBytes);
            
            System.out.println("Expected signature: " + expectedSignature);
            System.out.println("Received signature: " + signature);
            
            return expectedSignature.equals(signature);
            
        } catch (Exception e) {
            System.out.println("Error verifying webhook signature: " + e.getMessage());
            return false;
        }
    }

    /**
     * Tạo link thanh toán PayOS
     */
    private String createPayOSPaymentLink(Order order) {
        try {
            // Tạo payment data theo PayOS API
            Map<String, Object> paymentData = new HashMap<>();
            paymentData.put("orderCode", order.getOrderId());
            paymentData.put("amount", (int) order.getFinalAmount().doubleValue()); // PayOS expects amount in VND (final amount after discounts)
            
            // PayOS chỉ cho phép tối đa 25 ký tự cho description
            String description = "Đơn hàng #" + order.getOrderId();
            if (description.length() > 25) {
                description = description.substring(0, 25);
            }
            paymentData.put("description", description);
            paymentData.put("returnUrl", payosReturnUrl);
            paymentData.put("cancelUrl", payosCancelUrl);
            
            System.out.println("PayOS Payment Data: " + paymentData);
            
            // Gọi PayOS API thực tế để tạo payment link
            String paymentUrl = callPayOSAPI(paymentData);
            
            if (paymentUrl != null && !paymentUrl.isEmpty()) {
                System.out.println("PayOS Payment URL: " + paymentUrl);
                return paymentUrl;
            } else {
                // PayOS API failed - redirect to checkout with error
                System.out.println("PayOS API failed");
                return "redirect:/checkout?error=Không thể tạo link thanh toán PayOS";
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("PayOS error: " + e.getMessage());
            return "redirect:/checkout?error=Lỗi tạo thanh toán PayOS: " + e.getMessage();
        }
    }
    
    /**
     * Gọi PayOS API để tạo payment link theo đúng tài liệu PayOS
     */
    private String callPayOSAPI(Map<String, Object> paymentData) {
        try {
            // PayOS API endpoint theo docs
            String apiUrl = "https://api-merchant.payos.vn/v2/payment-requests";
            
            // Tạo signature theo PayOS docs
            String signature = createPayOSSignature(paymentData);
            paymentData.put("signature", signature);
            
            // Tạo HTTP request
            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
            
            // Tạo JSON payload
            String jsonPayload = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(paymentData);
            
            System.out.println("PayOS Request Payload: " + jsonPayload);
            
            // Tạo request với headers theo PayOS docs
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                .uri(java.net.URI.create(apiUrl))
                .header("Content-Type", "application/json")
                .header("x-client-id", payosClientId)
                .header("x-api-key", payosApiKey)
                .POST(java.net.http.HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();
            
            // Gửi request
            java.net.http.HttpResponse<String> response = client.send(request, 
                java.net.http.HttpResponse.BodyHandlers.ofString());
            
            System.out.println("PayOS API Response Status: " + response.statusCode());
            System.out.println("PayOS API Response Body: " + response.body());
            
            if (response.statusCode() == 200) {
                // Parse response theo PayOS API format
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                @SuppressWarnings("unchecked")
                Map<String, Object> responseData = mapper.readValue(response.body(), Map.class);
                
                // PayOS response format: {"code": "00", "desc": "Success", "data": {...}}
                if ("00".equals(responseData.get("code"))) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> data = (Map<String, Object>) responseData.get("data");
                    if (data != null && data.containsKey("checkoutUrl")) {
                        return (String) data.get("checkoutUrl");
                    }
                } else {
                    System.out.println("PayOS API Error: " + responseData.get("desc"));
                }
            } else {
                System.out.println("PayOS API HTTP Error: " + response.statusCode());
            }
            
            return null;
            
        } catch (Exception e) {
            System.out.println("PayOS API call failed: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Tạo signature theo PayOS docs với HMAC_SHA256
     */
    private String createPayOSSignature(Map<String, Object> paymentData) {
        try {
            // Tạo data string theo format: amount=$amount&cancelUrl=$cancelUrl&description=$description&orderCode=$orderCode&returnUrl=$returnUrl
            StringBuilder dataString = new StringBuilder();
            dataString.append("amount=").append(paymentData.get("amount"));
            dataString.append("&cancelUrl=").append(paymentData.get("cancelUrl"));
            dataString.append("&description=").append(paymentData.get("description"));
            dataString.append("&orderCode=").append(paymentData.get("orderCode"));
            dataString.append("&returnUrl=").append(paymentData.get("returnUrl"));
            
            System.out.println("PayOS Signature Data: " + dataString.toString());
            
            // Tạo HMAC_SHA256 signature
            javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
            javax.crypto.spec.SecretKeySpec secretKeySpec = new javax.crypto.spec.SecretKeySpec(
                payosChecksumKey.getBytes("UTF-8"), "HmacSHA256");
            mac.init(secretKeySpec);
            
            byte[] signatureBytes = mac.doFinal(dataString.toString().getBytes("UTF-8"));
            String signature = bytesToHex(signatureBytes);
            
            System.out.println("PayOS Generated Signature: " + signature);
            
            return signature;
            
        } catch (Exception e) {
            System.out.println("Error creating PayOS signature: " + e.getMessage());
            e.printStackTrace();
            return "";
        }
    }
    
    /**
     * Convert bytes to hex string
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
}
