package vn.state.order;

import vn.entity.Order;
import vn.entity.OrderDetail;
import vn.entity.Product;
import vn.entity.User;

import java.time.LocalDateTime;

/**
 * Logic nghiệp vụ tách từ {@code OrderServiceImpl} (confirm / hủy vendor) để State gọi.
 */
public final class OrderStateActions {

    private OrderStateActions() {
    }

    public static void confirmFromPending(OrderTransitionContext ctx) {
        Order order = ctx.getOrder();

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

        if (order.getOrderDetails() != null && !order.getOrderDetails().isEmpty()) {
            for (OrderDetail orderDetail : order.getOrderDetails()) {
                Product product = orderDetail.getProduct();
                int oldQuantity = product.getQuantity();
                int newQuantity = product.getQuantity() - orderDetail.getQuantity();
                product.setQuantity(newQuantity);
                ctx.getProductService().save(product);
                System.out.println("Deducted " + orderDetail.getQuantity() + " units of product '" +
                        product.getProductName() + "' (ID: " + product.getProductId() + ") from stock. " +
                        "Old stock: " + oldQuantity + ", New stock: " + newQuantity);
            }
        }

        Order.OrderStatus oldStatus = order.getStatus();
        order.setStatus(Order.OrderStatus.CONFIRMED);
        ctx.saveOrder(order);
        ctx.publish(oldStatus, Order.OrderStatus.CONFIRMED, "OrderService.confirmOrder");
    }

    public static void cancelVendorPending(OrderTransitionContext ctx, User vendor) {
        Order order = ctx.getOrder();
        assertVendorOwnsOrder(order, vendor);

        // Giữ đúng code cũ OrderServiceImpl: luôn restore từng dòng chi tiết khi vendor hủy (kể cả PENDING)
        restoreStockForAllLines(ctx, order);

        Order.OrderStatus oldStatus = order.getStatus();
        order.setStatus(Order.OrderStatus.CANCELLED);
        order.setCancelledDate(LocalDateTime.now());
        ctx.saveOrder(order);
        ctx.publish(oldStatus, Order.OrderStatus.CANCELLED, "OrderService.cancelOrder");
    }

    public static void cancelVendorConfirmed(OrderTransitionContext ctx, User vendor) {
        Order order = ctx.getOrder();
        assertVendorOwnsOrder(order, vendor);

        restoreStockForAllLines(ctx, order);

        Order.OrderStatus oldStatus = order.getStatus();
        order.setStatus(Order.OrderStatus.CANCELLED);
        order.setCancelledDate(LocalDateTime.now());
        ctx.saveOrder(order);
        ctx.publish(oldStatus, Order.OrderStatus.CANCELLED, "OrderService.cancelOrder");
    }

    private static void assertVendorOwnsOrder(Order order, User vendor) {
        if (order.getShop() == null || !order.getShop().getVendor().equals(vendor)) {
            throw new IllegalStateException("Bạn không có quyền hủy đơn hàng này.");
        }
    }

    private static void restoreStockForAllLines(OrderTransitionContext ctx, Order order) {
        if (order.getOrderDetails() == null || order.getOrderDetails().isEmpty()) {
            return;
        }
        for (OrderDetail orderDetail : order.getOrderDetails()) {
            Product product = orderDetail.getProduct();
            int newQuantity = product.getQuantity() + orderDetail.getQuantity();
            product.setQuantity(newQuantity);
            ctx.getProductService().save(product);
            System.out.println("Restored " + orderDetail.getQuantity() + " units of product '" +
                    product.getProductName() + "' (ID: " + product.getProductId() + ") to stock. " +
                    "New stock: " + newQuantity);
        }
    }
}
