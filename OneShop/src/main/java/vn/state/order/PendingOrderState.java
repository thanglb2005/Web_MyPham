package vn.state.order;

import java.time.LocalDateTime;

import vn.entity.Order;
import vn.entity.OrderDetail;
import vn.entity.Product;
import vn.entity.User;

/**
 * ③ ConcreteState: trạng thái chờ xác nhận (PENDING).
 *
 * <p>Cho phép {@link #confirm()} (trừ kho + chuyển CONFIRMED)
 * và {@link #cancelByVendor(User)} (hoàn kho + chuyển CANCELLED).</p>
 */
public final class PendingOrderState extends AbstractOrderState {

    @Override
    public void confirm() {
        Order order = context.getOrder();

        // Validate tồn kho
        if (order.getOrderDetails() != null && !order.getOrderDetails().isEmpty()) {
            for (OrderDetail orderDetail : order.getOrderDetails()) {
                Product product = orderDetail.getProduct();
                if (product.getQuantity() < orderDetail.getQuantity()) {
                    throw new IllegalStateException(
                            "Sản phẩm '" + product.getProductName() + "' không đủ tồn kho. " +
                                    "Cần: " + orderDetail.getQuantity() + ", Có: " + product.getQuantity()
                    );
                }
            }
        }

        // Trừ kho
        if (order.getOrderDetails() != null && !order.getOrderDetails().isEmpty()) {
            for (OrderDetail orderDetail : order.getOrderDetails()) {
                Product product = orderDetail.getProduct();
                int oldQuantity = product.getQuantity();
                int newQuantity = product.getQuantity() - orderDetail.getQuantity();
                product.setQuantity(newQuantity);
                context.getProductService().save(product);
                System.out.println("Deducted " + orderDetail.getQuantity() + " units of product '" +
                        product.getProductName() + "' (ID: " + product.getProductId() + ") from stock. " +
                        "Old stock: " + oldQuantity + ", New stock: " + newQuantity);
            }
        }

        Order.OrderStatus oldStatus = order.getStatus();
        order.setStatus(Order.OrderStatus.CONFIRMED);
        context.saveOrder(order);
        context.publish(oldStatus, Order.OrderStatus.CONFIRMED, "OrderService.confirmOrder");

        // ④ State tự chuyển trạng thái qua Context (GoF)
        context.changeState(new ConfirmedOrderState());
    }

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
