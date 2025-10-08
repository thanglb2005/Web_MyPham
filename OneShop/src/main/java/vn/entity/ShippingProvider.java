package vn.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Entity for shipping providers
 * @author OneShop Team
 */
@Entity
@Table(name = "shipping_providers")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShippingProvider implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long providerId;
    
    @Column(nullable = false)
    private String providerName;
    
    @Column(length = 15)
    private String contactPhone;
    
    @Column
    private String contactEmail;
    
    @Column(length = 1000)
    private String description;
    
    @Column
    private String website;
    
    @Column
    private String address;
    
    @Column
    private Double shippingFees;
    
    @Column
    private String deliveryTimeRange;
    
    @Column
    private String logo;
    
    @Column(nullable = false)
    private Boolean status = true;
}