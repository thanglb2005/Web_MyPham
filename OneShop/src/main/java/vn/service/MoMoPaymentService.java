package vn.service;

import vn.dto.MomoPaymentRequestDTO;

public interface MoMoPaymentService {
    
    /**
     * Tạo payment request với MoMo
     * @param requestDto Dữ liệu thanh toán đã được adapter chuyển đổi
     * @return Payment URL từ MoMo
     */
    String createPaymentRequest(MomoPaymentRequestDTO requestDto);
    
    /**
     * Xử lý callback từ MoMo
     * @param orderId ID đơn hàng
     * @param resultCode Mã kết quả từ MoMo
     * @param transId Transaction ID từ MoMo
     * @param amount Số tiền thanh toán
     * @return true nếu thanh toán thành công
     */
    boolean processPaymentCallback(Long orderId, String resultCode, String transId, Double amount);
    
}
