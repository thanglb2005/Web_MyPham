package vn.state.order;

import org.springframework.stereotype.Component;

import vn.entity.Order;

/**
 * Factory map {@link Order.OrderStatus} → {@link OrderState} (State pattern).
 *
 * <p>Tạo instance mới mỗi lần vì mỗi ConcreteState giữ tham chiếu riêng
 * đến {@link OrderTransitionContext} (theo chuẩn GoF).</p>
 */
@Component
public class OrderStateFactory {

    /**
     * Tạo OrderState tương ứng với trạng thái hiện tại của đơn hàng.
     *
     * @param order đơn hàng cần lấy state
     * @return OrderState mới (instance riêng)
     */
    public OrderState forOrder(Order order) {
        if (order == null || order.getStatus() == null) {
            throw new IllegalArgumentException("Order hoặc status không hợp lệ.");
        }
        return createForStatus(order.getStatus());
    }

    /**
     * Static factory method: tạo OrderState cho một status cụ thể.
     * ConcreteState gọi method này khi cần chuyển trạng thái qua
     * {@code context.changeState(OrderStateFactory.createForStatus(newStatus))}.
     *
     * @param status trạng thái đơn hàng
     * @return OrderState mới tương ứng
     */
    public static OrderState createForStatus(Order.OrderStatus status) {
        switch (status) {
            case PENDING:
                return new PendingOrderState();
            case CONFIRMED:
                return new ConfirmedOrderState();
            case NEW:
            case SHIPPING:
            case DELIVERED:
            case OVERDUE:
            case CANCELLED:
            case RETURN_REQUESTED:
            case RETURNED:
                return new StandardOrderState();
            default:
                return new StandardOrderState();
        }
    }
}
