package vn.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ShippingProvider entity - Đơn vị vận chuyển
 * GHN, GHTK, J&T Express, Viettel Post, VNPost...
 */
@Entity
@Table(name = "shipping_providers")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShippingProvider {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "provider_id")
    private Long providerId;

    @Column(name = "provider_name", nullable = false, columnDefinition = "NVARCHAR(255)")
    private String providerName;

    @Column(name = "contact_phone", length = 15)
    private String contactPhone;

    @Column(name = "contact_email", columnDefinition = "NVARCHAR(255)")
    private String contactEmail;

    @Column(name = "description", length = 1000, columnDefinition = "NVARCHAR(1000)")
    private String description;

    @Column(name = "website", columnDefinition = "NVARCHAR(255)")
    private String website;

    @Column(name = "address", columnDefinition = "NVARCHAR(255)")
    private String address;

    @Column(name = "shipping_fees")
    private Double shippingFees;

    @Column(name = "delivery_time_range", columnDefinition = "NVARCHAR(255)")
    private String deliveryTimeRange;  // VD: "1-3 ngày"

    @Column(name = "logo", columnDefinition = "NVARCHAR(255)")
    private String logo;

    @Column(name = "status")
    private Boolean status = true;  // true = active, false = inactive
}

