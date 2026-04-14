package vn.service;

import vn.dto.PayOSPaymentRequestDTO;

import java.util.Map;

/**
 * Service interface cho PayOS Payment Gateway (Adaptee trong Adapter Pattern).
 * Định nghĩa các phương thức đặc thù của PayOS API.
 */
public interface PayOSPaymentService {

    /**
     * Gọi PayOS API để tạo link thanh toán
     * @param requestDto Dữ liệu thanh toán theo format PayOS
     * @return URL checkout từ PayOS
     */
    String createPaymentRequest(PayOSPaymentRequestDTO requestDto);

    /**
     * Xử lý callback/webhook từ PayOS
     * @param orderId ID đơn hàng
     * @param resultCode Mã kết quả callback
     * @param transId Transaction ID (nếu có)
     * @param amount Số tiền (nếu có)
     * @return true nếu thanh toán thành công
     */
    boolean processPaymentCallback(Long orderId, String resultCode, String transId, Double amount);

    /**
     * Tạo chữ ký cho request PayOS
     * @param requestDto Dữ liệu thanh toán
     * @return Chữ ký HMAC-SHA256
     */
    String createSignature(PayOSPaymentRequestDTO requestDto);

    /**
     * Xác thực chữ ký webhook từ PayOS
     * @param payload Dữ liệu webhook
     * @param signature Chữ ký từ header
     * @return true nếu chữ ký hợp lệ
     */
    boolean verifyWebhookSignature(Map<String, Object> payload, String signature);
}
