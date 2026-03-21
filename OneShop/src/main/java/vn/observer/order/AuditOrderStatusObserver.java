package vn.observer.order;

import org.springframework.stereotype.Component;

/**
 * Observer ghi audit log đơn giản cho thay đổi trạng thái đơn hàng.
 */
@Component
public class AuditOrderStatusObserver implements OrderStatusObserver {

    @Override
    public void onOrderStatusChanged(OrderStatusChangedEvent event) {
        System.out.println("[ORDER_AUDIT] orderId=" + event.getOrderId()
                + ", userId=" + event.getUserId()
                + ", oldStatus=" + event.getOldStatus()
                + ", newStatus=" + event.getNewStatus()
                + ", changedAt=" + event.getChangedAt()
                + ", source=" + event.getSource());
    }
}

