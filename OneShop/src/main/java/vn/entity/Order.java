package vn.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long orderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "customer_name", nullable = false)
    private String customerName;

    @Column(name = "customer_email", nullable = false)
    private String customerEmail;

    @Column(name = "customer_phone", nullable = false)
    private String customerPhone;

    @Column(name = "shipping_address", nullable = false)
    private String shippingAddress;
    
    @Column(name = "pickup_address")
    private String pickupAddress;  // Địa chỉ lấy hàng (từ shop/vendor)
    
    @Column(name = "package_type")
    private String packageType;  // Loại hàng: Hàng nhỏ, Hàng dễ vỡ, Thực phẩm, etc.
    
    @Column(name = "weight")
    private Double weight;  // Khối lượng (kg)

    @Column(columnDefinition = "TEXT")
    private String note;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50, nullable = false)
    private OrderStatus status = OrderStatus.NEW;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod = PaymentMethod.COD;

    @Column(name = "total_amount", nullable = false)
    private Double totalAmount;

    @Column(name = "order_date", nullable = false)
    private LocalDateTime orderDate = LocalDateTime.now();

    @Column(name = "shipped_date")
    private LocalDateTime shippedDate;

    @Column(name = "delivered_date")
    private LocalDateTime deliveredDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipper_id")
    private User shipper;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id")
    private Shop shop;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderDetail> orderDetails;

    // Payment fields from DB schema
    // Note: DB stores payment_status as BIT (0/1). We map it to Boolean for simplicity.
    @Column(name = "payment_status")
    private Boolean paymentPaid; // true = PAID, false/null = not paid

    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    public enum OrderStatus {
        PENDING,                // Cho xac nhan
        NEW,                    // Don hang moi
        CONFIRMED,              // Da xac nhan
        SHIPPING,               // Dang giao
        DELIVERED,              // Da giao
        CANCELLED,              // Huy (2 chu L - British spelling)
        CANCELED,               // Huy (1 chu L - American spelling)
        RETURNED_REFUNDED       // Tra hang - hoan tien
    }

    public enum PaymentMethod {
        COD,
        MOMO,
        BANK_TRANSFER,
        VIETQR
    }

    public enum PaymentStatus {
        PENDING,    // Chờ thanh toán
        PAID,       // Đã thanh toán
        FAILED      // Thanh toán thất bại
    }
}
