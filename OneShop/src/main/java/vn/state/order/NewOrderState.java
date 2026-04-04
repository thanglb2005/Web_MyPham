package vn.state.order;

import vn.entity.User;

/**
 * ③ ConcreteState: trạng thái đơn mới (NEW).
 *
 * <p>Không cho phép confirm hoặc hủy bởi vendor — ném {@link IllegalStateException}.</p>
 */
public final class NewOrderState extends AbstractOrderState {

    @Override
    public void confirm() {
        throw new IllegalStateException("Chỉ có thể xác nhận đơn hàng ở trạng thái 'Chờ xác nhận'.");
    }

    @Override
    public void cancelByVendor(User vendor) {
        throw new IllegalStateException(
                "Chỉ có thể hủy đơn hàng khi ở trạng thái 'Chờ xác nhận' hoặc 'Đã xác nhận'.");
    }
}
