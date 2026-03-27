package vn.state.order;

import java.util.EnumMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import vn.entity.Order;

/**
 * Map {@link Order.OrderStatus} → {@link OrderState} (State pattern).
 */
@Component
public class OrderStateFactory {

    private final Map<Order.OrderStatus, OrderState> byStatus = new EnumMap<>(Order.OrderStatus.class);

    public OrderStateFactory() {
        PendingOrderState pending = new PendingOrderState();
        NewOrderState newState = new NewOrderState();
        ConfirmedOrderState confirmed = new ConfirmedOrderState();
        StandardOrderState standard = new StandardOrderState();

        byStatus.put(Order.OrderStatus.PENDING, pending);
        byStatus.put(Order.OrderStatus.NEW, newState);
        byStatus.put(Order.OrderStatus.CONFIRMED, confirmed);
        byStatus.put(Order.OrderStatus.SHIPPING, standard);
        byStatus.put(Order.OrderStatus.DELIVERED, standard);
        byStatus.put(Order.OrderStatus.OVERDUE, standard);
        byStatus.put(Order.OrderStatus.CANCELLED, standard);
        byStatus.put(Order.OrderStatus.RETURN_REQUESTED, standard);
        byStatus.put(Order.OrderStatus.RETURNED, standard);
    }

    public OrderState forOrder(Order order) {
        if (order == null || order.getStatus() == null) {
            throw new IllegalArgumentException("Order hoặc status không hợp lệ.");
        }
        OrderState state = byStatus.get(order.getStatus());
        if (state == null) {
            return new StandardOrderState();
        }
        return state;
    }
}
