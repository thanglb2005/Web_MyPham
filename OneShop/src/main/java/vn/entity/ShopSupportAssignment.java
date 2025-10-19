package vn.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Mapping of a Shop to an assigned CSKH agent.
 * Enforces: one active CSKH per shop for liaison chats.
 */
@Entity
@Table(name = "shop_support_assignment", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"shop_id"})
})
public class ShopSupportAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cskh_id", nullable = false)
    private User cskh;

    @Column(name = "assigned_at", nullable = false)
    private LocalDateTime assignedAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Shop getShop() {
        return shop;
    }

    public void setShop(Shop shop) {
        this.shop = shop;
    }

    public User getCskh() {
        return cskh;
    }

    public void setCskh(User cskh) {
        this.cskh = cskh;
    }

    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(LocalDateTime assignedAt) {
        this.assignedAt = assignedAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

