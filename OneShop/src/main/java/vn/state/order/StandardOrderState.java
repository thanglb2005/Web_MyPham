package vn.state.order;

/**
 * ③ ConcreteState: trạng thái chung (SHIPPING, DELIVERED, OVERDUE, CANCELLED, RETURN_REQUESTED, RETURNED).
 *
 * <p>Chỉ dùng {@link AbstractOrderState#updateStatus} cho cập nhật trạng thái tổng quát
 * (shipper, giao hàng, trả hàng, …). Không cho confirm hay hủy vendor.</p>
 */
public final class StandardOrderState extends AbstractOrderState {
}
