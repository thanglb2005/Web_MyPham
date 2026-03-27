package vn.state.order;

import vn.entity.User;

/** Trạng thái chờ xác nhận: cho phép confirm và hủy bởi vendor (không hoàn kho). */
public final class PendingOrderState extends AbstractOrderState {

    @Override
    public void confirm(OrderTransitionContext ctx) {
        OrderStateActions.confirmFromPending(ctx);
    }

    @Override
    public void cancelByVendor(OrderTransitionContext ctx, User vendor) {
        OrderStateActions.cancelVendorPending(ctx, vendor);
    }
}
