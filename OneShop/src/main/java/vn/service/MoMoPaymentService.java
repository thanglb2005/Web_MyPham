package vn.service;

import vn.entity.Order;

public interface MoMoPaymentService {
    
    /**
     * Tạo payment request với MoMo
     * @param order Đơn hàng cần thanh toán
     * @param returnUrl URL trả về sau khi thanh toán thành công
     * @param notifyUrl URL callback từ MoMo
     * @return Payment URL từ MoMo
     */
    String createPaymentRequest(Order order, String returnUrl, String notifyUrl);
    
    /**
     * Xử lý callback từ MoMo
     * @param orderId ID đơn hàng
     * @param resultCode Mã kết quả từ MoMo
     * @param transId Transaction ID từ MoMo
     * @param amount Số tiền thanh toán
     * @return true nếu thanh toán thành công
     */
    boolean processPaymentCallback(Long orderId, String resultCode, String transId, Double amount);
    
    /**
     * Kiểm tra trạng thái thanh toán
     * @param orderId ID đơn hàng
     * @return true nếu thanh toán thành công
     */
    boolean checkPaymentStatus(Long orderId);
}
