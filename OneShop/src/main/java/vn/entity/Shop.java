package vn.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Shop entity mapping vendor owned storefronts.
 */
@Entity
@Table(name = "shops")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(exclude = {"shippers", "vendor"})
@ToString(exclude = {"shippers", "vendor"})
public class Shop implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "shop_id")
    private Long shopId;

    @Column(name = "shop_name", nullable = false, unique = true, columnDefinition = "NVARCHAR(255)")
    private String shopName;

    @Column(name = "shop_slug", unique = true)
    private String shopSlug;

    @Column(name = "shop_description", length = 2000, columnDefinition = "NVARCHAR(2000)")
    private String shopDescription;

    @Column(name = "shop_logo")
    private String shopLogo;

    @Column(name = "shop_banner")
    private String shopBanner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", nullable = false)
    private User vendor;

    @Column(name = "phone_number", length = 15)
    private String phoneNumber;

    @Column(name = "address", length = 500, columnDefinition = "NVARCHAR(500)")
    private String address;

    @Column(name = "city", length = 100, columnDefinition = "NVARCHAR(100)")
    private String city;

    @Column(name = "district", length = 100, columnDefinition = "NVARCHAR(100)")
    private String district;

    @Column(name = "ward", length = 100, columnDefinition = "NVARCHAR(100)")
    private String ward;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ShopStatus status = ShopStatus.PENDING;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "rejection_reason", length = 500, columnDefinition = "NVARCHAR(500)")
    private String rejectionReason;

    @Column(name = "total_products")
    private Integer totalProducts = 0;

    @Column(name = "total_orders")
    private Integer totalOrders = 0;

    @Column(name = "total_revenue")
    private Double totalRevenue = 0.0;

    @Column(name = "allow_cod")
    private Boolean allowCod = Boolean.TRUE;

    @Column(name = "preparation_days")
    private Integer preparationDays = 2;

    @Column(name = "allow_express")
    private Boolean allowExpress = Boolean.TRUE;  // Cho phép giao hỏa tốc

    @Column(name = "express_radius_km")
    private Integer expressRadiusKm = 20;  // Bán kính giao hỏa tốc (km)

    // Quan hệ Many-to-Many với Shipper (User)
    // Một shop có thể có nhiều shipper, một shipper có thể thuộc nhiều shop
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "shop_shippers",
        joinColumns = @JoinColumn(name = "shop_id"),
        inverseJoinColumns = @JoinColumn(name = "shipper_id")
    )
    @JsonIgnore
    private Set<User> shippers = new HashSet<>();

    public enum ShopStatus {
        PENDING,
        ACTIVE,
        SUSPENDED,
        REJECTED
    }
}

