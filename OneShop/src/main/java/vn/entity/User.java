package vn.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * User entity for authentication and user management
 * @author OneShop Team
 */
@Entity
@Table(name = "[user]", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(exclude = {"assignedShops", "roles"})
@ToString(exclude = {"assignedShops", "roles"})
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
    
    @Column(name = "one_xu_balance")
    private Double oneXuBalance = 0.0;

    // OAuth2 fields
    @Column(name = "provider")
    private String provider; // facebook, google, etc.
    
    @Column(name = "provider_id")
    private String providerId; // ID from OAuth2 provider
    
    @Column(name = "enabled")
    private Boolean enabled = true;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "users_roles",
            joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id"))
    private Collection<Role> roles;

    // Quan hệ Many-to-Many với Shop (cho shipper)
    // Một shipper có thể thuộc nhiều shop, một shop có thể có nhiều shipper
    @ManyToMany(mappedBy = "shippers", fetch = FetchType.LAZY)
    private Set<Shop> assignedShops = new HashSet<>();
}
