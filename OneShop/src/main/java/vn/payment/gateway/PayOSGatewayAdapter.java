package vn.payment.gateway;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import vn.dto.PayOSPaymentRequestDTO;
import vn.entity.Order;
import vn.service.PayOSPaymentService;

import java.util.Map;

/**
 * Adapter cho PayOS Payment Gateway (Adapter Pattern).
 * 
 * Cấu trúc theo Adapter Pattern:
 * - Client Interface: PaymentGatewayPort
 * - Adapter: PayOSGatewayAdapter (class này)
 * - Adaptee/Service: PayOSPaymentService
 * 
 * Adapter chuyển đổi interface PaymentGatewayPort sang các method của PayOSPaymentService.
 */
@Component
public class PayOSGatewayAdapter implements PaymentGatewayPort {

    // Adaptee - Service được wrap bởi Adapter
    private final PayOSPaymentService adaptee;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public PayOSGatewayAdapter(PayOSPaymentService payOSPaymentService) {
        this.adaptee = payOSPaymentService;
    }

    @Override
    public String createPaymentUrl(Order order, String returnUrl, String notifyUrl) {
        try {
            // Chuyển đổi dữ liệu từ Order sang format PayOS (convertToServiceFormat)
            PayOSPaymentRequestDTO requestDto = convertToServiceFormat(order, returnUrl, notifyUrl);
            
            // Gọi method của Adaptee (PayOSPaymentService)
            return adaptee.createPaymentRequest(requestDto);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi tạo thanh toán PayOS: " + e.getMessage(), e);
        }
    }

    /**
     * Chuyển đổi Order sang format dữ liệu của PayOS (convertToServiceFormat trong Adapter Pattern)
     */
    private PayOSPaymentRequestDTO convertToServiceFormat(Order order, String returnUrl, String notifyUrl) {
        // Xử lý tạo orderCode là duy nhất: orderId * 10000 + random(0-9999)
        long randomPart = (long) (Math.random() * 10000L);
        long uniqueOrderCode = order.getOrderId() * 10000L + randomPart;
        
        Double paymentAmount = (order.getFinalAmount() != null && order.getFinalAmount() > 0) 
                ? order.getFinalAmount() 
                : order.getTotalAmount();
        int amount = (int) Math.round(paymentAmount);

        String description = "Đơn hàng #" + order.getOrderId();
        if (description.length() > 25) {
            description = description.substring(0, 25);
        }
        
        String finalReturnUrl = returnUrl + (returnUrl.contains("?") ? "&" : "?") + "orderId=" + order.getOrderId();
        
        String cancelUrl = notifyUrl != null && !notifyUrl.isEmpty() ? notifyUrl : returnUrl.replace("/return", "/cancel");
        cancelUrl = cancelUrl + (cancelUrl.contains("?") ? "&" : "?") + "orderId=" + order.getOrderId();

        return new PayOSPaymentRequestDTO(uniqueOrderCode, amount, description, finalReturnUrl, cancelUrl);
    }

    @Override
    public PaymentCallbackResult processCallback(HttpServletRequest request) {
        CallbackData callbackData = convertCallbackToServiceFormat(request);

        Long originalOrderId = parseOrderId(callbackData.orderId(), callbackData.orderCode());
        if (originalOrderId == null) {
            return new PaymentCallbackResult(false, false, null, null, 0D, "Order ID format invalid");
        }

        boolean isCancel = "true".equals(callbackData.cancel()) || "CANCELLED".equals(callbackData.status());
        boolean isSuccess = adaptee.processPaymentCallback(originalOrderId, normalizeResultCode(callbackData), null, 0D);

        return new PaymentCallbackResult(isSuccess, isCancel, originalOrderId, null, 0D, "PayOS return " + callbackData.status());
    }

    @Override
    @SuppressWarnings("unchecked")
    public PaymentWebhookResult processWebhook(HttpServletRequest request, String payload) {
        String signature = request.getHeader("x-payos-signature");
        try {
            Map<String, Object> payloadMap = convertWebhookToServiceFormat(payload);
            
            // Gọi method verifyWebhookSignature của Adaptee
            if (!adaptee.verifyWebhookSignature(payloadMap, signature)) {
                return new PaymentWebhookResult(false, false, "ERROR", null, "Invalid webhook signature");
            }

            if (payloadMap.containsKey("data")) {
                Map<String, Object> data = (Map<String, Object>) payloadMap.get("data");
                Integer orderCode = (Integer) data.get("orderCode");
                String status = (String) data.get("status");
                
                if (orderCode != null) {
                    long originalOrderId = orderCode.longValue() / 10000L;
                    boolean isSuccess = adaptee.processPaymentCallback(originalOrderId, status, null, 0D);
                    return new PaymentWebhookResult(isSuccess, true, status, originalOrderId, "Webhook from PayOS");
                }
            }
            return new PaymentWebhookResult(false, true, "UNKNOWN", null, "No data present");

        } catch (Exception e) {
            return new PaymentWebhookResult(false, false, "ERROR", null, e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> convertWebhookToServiceFormat(String payload) throws Exception {
        return objectMapper.readValue(payload, Map.class);
    }

    private CallbackData convertCallbackToServiceFormat(HttpServletRequest request) {
        return new CallbackData(
                request.getParameter("status"),
                request.getParameter("orderId"),
                request.getParameter("orderCode"),
                request.getParameter("code"),
                request.getParameter("cancel")
        );
    }

    private Long parseOrderId(String orderId, String orderCode) {
        try {
            if (orderId != null && !orderId.isEmpty()) {
                return Long.parseLong(orderId);
            }
            if (orderCode != null && !orderCode.isEmpty()) {
                return Long.parseLong(orderCode) / 10000L;
            }
            return null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String normalizeResultCode(CallbackData callbackData) {
        if (callbackData.code() != null && !callbackData.code().isEmpty()) {
            return callbackData.code();
        }
        return callbackData.status();
    }

    private record CallbackData(String status, String orderId, String orderCode, String code, String cancel) {
    }
}
