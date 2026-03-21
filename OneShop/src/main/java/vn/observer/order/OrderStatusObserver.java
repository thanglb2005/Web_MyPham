package vn.observer.order;

/**
 * Observer contract cho sự kiện thay đổi trạng thái đơn hàng.
 */
public interface OrderStatusObserver {
    void onOrderStatusChanged(OrderStatusChangedEvent event);
}
