package vn.dto;

import vn.entity.Promotion;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class VendorPromotionForm {
    
    private String promotionName;
    private String description;
    private String promotionCode;
    private Promotion.PromotionType promotionType;
    private BigDecimal discountValue;
    private BigDecimal minimumOrderAmount;
    private BigDecimal maximumDiscountAmount;
    private Integer usageLimit;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean isActive;
    
    // Constructors
    public VendorPromotionForm() {}
    
    public VendorPromotionForm(String promotionName, String description, String promotionCode, 
                              Promotion.PromotionType promotionType, BigDecimal discountValue, 
                              BigDecimal minimumOrderAmount, BigDecimal maximumDiscountAmount, 
                              Integer usageLimit, LocalDateTime startDate, LocalDateTime endDate, 
                              Boolean isActive) {
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
        this.isActive = isActive;
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
    
    @Override
    public String toString() {
        return "VendorPromotionForm{" +
                "promotionName='" + promotionName + '\'' +
                ", promotionCode='" + promotionCode + '\'' +
                ", promotionType=" + promotionType +
                ", discountValue=" + discountValue +
                ", isActive=" + isActive +
                '}';
    }
}
