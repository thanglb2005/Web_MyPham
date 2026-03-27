package vn.state.order;

import java.time.LocalDateTime;

import vn.entity.Order;
import vn.entity.User;

/**
 * Trạng thái mặc định: không cho confirm/hủy vendor; cập nhật status tổng quát giống code cũ
 * {@code OrderServiceImpl.updateOrderStatus}.
 */
public abstract class AbstractOrderState implements OrderState {

    @Override
    public void confirm(OrderTransitionContext ctx) {
        throw new IllegalStateException("Chỉ có thể xác nhận đơn hàng ở trạng thái 'Chờ xác nhận'.");
    }

    @Override
    public void cancelByVendor(OrderTransitionContext ctx, User vendor) {
        throw new IllegalStateException(
                "Chỉ có thể hủy đơn hàng khi ở trạng thái 'Chờ xác nhận' hoặc 'Đã xác nhận'.");
    }

    @Override
    public void updateStatus(OrderTransitionContext ctx, Order.OrderStatus newStatus, String source) {
        Order order = ctx.getOrder();
        Order.OrderStatus oldStatus = order.getStatus();
        order.setStatus(newStatus);

        if (newStatus == Order.OrderStatus.SHIPPING && order.getShippedDate() == null) {
            order.setShippedDate(LocalDateTime.now());
        } else if (newStatus == Order.OrderStatus.DELIVERED && order.getDeliveredDate() == null) {
            order.setDeliveredDate(LocalDateTime.now());
            if (oldStatus != Order.OrderStatus.DELIVERED) {
                try {
                    ctx.getOneXuService().rewardFromOrder(
                            order.getUser().getUserId(),
                            order.getOrderId(),
                            order.getTotalAmount());
                } catch (Exception e) {
                    System.err.println("Error rewarding One Xu for order " + order.getOrderId() + ": " + e.getMessage());
                }
            }
        }

        ctx.saveOrder(order);
        ctx.publish(oldStatus, newStatus, source);
    }
}
