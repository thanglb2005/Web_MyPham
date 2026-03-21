package vn.observer.order;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Subject/Publisher: nhận event và notify cho tất cả observers.
 */
@Component
public class OrderStatusSubject {

    private final List<OrderStatusObserver> observers;

    public OrderStatusSubject(List<OrderStatusObserver> observers) {
        this.observers = observers;
    }

    public void notifyOrderStatusChanged(OrderStatusChangedEvent event) {
        for (OrderStatusObserver observer : observers) {
            observer.onOrderStatusChanged(event);
        }
    }
}
