package vn.state.order;

import java.time.LocalDateTime;

import vn.entity.Order;
import vn.entity.User;

/**
 * ③ Abstract ConcreteState (State Design Pattern — GoF).
 *
 * <p>Giữ tham chiếu ngược đến {@link OrderTransitionContext} (Context) qua thuộc tính
 * {@code context} và method {@link #setContext(OrderTransitionContext)}.</p>
 *
 * <p>Cung cấp hành vi mặc định: không cho confirm/hủy vendor (ném exception);
 * cập nhật status tổng quát + side effect (ngày giao, OneXu, publish Observer).</p>
 */
public abstract class AbstractOrderState implements OrderState {

    /** ③ Thuộc tính context — tham chiếu ngược đến Context (GoF). */
    protected OrderTransitionContext context;

    /** ③ setContext(context) — gán Context cho State (GoF). */
    @Override
    public void setContext(OrderTransitionContext context) {
        this.context = context;
    }

    @Override
    public void confirm() {
        throw new IllegalStateException("Chỉ có thể xác nhận đơn hàng ở trạng thái 'Chờ xác nhận'.");
    }

    @Override
    public void cancelByVendor(User vendor) {
        throw new IllegalStateException(
                "Chỉ có thể hủy đơn hàng khi ở trạng thái 'Chờ xác nhận' hoặc 'Đã xác nhận'.");
    }

    @Override
    public void updateStatus(Order.OrderStatus newStatus, String source) {
        Order order = context.getOrder();
        Order.OrderStatus oldStatus = order.getStatus();
        order.setStatus(newStatus);

        if (newStatus == Order.OrderStatus.SHIPPING && order.getShippedDate() == null) {
            order.setShippedDate(LocalDateTime.now());
        } else if (newStatus == Order.OrderStatus.DELIVERED && order.getDeliveredDate() == null) {
            order.setDeliveredDate(LocalDateTime.now());
            if (oldStatus != Order.OrderStatus.DELIVERED) {
                try {
                    context.getOneXuService().rewardFromOrder(
                            order.getUser().getUserId(),
                            order.getOrderId(),
                            order.getTotalAmount());
                } catch (Exception e) {
                    System.err.println("Error rewarding One Xu for order " + order.getOrderId() + ": " + e.getMessage());
                }
            }
        }

        context.saveOrder(order);
        context.publish(oldStatus, newStatus, source);

        // ④ State tự chuyển trạng thái qua Context (GoF)
        context.changeState(OrderStateFactory.createForStatus(newStatus));
    }
}
