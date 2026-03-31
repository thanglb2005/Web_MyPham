package vn.observer.order;

import org.springframework.stereotype.Component;

/**
 * Observer ghi audit log đơn giản cho thay đổi trạng thái đơn hàng.
 */
@Component
public class AuditOrderStatusObserver implements OrderStatusObserver {

    /** In đậm + nền đảo màu — dễ nhìn trên Windows Terminal / VS Code / IDE hiện đại. */
    private static final String ANSI_EMPHASIS = "\u001B[1;7m";
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String RULE = "================================================================================";

    @Override
    public void onOrderStatusChanged(OrderStatusChangedEvent event) {
        String detail = "[ORDER_AUDIT] orderId=" + event.getOrderId()
                + ", userId=" + event.getUserId()
                + ", oldStatus=" + event.getOldStatus()
                + ", newStatus=" + event.getNewStatus()
                + ", changedAt=" + event.getChangedAt()
                + ", source=" + event.getSource();
        System.out.println();
        System.out.println(RULE);
        System.out.println(ANSI_EMPHASIS + " " + detail + " " + ANSI_RESET);
        System.out.println(RULE);
        System.out.println();
    }
}

