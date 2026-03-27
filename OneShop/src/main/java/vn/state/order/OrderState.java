package vn.state.order;

import vn.entity.User;

/**
 * State pattern: hành vi chuyển trạng thái đơn hàng phụ thuộc trạng thái hiện tại.
 * Mỗi trạng thái {@link vn.entity.Order.OrderStatus} có một implementation tương ứng
 * (hoặc dùng chung {@link AbstractOrderState} cho các trạng thái chỉ cần cập nhật status chung).
 */
public interface OrderState {

    /**
     * Xác nhận đơn (vendor): chỉ trạng thái cho phép mới thực hiện (vd: PENDING, NEW).
     */
    void confirm(OrderTransitionContext ctx);

    /**
     * Hủy đơn bởi vendor: chỉ PENDING / CONFIRMED (kèm hoàn kho khi đã trừ).
     */
    void cancelByVendor(OrderTransitionContext ctx, User vendor);

    /**
     * Cập nhật trạng thái tổng quát (shipper, vendor mark delivered, admin, …) — giữ side effect
     * ngày giao, thưởng OneXu như code cũ {@code OrderServiceImpl.updateOrderStatus}.
     */
    void updateStatus(OrderTransitionContext ctx, vn.entity.Order.OrderStatus newStatus, String source);
}
