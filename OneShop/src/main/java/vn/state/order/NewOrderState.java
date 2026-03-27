package vn.state.order;

import vn.entity.User;

/**
 * Đơn "NEW": giống code cũ {@code confirmOrder} chỉ cho PENDING — không xác nhận từ đây;
 * hủy vendor cũng không áp dụng (chỉ PENDING/CONFIRMED).
 */
public final class NewOrderState extends AbstractOrderState {

    @Override
    public void confirm(OrderTransitionContext ctx) {
        throw new IllegalStateException("Chỉ có thể xác nhận đơn hàng ở trạng thái 'Chờ xác nhận'.");
    }

    @Override
    public void cancelByVendor(OrderTransitionContext ctx, User vendor) {
        throw new IllegalStateException(
                "Chỉ có thể hủy đơn hàng khi ở trạng thái 'Chờ xác nhận' hoặc 'Đã xác nhận'.");
    }
}
