package vn.state.order;

import vn.entity.Order;
import vn.entity.User;

/**
 * ② State interface (State Design Pattern — GoF).
 *
 * Khai báo các hành vi phụ thuộc trạng thái đơn hàng.
 * Mỗi {@link Order.OrderStatus} có một ConcreteState tương ứng.
 *
 * <p>Theo chuẩn GoF: State interface không nhận Context làm tham số —
 * ConcreteState giữ tham chiếu ngược đến Context qua {@link #setContext(OrderTransitionContext)}.</p>
 */
public interface OrderState {

    /**
     * ③ Gán Context cho State (GoF: ConcreteState giữ tham chiếu ngược đến Context).
     */
    void setContext(OrderTransitionContext context);

    /**
     * Xác nhận đơn (vendor): chỉ trạng thái cho phép mới thực hiện (vd: PENDING).
     */
    void confirm();

    /**
     * Hủy đơn bởi vendor: chỉ PENDING / CONFIRMED (kèm hoàn kho khi đã trừ).
     */
    void cancelByVendor(User vendor);

    /**
     * Cập nhật trạng thái tổng quát (shipper, vendor mark delivered, admin, …) — giữ side effect
     * ngày giao, thưởng OneXu.
     */
    void updateStatus(Order.OrderStatus newStatus, String source);
}
