package vn.service;

import vn.entity.CartItem;
import vn.entity.Order;
import vn.entity.User;

import java.util.Collection;
import java.util.Map;

public interface OrderService {
    Order createOrder(User user, String customerName, String customerEmail, String customerPhone,
                      String shippingAddress, String note, Order.PaymentMethod paymentMethod,
                      Map<Long, CartItem> cartItems);

    Order getOrderById(Long orderId);

    Collection<Order> getOrdersByUser(User user);

    void updateOrderStatus(Long orderId, Order.OrderStatus newStatus);

    void assignShipper(Long orderId, User shipper);
}