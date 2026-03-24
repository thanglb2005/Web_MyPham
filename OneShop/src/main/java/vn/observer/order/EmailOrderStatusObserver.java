package vn.observer.order;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.entity.Order;
import vn.repository.OrderRepository;
import vn.service.SendMailService;
import vn.service.mail.OrderStatusEmailComposer;
import vn.service.mail.OrderStatusEmailComposer.OrderStatusEmailContent;

/**
 * Observer gửi email khi trạng thái đơn hàng thay đổi.
 * Nội dung HTML do {@link OrderStatusEmailComposer} dựng (cùng chất lượng với mail vendor/shipper trước đây).
 * <p>
 * Dùng {@link OrderRepository} thay vì {@code OrderService} để tránh vòng phụ thuộc:
 * OrderServiceImpl → OrderStatusSubject → observer này.
 */
@Component
public class EmailOrderStatusObserver implements OrderStatusObserver {

    private final SendMailService sendMailService;
    private final OrderRepository orderRepository;
    private final OrderStatusEmailComposer emailComposer;

    public EmailOrderStatusObserver(SendMailService sendMailService,
                                    OrderRepository orderRepository,
                                    OrderStatusEmailComposer emailComposer) {
        this.sendMailService = sendMailService;
        this.orderRepository = orderRepository;
        this.emailComposer = emailComposer;
    }

    @Override
    @Transactional(readOnly = true)
    public void onOrderStatusChanged(OrderStatusChangedEvent event) {
        Order order = loadOrderForEmail(event.getOrderId());
        if (order == null) {
            return;
        }

        OrderStatusEmailContent content = emailComposer
                .buildForTransition(order, event.getOldStatus(), event.getNewStatus())
                .orElse(null);
        if (content == null) {
            return;
        }

        String to = resolveCustomerEmail(order, event);
        if (to == null || to.isEmpty()) {
            return;
        }

        sendMailService.queue(to, content.subject(), content.htmlBody());
    }

    private String resolveCustomerEmail(Order order, OrderStatusChangedEvent event) {
        if (event.getCustomerEmail() != null && !event.getCustomerEmail().isEmpty()) {
            return event.getCustomerEmail();
        }
        if (order.getCustomerEmail() != null && !order.getCustomerEmail().isEmpty()) {
            return order.getCustomerEmail();
        }
        if (order.getUser() != null && order.getUser().getEmail() != null) {
            return order.getUser().getEmail();
        }
        return null;
    }

    /**
     * Load order trong transaction để lazy shop/shipper/user hoạt động khi build HTML.
     */
    private Order loadOrderForEmail(Long orderId) {
        if (orderId == null) {
            return null;
        }
        return orderRepository.findById(orderId).orElse(null);
    }
}
