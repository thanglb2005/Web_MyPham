package vn.state.order;

import vn.entity.Order;
import vn.service.OneXuService;
import vn.service.ProductService;
import vn.repository.OrderRepository;

/**
 * Context truyền vào các {@link OrderState}: đơn hàng + dependency cần cho chuyển trạng thái.
 */
public final class OrderTransitionContext {

    private final Order order;
    private final OrderRepository orderRepository;
    private final ProductService productService;
    private final OneXuService oneXuService;
    private final OrderStatusPublisher publisher;

    public OrderTransitionContext(Order order,
                                  OrderRepository orderRepository,
                                  ProductService productService,
                                  OneXuService oneXuService,
                                  OrderStatusPublisher publisher) {
        this.order = order;
        this.orderRepository = orderRepository;
        this.productService = productService;
        this.oneXuService = oneXuService;
        this.publisher = publisher;
    }

    public Order getOrder() {
        return order;
    }

    public OrderRepository getOrderRepository() {
        return orderRepository;
    }

    public ProductService getProductService() {
        return productService;
    }

    public OneXuService getOneXuService() {
        return oneXuService;
    }

    public void saveOrder(Order o) {
        orderRepository.save(o);
    }

    public void publish(Order.OrderStatus oldStatus, Order.OrderStatus newStatus, String source) {
        publisher.publish(order, oldStatus, newStatus, source);
    }
}
