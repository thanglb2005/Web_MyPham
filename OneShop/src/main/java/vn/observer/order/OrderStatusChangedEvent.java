package vn.observer.order;

import vn.entity.Order;

import java.time.LocalDateTime;

/**
 * Event được phát khi trạng thái đơn hàng thay đổi.
 */
public class OrderStatusChangedEvent {
    private final Long orderId;
    private final Long userId;
    private final String customerEmail;
    private final String customerName;
    private final Order.OrderStatus oldStatus;
    private final Order.OrderStatus newStatus;
    private final LocalDateTime changedAt;
    private final String source;

    public OrderStatusChangedEvent(Long orderId,
                                   Long userId,
                                   String customerEmail,
                                   String customerName,
                                   Order.OrderStatus oldStatus,
                                   Order.OrderStatus newStatus,
                                   LocalDateTime changedAt,
                                   String source) {
        this.orderId = orderId;
        this.userId = userId;
        this.customerEmail = customerEmail;
        this.customerName = customerName;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.changedAt = changedAt;
        this.source = source;
    }

    public Long getOrderId() { return orderId; }
    public Long getUserId() { return userId; }
    public String getCustomerEmail() { return customerEmail; }
    public String getCustomerName() { return customerName; }
    public Order.OrderStatus getOldStatus() { return oldStatus; }
    public Order.OrderStatus getNewStatus() { return newStatus; }
    public LocalDateTime getChangedAt() { return changedAt; }
    public String getSource() { return source; }
}
