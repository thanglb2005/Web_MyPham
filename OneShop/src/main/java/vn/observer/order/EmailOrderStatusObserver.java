package vn.observer.order;

import org.springframework.stereotype.Component;
import vn.service.SendMailService;

/**
 * Observer gui email khi trang thai don hang thay doi.
 */
@Component
public class EmailOrderStatusObserver implements OrderStatusObserver {

    private final SendMailService sendMailService;

    public EmailOrderStatusObserver(SendMailService sendMailService) {
        this.sendMailService = sendMailService;
    }

    @Override
    public void onOrderStatusChanged(OrderStatusChangedEvent event) {
        if (event.getCustomerEmail() == null || event.getCustomerEmail().isEmpty()) {
            return;
        }

        String subject = "[OneShop] Don hang #" + event.getOrderId() + " da cap nhat trang thai";
        String body = "Xin chao " + (event.getCustomerName() != null ? event.getCustomerName() : "Quy khach") + ",<br/>"
                + "Trang thai don hang <b>#" + event.getOrderId() + "</b> da thay doi tu "
                + "<b>" + event.getOldStatus() + "</b> sang <b>" + event.getNewStatus() + "</b>.<br/>"
                + "Thoi gian: " + event.getChangedAt() + ".<br/>"
                + "Cam on ban da mua sam tai OneShop.";

        sendMailService.queue(event.getCustomerEmail(), subject, body);
    }
}

