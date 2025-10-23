package vn.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Provider entity for shipping providers
 * @author OneShop Team
 */
@Entity
@Table(name = "shipping_providers")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Provider implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "provider_id")
    private Long providerId;
    
    @Column(name = "provider_name", nullable = false)
    private String providerName;
    
    @Column(name = "contact_phone")
    private String contactPhone;
    
    @Column(name = "contact_email")
    private String contactEmail;
    
    @Column(length = 1000)
    private String description;
    
    private String website;
    
    private String address;
    
    @Column(name = "shipping_fees")
    private Double shippingFees;
    
    @Column(name = "delivery_time_range")
    private String deliveryTimeRange;
    
    private String logo;
    
    @Column(nullable = false)
    private Boolean status = true;
}
