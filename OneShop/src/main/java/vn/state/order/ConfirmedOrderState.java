package vn.state.order;

import vn.entity.User;

/** Đã xác nhận: hủy vendor hoàn kho; không confirm lại. */
public final class ConfirmedOrderState extends AbstractOrderState {

    @Override
    public void cancelByVendor(OrderTransitionContext ctx, User vendor) {
        OrderStateActions.cancelVendorConfirmed(ctx, vendor);
    }
}
