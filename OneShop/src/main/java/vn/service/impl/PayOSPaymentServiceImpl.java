package vn.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import vn.dto.PayOSPaymentRequestDTO;
import vn.service.PayOSPaymentService;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation của PayOSPaymentService (Service/Adaptee trong Adapter Pattern).
 * Chứa logic gọi PayOS API trực tiếp với các phương thức đặc thù của PayOS.
 */
@Service
public class PayOSPaymentServiceImpl implements PayOSPaymentService {

    @Value("${payos.client-id:}")
    private String payosClientId;

    @Value("${payos.api-key:}")
    private String payosApiKey;

    @Value("${payos.checksum-key:}")
    private String payosChecksumKey;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String PAYOS_API_URL = "https://api-merchant.payos.vn/v2/payment-requests";

    @Override
    public String createPaymentRequest(PayOSPaymentRequestDTO requestDto) {
        try {
            Map<String, Object> paymentData = convertToPayOSPayload(requestDto);
            String signature = createSignature(requestDto);
            paymentData.put("signature", signature);

            HttpClient client = HttpClient.newHttpClient();
            String jsonPayload = objectMapper.writeValueAsString(paymentData);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(PAYOS_API_URL))
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
        } catch (Exception e) {
            throw new RuntimeException("Lỗi gọi PayOS API: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean processPaymentCallback(Long orderId, String resultCode, String transId, Double amount) {
        if (orderId == null || resultCode == null) {
            return false;
        }
        return "00".equals(resultCode) || "PAID".equalsIgnoreCase(resultCode) || "success".equalsIgnoreCase(resultCode);
    }

    @Override
    public String createSignature(PayOSPaymentRequestDTO requestDto) {
        try {
            StringBuilder dataString = new StringBuilder();
            dataString.append("amount=").append(requestDto.getAmount());
            dataString.append("&cancelUrl=").append(requestDto.getCancelUrl());
            dataString.append("&description=").append(requestDto.getDescription());
            dataString.append("&orderCode=").append(requestDto.getOrderCode());
            dataString.append("&returnUrl=").append(requestDto.getReturnUrl());

            return signWithHmacSHA256(dataString.toString(), payosChecksumKey);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi tạo chữ ký PayOS: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean verifyWebhookSignature(Map<String, Object> payload, String signature) {
        try {
            if (signature == null || signature.isEmpty()) return false;
            String dataString = objectMapper.writeValueAsString(payload);
            String expectedSignature = signWithHmacSHA256(dataString, payosChecksumKey);
            return expectedSignature.equals(signature);
        } catch (Exception e) {
            return false;
        }
    }

    private Map<String, Object> convertToPayOSPayload(PayOSPaymentRequestDTO requestDto) {
        Map<String, Object> paymentData = new HashMap<>();
        paymentData.put("orderCode", requestDto.getOrderCode());
        paymentData.put("amount", requestDto.getAmount());
        paymentData.put("description", requestDto.getDescription());
        paymentData.put("returnUrl", requestDto.getReturnUrl());
        paymentData.put("cancelUrl", requestDto.getCancelUrl());
        return paymentData;
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
