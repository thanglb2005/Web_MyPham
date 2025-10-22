package vn.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.entity.ShippingProvider;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeliveryOptionsDTO {
    
    // Có thể giao hỏa tốc không (cùng tỉnh + có shipper)
    private Boolean expressAvailable;
    
    // Phí giao hỏa tốc
    private Double expressFee;
    
    // Thời gian giao hỏa tốc
    private String expressTime;
    
    // Phí giao thường (cùng tỉnh)
    private Double standardFee;
    
    // Thời gian giao thường
    private String standardTime;
    
    // Danh sách đơn vị vận chuyển (nếu khác tỉnh)
    private List<ShippingProvider> shippingProviders;
    
    // Có cùng tỉnh không
    private Boolean sameCity;
    
    // Shop city
    private String shopCity;
    
    // Danh sách voucher giảm ship
    private List<ShippingVoucherDTO> shippingVouchers;
}

