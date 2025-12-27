package vn.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * FlashSaleProduct entity for managing products in flash sale
 * @author OneShop Team
 */
@Entity
@Table(name = "flash_sale_products")
@EntityListeners(AuditingEntityListener.class)
public class FlashSaleProduct {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "flash_sale_product_id")
    private Long flashSaleProductId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flash_sale_id", nullable = false)
    @JsonIgnoreProperties({"products", "createdBy", "hibernateLazyInitializer", "handler"})
    private FlashSale flashSale;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnoreProperties({"shop", "category", "brand", "hibernateLazyInitializer", "handler"})
    private Product product;
    
    @Column(name = "flash_sale_price", nullable = false, precision = 18, scale = 2)
    private BigDecimal flashSalePrice;
    
    @Column(name = "original_price", nullable = false, precision = 18, scale = 2)
    private BigDecimal originalPrice;
    
    @Column(name = "quantity_limit", nullable = false)
    private Integer quantityLimit;
    
    @Column(name = "sold_quantity", nullable = false)
    private Integer soldQuantity = 0;
    
    @Column(name = "max_quantity_per_user", nullable = false)
    private Integer maxQuantityPerUser = 1;
    
    @Column(name = "display_order")
    private Integer displayOrder = 0;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    // Constructors
    public FlashSaleProduct() {}
    
    public FlashSaleProduct(FlashSale flashSale, Product product, BigDecimal flashSalePrice, 
                          BigDecimal originalPrice, Integer quantityLimit, Integer maxQuantityPerUser) {
        this.flashSale = flashSale;
        this.product = product;
        this.flashSalePrice = flashSalePrice;
        this.originalPrice = originalPrice;
        this.quantityLimit = quantityLimit;
        this.maxQuantityPerUser = maxQuantityPerUser;
    }
    
    // Business methods
    /**
     * Get remaining quantity available for flash sale
     */
    public int getRemainingQuantity() {
        return Math.max(0, quantityLimit - soldQuantity);
    }
    
    /**
     * Check if product is available in flash sale
     */
    public boolean isAvailable() {
        return isActive && getRemainingQuantity() > 0 && flashSale != null && flashSale.isActive();
    }
    
    /**
     * Calculate discount percentage
     */
    public double getDiscountPercentage() {
        if (originalPrice == null || originalPrice.compareTo(BigDecimal.ZERO) == 0) {
            return 0.0;
        }
        BigDecimal discount = originalPrice.subtract(flashSalePrice);
        return discount.divide(originalPrice, 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
    }
    
    /**
     * Check if user can purchase specified quantity
     */
    public boolean canPurchase(int quantity) {
        return isAvailable() && quantity > 0 && quantity <= getRemainingQuantity();
    }
    
    // Getters and Setters
    public Long getFlashSaleProductId() {
        return flashSaleProductId;
    }
    
    public void setFlashSaleProductId(Long flashSaleProductId) {
        this.flashSaleProductId = flashSaleProductId;
    }
    
    public FlashSale getFlashSale() {
        return flashSale;
    }
    
    public void setFlashSale(FlashSale flashSale) {
        this.flashSale = flashSale;
    }
    
    public Product getProduct() {
        return product;
    }
    
    public void setProduct(Product product) {
        this.product = product;
    }
    
    public BigDecimal getFlashSalePrice() {
        return flashSalePrice;
    }
    
    public void setFlashSalePrice(BigDecimal flashSalePrice) {
        this.flashSalePrice = flashSalePrice;
    }
    
    public BigDecimal getOriginalPrice() {
        return originalPrice;
    }
    
    public void setOriginalPrice(BigDecimal originalPrice) {
        this.originalPrice = originalPrice;
    }
    
    public Integer getQuantityLimit() {
        return quantityLimit;
    }
    
    public void setQuantityLimit(Integer quantityLimit) {
        this.quantityLimit = quantityLimit;
    }
    
    public Integer getSoldQuantity() {
        return soldQuantity;
    }
    
    public void setSoldQuantity(Integer soldQuantity) {
        this.soldQuantity = soldQuantity;
    }
    
    public Integer getMaxQuantityPerUser() {
        return maxQuantityPerUser;
    }
    
    public void setMaxQuantityPerUser(Integer maxQuantityPerUser) {
        this.maxQuantityPerUser = maxQuantityPerUser;
    }
    
    public Integer getDisplayOrder() {
        return displayOrder;
    }
    
    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (soldQuantity == null) {
            soldQuantity = 0;
        }
        if (maxQuantityPerUser == null) {
            maxQuantityPerUser = 1;
        }
        if (displayOrder == null) {
            displayOrder = 0;
        }
        if (isActive == null) {
            isActive = true;
        }
    }
    
    @Override
    public String toString() {
        return "FlashSaleProduct{" +
                "flashSaleProductId=" + flashSaleProductId +
                ", flashSalePrice=" + flashSalePrice +
                ", originalPrice=" + originalPrice +
                ", quantityLimit=" + quantityLimit +
                ", soldQuantity=" + soldQuantity +
                '}';
    }
}

