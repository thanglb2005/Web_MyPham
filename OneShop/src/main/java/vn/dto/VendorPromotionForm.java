package vn.dto;

import jakarta.validation.constraints.*;
import vn.entity.Promotion;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class VendorPromotionForm {
    
    @NotBlank(message = "Tên khuyến mãi không được để trống")
    @Size(max = 200, message = "Tên khuyến mãi không được quá 200 ký tự")
    private String promotionName;
    
    @Size(max = 1000, message = "Mô tả không được quá 1000 ký tự")
    private String description;
    
    @NotBlank(message = "Mã khuyến mãi không được để trống")
    @Size(max = 50, message = "Mã khuyến mãi không được quá 50 ký tự")
    @Pattern(regexp = "^[A-Z0-9_-]+$", message = "Mã khuyến mãi chỉ được chứa chữ hoa, số, gạch ngang và gạch dưới")
    private String promotionCode;
    
    @NotNull(message = "Loại khuyến mãi không được để trống")
    private Promotion.PromotionType promotionType;
    
    @NotNull(message = "Giá trị giảm giá không được để trống")
    @DecimalMin(value = "0.01", message = "Giá trị giảm giá phải lớn hơn 0")
    @DecimalMax(value = "999999999.99", message = "Giá trị giảm giá quá lớn")
    private BigDecimal discountValue;
    
    @NotNull(message = "Đơn hàng tối thiểu không được để trống")
    @DecimalMin(value = "0.00", message = "Đơn hàng tối thiểu không được âm")
    @DecimalMax(value = "999999999.99", message = "Đơn hàng tối thiểu quá lớn")
    private BigDecimal minimumOrderAmount;
    
    @NotNull(message = "Giảm giá tối đa không được để trống")
    @DecimalMin(value = "0.00", message = "Giảm giá tối đa không được âm")
    @DecimalMax(value = "999999999.99", message = "Giảm giá tối đa quá lớn")
    private BigDecimal maximumDiscountAmount;
    
    @NotNull(message = "Giới hạn sử dụng không được để trống")
    @Min(value = 1, message = "Giới hạn sử dụng phải lớn hơn 0")
    @Max(value = 999999, message = "Giới hạn sử dụng quá lớn")
    private Integer usageLimit;
    
    @NotNull(message = "Ngày bắt đầu không được để trống")
    @Future(message = "Ngày bắt đầu phải là ngày tương lai")
    private LocalDateTime startDate;
    
    @NotNull(message = "Ngày kết thúc không được để trống")
    @Future(message = "Ngày kết thúc phải là ngày tương lai")
    private LocalDateTime endDate;
    
    private Boolean isActive = true;
    
    // Constructors
    public VendorPromotionForm() {}
    
    public VendorPromotionForm(String promotionName, String description, String promotionCode,
                              Promotion.PromotionType promotionType, BigDecimal discountValue,
                              BigDecimal minimumOrderAmount, BigDecimal maximumDiscountAmount,
                              Integer usageLimit, LocalDateTime startDate, LocalDateTime endDate) {
        this.promotionName = promotionName;
        this.description = description;
        this.promotionCode = promotionCode;
        this.promotionType = promotionType;
        this.discountValue = discountValue;
        this.minimumOrderAmount = minimumOrderAmount;
        this.maximumDiscountAmount = maximumDiscountAmount;
        this.usageLimit = usageLimit;
        this.startDate = startDate;
        this.endDate = endDate;
    }
    
    // Getters and Setters
    public String getPromotionName() {
        return promotionName;
    }
    
    public void setPromotionName(String promotionName) {
        this.promotionName = promotionName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getPromotionCode() {
        return promotionCode;
    }
    
    public void setPromotionCode(String promotionCode) {
        this.promotionCode = promotionCode;
    }
    
    public Promotion.PromotionType getPromotionType() {
        return promotionType;
    }
    
    public void setPromotionType(Promotion.PromotionType promotionType) {
        this.promotionType = promotionType;
    }
    
    public BigDecimal getDiscountValue() {
        return discountValue;
    }
    
    public void setDiscountValue(BigDecimal discountValue) {
        this.discountValue = discountValue;
    }
    
    public BigDecimal getMinimumOrderAmount() {
        return minimumOrderAmount;
    }
    
    public void setMinimumOrderAmount(BigDecimal minimumOrderAmount) {
        this.minimumOrderAmount = minimumOrderAmount;
    }
    
    public BigDecimal getMaximumDiscountAmount() {
        return maximumDiscountAmount;
    }
    
    public void setMaximumDiscountAmount(BigDecimal maximumDiscountAmount) {
        this.maximumDiscountAmount = maximumDiscountAmount;
    }
    
    public Integer getUsageLimit() {
        return usageLimit;
    }
    
    public void setUsageLimit(Integer usageLimit) {
        this.usageLimit = usageLimit;
    }
    
    public LocalDateTime getStartDate() {
        return startDate;
    }
    
    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }
    
    public LocalDateTime getEndDate() {
        return endDate;
    }
    
    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    // Business validation methods
    public boolean isValidDateRange() {
        if (startDate == null || endDate == null) {
            return false;
        }
        return endDate.isAfter(startDate);
    }
    
    public boolean isValidDiscountAmounts() {
        if (discountValue == null || minimumOrderAmount == null || maximumDiscountAmount == null) {
            return false;
        }
        
        // For percentage discounts, discount value should be between 0 and 100
        if (promotionType == Promotion.PromotionType.PERCENTAGE) {
            return discountValue.compareTo(BigDecimal.ZERO) > 0 && 
                   discountValue.compareTo(BigDecimal.valueOf(100)) <= 0;
        }
        
        // For fixed amount discounts, discount value should be positive
        if (promotionType == Promotion.PromotionType.FIXED_AMOUNT) {
            return discountValue.compareTo(BigDecimal.ZERO) > 0;
        }
        
        return true;
    }
    
    @Override
    public String toString() {
        return "VendorPromotionForm{" +
                "promotionName='" + promotionName + '\'' +
                ", promotionCode='" + promotionCode + '\'' +
                ", promotionType=" + promotionType +
                ", discountValue=" + discountValue +
                ", minimumOrderAmount=" + minimumOrderAmount +
                ", maximumDiscountAmount=" + maximumDiscountAmount +
                ", usageLimit=" + usageLimit +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", isActive=" + isActive +
                '}';
    }
}