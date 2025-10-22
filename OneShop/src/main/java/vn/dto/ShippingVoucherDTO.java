package vn.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShippingVoucherDTO {
    
    private Long promotionId;
    
    // Mã voucher
    private String promotionCode;
    
    // Tên voucher
    private String promotionName;
    
    // Mô tả
    private String description;
    
    // Loại giảm giá (PERCENTAGE, FIXED_AMOUNT, FREE_SHIPPING)
    private String promotionType;
    
    // Giá trị giảm (% hoặc số tiền)
    private BigDecimal discountValue;
    
    // Đơn hàng tối thiểu
    private BigDecimal minimumOrderAmount;
    
    // Giảm tối đa
    private BigDecimal maximumDiscountAmount;
    
    // Số lượng còn lại
    private Integer remainingUsage;
    
    // Số tiền giảm tối đa cho đơn này (đã tính toán)
    private BigDecimal calculatedDiscount;
}

