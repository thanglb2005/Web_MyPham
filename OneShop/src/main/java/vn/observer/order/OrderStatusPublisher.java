package vn.observer.order;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Publisher: nhận {@link OrderStatusChangedEvent} và thông báo cho tất cả subscribers.
 */
@Component
public class OrderStatusPublisher {

    private final List<OrderStatusSubscriber> subscribers;

    public OrderStatusPublisher(List<OrderStatusSubscriber> subscribers) {
        this.subscribers = subscribers;
    }

    public void notifySubscribers(OrderStatusChangedEvent event) {
        for (OrderStatusSubscriber subscriber : subscribers) {
            subscriber.update(event);
        }
    }
}
