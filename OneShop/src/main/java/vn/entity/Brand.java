package vn.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Brand entity for cosmetic brands
 * @author OneShop Team
 */
@Entity
@Table(name = "brands")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Brand implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long brandId;
    
    @Column(nullable = false)
    private String brandName;
    
    private String brandImage;
    
    @Column(length = 1000)
    private String description;
    
    private String origin;
    
    @Column(nullable = false)
    private Boolean status = true;
}

