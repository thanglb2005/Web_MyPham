package vn.payment.gateway;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import vn.entity.Order;
import vn.service.PayOSPaymentService;

import java.util.HashMap;
import java.util.Map;

/**
 * Adapter cho PayOS Payment Gateway (Adapter Pattern).
 * 
 * Cấu trúc theo Adapter Pattern:
 * - Client Interface: PaymentGatewayAdapter
 * - Adapter: PayOSGatewayAdapter (class này)
 * - Adaptee/Service: PayOSPaymentService
 * 
 * Adapter chuyển đổi interface PaymentGatewayAdapter sang các method của PayOSPaymentService.
 */
@Component
public class PayOSGatewayAdapter implements PaymentGatewayAdapter {

    // Adaptee - Service được wrap bởi Adapter
    private final PayOSPaymentService payOSPaymentService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public PayOSGatewayAdapter(PayOSPaymentService payOSPaymentService) {
        this.payOSPaymentService = payOSPaymentService;
    }

    @Override
    public String createPaymentUrl(Order order, String returnUrl, String notifyUrl) {
        try {
            // Chuyển đổi dữ liệu từ Order sang format PayOS (convertToServiceFormat)
            Map<String, Object> paymentData = convertOrderToPayOSFormat(order, returnUrl, notifyUrl);
            
            // Gọi method của Adaptee (PayOSPaymentService)
            return payOSPaymentService.callPayOSAPI(paymentData);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi tạo thanh toán PayOS: " + e.getMessage(), e);
        }
    }

    /**
     * Chuyển đổi Order sang format dữ liệu của PayOS (convertToServiceFormat trong Adapter Pattern)
     */
    private Map<String, Object> convertOrderToPayOSFormat(Order order, String returnUrl, String notifyUrl) {
        Map<String, Object> paymentData = new HashMap<>();
        
        // Xử lý tạo orderCode là duy nhất: orderId * 10000 + random(0-9999)
        long randomPart = (long) (Math.random() * 10000L);
        long uniqueOrderCode = order.getOrderId() * 10000L + randomPart;
        paymentData.put("orderCode", uniqueOrderCode);
        
        Double paymentAmount = (order.getFinalAmount() != null && order.getFinalAmount() > 0) 
                ? order.getFinalAmount() 
                : order.getTotalAmount();
        paymentData.put("amount", (int) Math.round(paymentAmount));

        String description = "Đơn hàng #" + order.getOrderId();
        if (description.length() > 25) {
            description = description.substring(0, 25);
        }
        paymentData.put("description", description);
        
        String finalReturnUrl = returnUrl + (returnUrl.contains("?") ? "&" : "?") + "orderId=" + order.getOrderId();
        paymentData.put("returnUrl", finalReturnUrl);
        
        String cancelUrl = notifyUrl != null && !notifyUrl.isEmpty() ? notifyUrl : returnUrl.replace("/return", "/cancel");
        cancelUrl = cancelUrl + (cancelUrl.contains("?") ? "&" : "?") + "orderId=" + order.getOrderId();
        paymentData.put("cancelUrl", cancelUrl);

        return paymentData;
    }

    @Override
    public PaymentCallbackResult processCallback(HttpServletRequest request) {
        String status = request.getParameter("status");
        String orderIdStr = request.getParameter("orderId");
        String orderCodeStr = request.getParameter("orderCode");
        String code = request.getParameter("code");
        String cancel = request.getParameter("cancel");

        Long originalOrderId = null;
        try {
            if (orderIdStr != null && !orderIdStr.isEmpty()) {
                originalOrderId = Long.parseLong(orderIdStr);
            } else if (orderCodeStr != null && !orderCodeStr.isEmpty()) {
                originalOrderId = Long.parseLong(orderCodeStr) / 10000L;
            }
        } catch (NumberFormatException e) {
            return new PaymentCallbackResult(false, false, null, null, 0D, "Order ID format invalid");
        }

        if (originalOrderId == null) {
            return new PaymentCallbackResult(false, false, null, null, 0D, "Thiếu Order ID trong callback");
        }

        boolean isCancel = "true".equals(cancel) || "CANCELLED".equals(status);
        boolean isSuccess = "success".equals(status) || "00".equals(code);

        return new PaymentCallbackResult(isSuccess, isCancel, originalOrderId, null, 0D, "PayOS return " + status);
    }

    @Override
    @SuppressWarnings("unchecked")
    public PaymentWebhookResult processWebhook(HttpServletRequest request, String payload) {
        String signature = request.getHeader("x-payos-signature");
        try {
            Map<String, Object> payloadMap = objectMapper.readValue(payload, Map.class);
            
            // Gọi method verifyWebhookSignature của Adaptee
            if (!payOSPaymentService.verifyWebhookSignature(payloadMap, signature)) {
                return new PaymentWebhookResult(false, false, "ERROR", null, "Invalid webhook signature");
            }

            if (payloadMap.containsKey("data")) {
                Map<String, Object> data = (Map<String, Object>) payloadMap.get("data");
                Integer orderCode = (Integer) data.get("orderCode");
                String status = (String) data.get("status");
                
                if (orderCode != null) {
                    long originalOrderId = orderCode.longValue() / 10000L;
                    boolean isSuccess = "PAID".equals(status);
                    return new PaymentWebhookResult(isSuccess, true, status, originalOrderId, "Webhook from PayOS");
                }
            }
            return new PaymentWebhookResult(false, true, "UNKNOWN", null, "No data present");

        } catch (Exception e) {
            return new PaymentWebhookResult(false, false, "ERROR", null, e.getMessage());
        }
    }
}
