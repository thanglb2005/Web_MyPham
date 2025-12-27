package vn.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * FlashSale entity for managing flash sale sessions
 * @author OneShop Team
 */
@Entity
@Table(name = "flash_sales")
@EntityListeners(AuditingEntityListener.class)
public class FlashSale {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "flash_sale_id")
    private Long flashSaleId;
    
    @Column(name = "sale_name", nullable = false, length = 200)
    private String saleName;
    
    @Column(name = "description", length = 1000)
    private String description;
    
    @Column(name = "start_time", nullable = false)
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime startTime;
    
    @Column(name = "end_time", nullable = false)
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime endTime;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private FlashSaleStatus status = FlashSaleStatus.DRAFT;
    
    @Column(name = "banner_image", length = 255)
    private String bannerImage;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    @JsonIgnoreProperties({"password", "roles", "hibernateLazyInitializer", "handler"})
    private User createdBy;
    
    @OneToMany(mappedBy = "flashSale", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"flashSale", "hibernateLazyInitializer", "handler"})
    private List<FlashSaleProduct> products = new ArrayList<>();
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Enum for Flash Sale Status
    public enum FlashSaleStatus {
        DRAFT,      // Nháp
        SCHEDULED,  // Đã lên lịch
        ACTIVE,     // Đang diễn ra
        ENDED,      // Đã kết thúc
        CANCELLED   // Đã hủy
    }
    
    // Constructors
    public FlashSale() {}
    
    public FlashSale(String saleName, String description, LocalDateTime startTime, 
                    LocalDateTime endTime, FlashSaleStatus status, User createdBy) {
        this.saleName = saleName;
        this.description = description;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.createdBy = createdBy;
    }
    
    // Business methods
    /**
     * Check if flash sale is currently active
     */
    public boolean isActive() {
        LocalDateTime now = LocalDateTime.now();
        return status == FlashSaleStatus.ACTIVE 
            && (now.isAfter(startTime) || now.isEqual(startTime))
            && now.isBefore(endTime);
    }
    
    /**
     * Check if flash sale has started
     */
    public boolean isStarted() {
        LocalDateTime now = LocalDateTime.now();
        return now.isAfter(startTime) || now.isEqual(startTime);
    }
    
    /**
     * Check if flash sale has ended
     */
    public boolean isEnded() {
        LocalDateTime now = LocalDateTime.now();
        return now.isAfter(endTime) || status == FlashSaleStatus.ENDED || status == FlashSaleStatus.CANCELLED;
    }
    
    /**
     * Get remaining seconds until flash sale ends
     */
    public long getRemainingSeconds() {
        if (!isActive()) return 0;
        return ChronoUnit.SECONDS.between(LocalDateTime.now(), endTime);
    }
    
    /**
     * Get remaining seconds until flash sale starts
     */
    public long getSecondsUntilStart() {
        if (isStarted()) return 0;
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(startTime)) {
            return ChronoUnit.SECONDS.between(now, startTime);
        }
        return 0;
    }
    
    // Getters and Setters
    public Long getFlashSaleId() {
        return flashSaleId;
    }
    
    public void setFlashSaleId(Long flashSaleId) {
        this.flashSaleId = flashSaleId;
    }
    
    public String getSaleName() {
        return saleName;
    }
    
    public void setSaleName(String saleName) {
        this.saleName = saleName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public LocalDateTime getStartTime() {
        return startTime;
    }
    
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }
    
    public LocalDateTime getEndTime() {
        return endTime;
    }
    
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
    
    public FlashSaleStatus getStatus() {
        return status;
    }
    
    public void setStatus(FlashSaleStatus status) {
        this.status = status;
    }
    
    public String getBannerImage() {
        return bannerImage;
    }
    
    public void setBannerImage(String bannerImage) {
        this.bannerImage = bannerImage;
    }
    
    public User getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }
    
    public List<FlashSaleProduct> getProducts() {
        return products;
    }
    
    public void setProducts(List<FlashSaleProduct> products) {
        this.products = products;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = createdAt;
        }
        if (status == null) {
            status = FlashSaleStatus.DRAFT;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    @Override
    public String toString() {
        return "FlashSale{" +
                "flashSaleId=" + flashSaleId +
                ", saleName='" + saleName + '\'' +
                ", status=" + status +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                '}';
    }
}

