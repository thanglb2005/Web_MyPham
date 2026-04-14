package vn.payment.gateway;

import jakarta.servlet.http.HttpServletRequest;
import vn.entity.Order;

public interface PaymentGatewayPort {
    /**
     * Tạo URL thanh toán chuyển hướng người dùng
     */
    String createPaymentUrl(Order order, String returnUrl, String notifyUrl);

    /**
     * Xử lý kết quả trả về từ trình duyệt của người dùng (Return URL)
     */
    PaymentCallbackResult processCallback(HttpServletRequest request);

    /**
     * Xử lý webhook/IPN từ server của cổng thanh toán
     */
    PaymentWebhookResult processWebhook(HttpServletRequest request, String payload);
}
