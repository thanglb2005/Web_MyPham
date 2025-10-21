package vn.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import vn.entity.Order;
import vn.service.MoMoPaymentService;
import vn.service.OrderService;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class MoMoPaymentServiceImpl implements MoMoPaymentService {

    @Autowired
    private OrderService orderService;

    @Value("${momo.partner.code}")
    private String partnerCode;

    @Value("${momo.access.key}")
    private String accessKey;

    @Value("${momo.secret.key}")
    private String secretKey;

    @Value("${momo.api.endpoint}")
    private String apiEndpoint;

    @Value("${momo.return.url}")
    private String returnUrl;

    @Value("${momo.notify.url}")
    private String notifyUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public String createPaymentRequest(Order order, String returnUrl, String notifyUrl) {
        try {
            // Tạo requestId unique hơn
            String requestId = "REQ_" + order.getOrderId() + "_" + System.currentTimeMillis();
            String orderId = order.getOrderId().toString();
            String orderInfo = "Thanh toan don hang #" + orderId;
            
            // Debug logging
            System.out.println("=== MoMo Payment Debug ===");
            System.out.println("Order ID: " + orderId);
            System.out.println("Request ID: " + requestId);
            System.out.println("Order Total Amount (VND): " + order.getTotalAmount());
            System.out.println("Order Final Amount (VND): " + order.getFinalAmount());
            
            // Use finalAmount (after discount) instead of totalAmount
            Double paymentAmount = (order.getFinalAmount() != null && order.getFinalAmount() > 0) 
                ? order.getFinalAmount() 
                : order.getTotalAmount();
            String amount = String.valueOf((long) Math.round(paymentAmount)); // Gửi trực tiếp bằng VND
            System.out.println("MoMo Amount (VND): " + amount);
            System.out.println("Partner Code: " + partnerCode);
            System.out.println("Access Key: " + accessKey);
            System.out.println("========================");
            
            String extraData = "";

            // Tạo raw data để ký
            String rawData = "accessKey=" + accessKey +
                    "&amount=" + amount +
                    "&extraData=" + extraData +
                    "&ipnUrl=" + notifyUrl +
                    "&orderId=" + orderId +
                    "&orderInfo=" + orderInfo +
                    "&partnerCode=" + partnerCode +
                    "&redirectUrl=" + returnUrl +
                    "&requestId=" + requestId +
                    "&requestType=captureWallet";

            // Debug raw data và signature
            System.out.println("Raw Data for Signature: " + rawData);
            
            // Tạo chữ ký
            String signature = createSignature(rawData, secretKey);
            System.out.println("Generated Signature: " + signature);

            // Tạo request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("partnerCode", partnerCode);
            requestBody.put("accessKey", accessKey);
            requestBody.put("requestId", requestId);
            requestBody.put("amount", amount);
            requestBody.put("orderId", orderId);
            requestBody.put("orderInfo", orderInfo);
            requestBody.put("redirectUrl", returnUrl);
            requestBody.put("ipnUrl", notifyUrl);
            requestBody.put("extraData", extraData);
            requestBody.put("requestType", "captureWallet");
            requestBody.put("signature", signature);

            // Gửi request đến MoMo API
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            @SuppressWarnings("unchecked")
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    apiEndpoint,
                    HttpMethod.POST,
                    entity,
                    (Class<Map<String, Object>>) (Class<?>) Map.class
            );

            System.out.println("MoMo Response Status: " + response.getStatusCode());
            System.out.println("MoMo Response Body: " + response.getBody());
            
            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> responseBody = response.getBody();
                if (responseBody != null && responseBody.containsKey("payUrl")) {
                    // Lưu request ID vào order
                    order.setMomoRequestId(requestId);
                    orderService.updateOrder(order);
                    
                    System.out.println("Payment URL created successfully: " + responseBody.get("payUrl"));
                    return (String) responseBody.get("payUrl");
                } else {
                    System.out.println("MoMo response missing payUrl: " + responseBody);
                }
            }

            throw new RuntimeException("Không thể tạo payment request với MoMo. Response: " + response.getBody());

        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi tạo payment request: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean processPaymentCallback(Long orderId, String resultCode, String transId, Double amount) {
        try {
            Order order = orderService.getOrderById(orderId);
            if (order == null) {
                return false;
            }

            // Validate amount matches finalAmount
            Double expectedAmount = (order.getFinalAmount() != null && order.getFinalAmount() > 0) 
                ? order.getFinalAmount() 
                : order.getTotalAmount();
            
            System.out.println("=== MoMo Callback Validation ===");
            System.out.println("Expected Amount: " + expectedAmount);
            System.out.println("Received Amount: " + amount);
            
            if (amount != null && Math.abs(expectedAmount - amount) > 1.0) {
                System.out.println("Amount mismatch! Payment rejected.");
                return false;
            }

            // Kiểm tra mã kết quả từ MoMo
            if ("0".equals(resultCode)) {
                // Thanh toán thành công
                order.setPaymentPaid(true);
                order.setPaymentDate(LocalDateTime.now());
                order.setStatus(Order.OrderStatus.CONFIRMED);
                order.setMomoTransactionId(transId);
                order.setNote(order.getNote() + " | MoMo Transaction ID: " + transId);
                
                orderService.updateOrder(order);
                System.out.println("Payment successful for order #" + orderId);
                return true;
            } else {
                // Thanh toán thất bại
                order.setStatus(Order.OrderStatus.CANCELLED);
                order.setNote(order.getNote() + " | MoMo Payment Failed - Code: " + resultCode);
                
                orderService.updateOrder(order);
                return false;
            }

        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi xử lý payment callback: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean checkPaymentStatus(Long orderId) {
        try {
            Order order = orderService.getOrderById(orderId);
            return order != null && order.getPaymentPaid();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Tạo chữ ký HMAC SHA256
     */
    private String createSignature(String rawData, String secretKey) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] signature = mac.doFinal(rawData.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(signature);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Lỗi khi tạo chữ ký: " + e.getMessage(), e);
        }
    }

    /**
     * Chuyển đổi byte array thành hex string
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
}
