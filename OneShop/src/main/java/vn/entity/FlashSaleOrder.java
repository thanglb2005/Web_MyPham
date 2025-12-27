package vn.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * FlashSaleOrder entity for tracking flash sale purchases
 * @author OneShop Team
 */
@Entity
@Table(name = "flash_sale_orders")
@EntityListeners(AuditingEntityListener.class)
public class FlashSaleOrder {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "flash_sale_order_id")
    private Long flashSaleOrderId;
    
    @Column(name = "flash_sale_id", nullable = false)
    private Long flashSaleId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonIgnoreProperties({"user", "shop", "orderDetails", "hibernateLazyInitializer", "handler"})
    private Order order;
    
    @Column(name = "product_id", nullable = false)
    private Long productId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"password", "roles", "hibernateLazyInitializer", "handler"})
    private User user;
    
    @Column(name = "quantity", nullable = false)
    private Integer quantity;
    
    @Column(name = "flash_sale_price", nullable = false, precision = 18, scale = 2)
    private BigDecimal flashSalePrice;
    
    @Column(name = "total_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalAmount;
    
    @CreatedDate
    @Column(name = "purchased_at", nullable = false, updatable = false)
    private LocalDateTime purchasedAt;
    
    // Constructors
    public FlashSaleOrder() {}
    
    public FlashSaleOrder(Long flashSaleId, Order order, Long productId, User user, 
                         Integer quantity, BigDecimal flashSalePrice) {
        this.flashSaleId = flashSaleId;
        this.order = order;
        this.productId = productId;
        this.user = user;
        this.quantity = quantity;
        this.flashSalePrice = flashSalePrice;
        this.totalAmount = flashSalePrice.multiply(BigDecimal.valueOf(quantity));
    }
    
    // Getters and Setters
    public Long getFlashSaleOrderId() {
        return flashSaleOrderId;
    }
    
    public void setFlashSaleOrderId(Long flashSaleOrderId) {
        this.flashSaleOrderId = flashSaleOrderId;
    }
    
    public Long getFlashSaleId() {
        return flashSaleId;
    }
    
    public void setFlashSaleId(Long flashSaleId) {
        this.flashSaleId = flashSaleId;
    }
    
    public Order getOrder() {
        return order;
    }
    
    public void setOrder(Order order) {
        this.order = order;
    }
    
    public Long getProductId() {
        return productId;
    }
    
    public void setProductId(Long productId) {
        this.productId = productId;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public Integer getQuantity() {
        return quantity;
    }
    
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
    
    public BigDecimal getFlashSalePrice() {
        return flashSalePrice;
    }
    
    public void setFlashSalePrice(BigDecimal flashSalePrice) {
        this.flashSalePrice = flashSalePrice;
    }
    
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
    
    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
    
    public LocalDateTime getPurchasedAt() {
        return purchasedAt;
    }
    
    public void setPurchasedAt(LocalDateTime purchasedAt) {
        this.purchasedAt = purchasedAt;
    }
    
    @PrePersist
    protected void onCreate() {
        if (purchasedAt == null) {
            purchasedAt = LocalDateTime.now();
        }
        if (totalAmount == null && flashSalePrice != null && quantity != null) {
            totalAmount = flashSalePrice.multiply(BigDecimal.valueOf(quantity));
        }
    }
    
    @Override
    public String toString() {
        return "FlashSaleOrder{" +
                "flashSaleOrderId=" + flashSaleOrderId +
                ", flashSaleId=" + flashSaleId +
                ", productId=" + productId +
                ", quantity=" + quantity +
                ", totalAmount=" + totalAmount +
                '}';
    }
}

