package vn.state.order;

import java.time.LocalDateTime;

import vn.entity.Order;
import vn.entity.OrderDetail;
import vn.entity.Product;
import vn.entity.User;

/**
 * ③ ConcreteState: trạng thái đã xác nhận (CONFIRMED).
 *
 * <p>Cho phép {@link #cancelByVendor(User)} (hoàn kho + chuyển CANCELLED).
 * Không cho confirm lại.</p>
 */
public final class ConfirmedOrderState extends AbstractOrderState {

    @Override
    public void cancelByVendor(User vendor) {
        Order order = context.getOrder();

        // Hoàn kho
        restoreStockForAllLines(order);

        Order.OrderStatus oldStatus = order.getStatus();
        order.setStatus(Order.OrderStatus.CANCELLED);
        order.setCancelledDate(LocalDateTime.now());
        context.saveOrder(order);
        context.publish(oldStatus, Order.OrderStatus.CANCELLED, "OrderService.cancelOrder");

        // ④ State tự chuyển trạng thái qua Context (GoF)
        context.changeState(new StandardOrderState());
    }

    private void restoreStockForAllLines(Order order) {
        if (order.getOrderDetails() == null || order.getOrderDetails().isEmpty()) {
            return;
        }
        for (OrderDetail orderDetail : order.getOrderDetails()) {
            Product product = orderDetail.getProduct();
            int newQuantity = product.getQuantity() + orderDetail.getQuantity();
            product.setQuantity(newQuantity);
            context.getProductService().save(product);
            System.out.println("Restored " + orderDetail.getQuantity() + " units of product '" +
                    product.getProductName() + "' (ID: " + product.getProductId() + ") to stock. " +
                    "New stock: " + newQuantity);
        }
    }
}
