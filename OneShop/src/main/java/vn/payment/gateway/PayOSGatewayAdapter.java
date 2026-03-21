package vn.payment.gateway;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import vn.entity.Order;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

@Component
public class PayOSGatewayAdapter implements PaymentGatewayAdapter {

    @Value("${payos.client-id:}")
    private String payosClientId;

    @Value("${payos.api-key:}")
    private String payosApiKey;

    @Value("${payos.checksum-key:}")
    private String payosChecksumKey;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String createPaymentUrl(Order order, String returnUrl, String notifyUrl) {
        try {
            Map<String, Object> paymentData = new HashMap<>();
            
            // Xử lý tạo orderCode là duy nhất: orderId * 10000 + random(0-9999). 
            // Điều này vì PayOS bắt buộc mỗi lần tạo link là 1 orderCode khác nhau (cho dù user retry thanh toán order cũ)
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
            
            // Cố định Order ID thực trên Return URL để dễ dàng lấy lại bên callback
            String finalReturnUrl = returnUrl + (returnUrl.contains("?") ? "&" : "?") + "orderId=" + order.getOrderId();
            paymentData.put("returnUrl", finalReturnUrl);
            
            String cancelUrl = notifyUrl != null && !notifyUrl.isEmpty() ? notifyUrl : returnUrl.replace("/return", "/cancel");
            cancelUrl = cancelUrl + (cancelUrl.contains("?") ? "&" : "?") + "orderId=" + order.getOrderId();
            paymentData.put("cancelUrl", cancelUrl);

            return callPayOSAPI(paymentData);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi tạo thanh toán PayOS: " + e.getMessage(), e);
        }
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
            if (!verifyWebhookSignature(payloadMap, signature)) {
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

    private String callPayOSAPI(Map<String, Object> paymentData) throws Exception {
        String apiUrl = "https://api-merchant.payos.vn/v2/payment-requests";
        String signature = createPayOSSignature(paymentData);
        paymentData.put("signature", signature);

        HttpClient client = HttpClient.newHttpClient();
        String jsonPayload = objectMapper.writeValueAsString(paymentData);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Content-Type", "application/json")
                .header("x-client-id", payosClientId)
                .header("x-api-key", payosApiKey)
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            @SuppressWarnings("unchecked")
            Map<String, Object> responseData = objectMapper.readValue(response.body(), Map.class);
            if ("00".equals(responseData.get("code"))) {
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) responseData.get("data");
                if (data != null && data.containsKey("checkoutUrl")) {
                    return (String) data.get("checkoutUrl");
                }
            } else {
                 throw new RuntimeException("PayOS code not 00: " + responseData.get("desc"));
            }
        }
        throw new RuntimeException("Server error contacting PayOS. HTTP " + response.statusCode());
    }

    private String createPayOSSignature(Map<String, Object> paymentData) throws Exception {
        StringBuilder dataString = new StringBuilder();
        dataString.append("amount=").append(paymentData.get("amount"));
        dataString.append("&cancelUrl=").append(paymentData.get("cancelUrl"));
        dataString.append("&description=").append(paymentData.get("description"));
        dataString.append("&orderCode=").append(paymentData.get("orderCode"));
        dataString.append("&returnUrl=").append(paymentData.get("returnUrl"));

        return signWithHmacSHA256(dataString.toString(), payosChecksumKey);
    }

    private boolean verifyWebhookSignature(Map<String, Object> payload, String signature) {
        try {
            if (signature == null || signature.isEmpty()) return false;
            String dataString = objectMapper.writeValueAsString(payload);
            String expectedSignature = signWithHmacSHA256(dataString, payosChecksumKey);
            return expectedSignature.equals(signature);
        } catch (Exception e) {
            return false;
        }
    }

    private String signWithHmacSHA256(String data, String key) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "HmacSHA256");
        mac.init(secretKeySpec);
        byte[] signatureBytes = mac.doFinal(data.getBytes("UTF-8"));
        return bytesToHex(signatureBytes);
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
}
