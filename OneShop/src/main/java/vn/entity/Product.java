package vn.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * Product entity for cosmetic products
 * @author OneShop Team
 */
@Entity
@Table(name = "products")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Product implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long productId;
    
    @Column(length = 1000)
    private String description;
    
    @Column(nullable = false)
    private Integer discount;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date enteredDate;
    
    @Column(nullable = false)
    private Double price;
    
    private String productImage;
    
    private String productName;
    
    @Column(nullable = false)
    private Integer quantity;
    
    private Boolean status;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id")
    private Brand brand;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Shop shop;
    
    @Temporal(TemporalType.DATE)
    private Date manufactureDate;
    
    @Temporal(TemporalType.DATE)
    private Date expiryDate;
    
    @Column(nullable = false)
    private Boolean favorite = false;
}

