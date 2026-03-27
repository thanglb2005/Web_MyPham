package vn.state.order;

import vn.entity.Order;

/**
 * Callback để State gọi lại {@code OrderServiceImpl} publish Observer sau khi đổi trạng thái
 * (không phá pattern Observer đã có).
 */
@FunctionalInterface
public interface OrderStatusPublisher {

    void publish(Order order, Order.OrderStatus oldStatus, Order.OrderStatus newStatus, String source);
}
