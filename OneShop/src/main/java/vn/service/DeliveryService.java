package vn.service;

import vn.dto.DeliveryOptionsDTO;

public interface DeliveryService {
    
    /**
     * Kiểm tra các tùy chọn giao hàng có sẵn
     * 
     * @param customerCity Tỉnh/thành của khách hàng
     * @param shopId ID của shop
     * @return DeliveryOptionsDTO chứa thông tin các tùy chọn giao hàng
     */
    DeliveryOptionsDTO checkDeliveryOptions(String customerCity, Long shopId);
}

