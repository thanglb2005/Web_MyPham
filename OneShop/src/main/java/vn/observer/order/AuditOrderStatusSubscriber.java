package vn.observer.order;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Subscriber ghi audit mỗi lần đơn đổi trạng thái qua {@link OrderStatusPublisher}
 * (cùng vòng lặp notify với {@link EmailOrderStatusSubscriber}).
 * SLF4J để gom vào file/log chuẩn; console in rõ để dễ thấy khi demo (tương tự dòng === QUEUEING EMAIL ===).
 */
@Component
public class AuditOrderStatusSubscriber implements OrderStatusSubscriber {

    private static final Logger log = LoggerFactory.getLogger(AuditOrderStatusSubscriber.class);

    @Override
    public void update(OrderStatusChangedEvent event) {
        log.info(
                "[ORDER_AUDIT] orderId={} userId={} oldStatus={} newStatus={} changedAt={} source={}",
                event.getOrderId(),
                event.getUserId(),
                event.getOldStatus(),
                event.getNewStatus(),
                event.getChangedAt(),
                event.getSource());

        String line = "[ORDER_AUDIT] orderId=" + event.getOrderId()
                + " userId=" + event.getUserId()
                + " oldStatus=" + event.getOldStatus()
                + " newStatus=" + event.getNewStatus()
                + " changedAt=" + event.getChangedAt()
                + " source=" + event.getSource();
        System.out.println();
        System.out.println("=== ORDER_AUDIT (Observer subscriber) ===");
        System.out.println(line);
        System.out.println("=== END ORDER_AUDIT ===");
        System.out.println();
    }
}
