package vn.observer.order;

/**
 * Subscriber contract (Observer pattern) cho sự kiện thay đổi trạng thái đơn hàng.
 */
public interface OrderStatusSubscriber {
    void update(OrderStatusChangedEvent event);
}
