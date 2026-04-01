package vn.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "addresses")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "address_id")
    private Long addressId;

    @Column(name = "street", nullable = false, length = 255, columnDefinition = "NVARCHAR(255)")
    private String street;

    @Column(name = "ward", nullable = false, length = 100, columnDefinition = "NVARCHAR(100)")
    private String ward;

    @Column(name = "city", nullable = false, length = 100, columnDefinition = "NVARCHAR(100)")
    private String city;

    public String getFullAddress() {
        return String.join(", ", street, ward, city);
    }
}
