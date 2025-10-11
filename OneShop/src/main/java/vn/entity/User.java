package vn.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;

/**
 * User entity for authentication and user management
 * @author OneShop Team
 */
@Entity
@Table(name = "[user]", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;
    
    private String name;
    
    @Column(unique = true)
    private String email;
    
    private String password;
    
    private String avatar;
    
    @Temporal(TemporalType.DATE)
    private Date registerDate;
    
    private Boolean status;
    
    // Shipping provider for shipper role (GHN, GHTK, J&T Express, Viettel Post, VNPost)
    @Column(name = "shipping_provider")
    private String shippingProvider;

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(name = "users_roles",
            joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id"))
    private Collection<Role> roles;
}
