package vn.service;

import java.util.Map;

/**
 * Service interface cho PayOS Payment Gateway (Adaptee trong Adapter Pattern).
 * Định nghĩa các phương thức đặc thù của PayOS API.
 */
public interface PayOSPaymentService {

    /**
     * Gọi PayOS API để tạo link thanh toán
     * @param paymentData Dữ liệu thanh toán theo format PayOS
     * @return URL checkout từ PayOS
     */
    String callPayOSAPI(Map<String, Object> paymentData);

    /**
     * Tạo chữ ký cho request PayOS
     * @param paymentData Dữ liệu thanh toán
     * @return Chữ ký HMAC-SHA256
     */
    String createSignature(Map<String, Object> paymentData);

    /**
     * Xác thực chữ ký webhook từ PayOS
     * @param payload Dữ liệu webhook
     * @param signature Chữ ký từ header
     * @return true nếu chữ ký hợp lệ
     */
    boolean verifyWebhookSignature(Map<String, Object> payload, String signature);
}
