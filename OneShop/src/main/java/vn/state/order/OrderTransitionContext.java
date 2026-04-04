package vn.state.order;

import vn.entity.Order;
import vn.entity.User;
import vn.service.OneXuService;
import vn.service.ProductService;
import vn.repository.OrderRepository;

/**
 * ① Context (State Design Pattern — GoF).
 *
 * <p>Giữ tham chiếu đến {@link OrderState} hiện tại và delegate các hành vi
 * phụ thuộc trạng thái sang State. ConcreteState có thể gọi
 * {@link #changeState(OrderState)} để chuyển trạng thái.</p>
 *
 * <p>Cung cấp các dependency (repository, service, publisher) cho ConcreteState
 * sử dụng khi thực hiện logic nghiệp vụ.</p>
 */
public final class OrderTransitionContext {

    /** ① Thuộc tính state — tham chiếu đến State hiện tại. */
    private OrderState state;

    private final Order order;
    private final OrderRepository orderRepository;
    private final ProductService productService;
    private final OneXuService oneXuService;
    private final OrderStatusPublisher publisher;

    /**
     * ① Context(initialState, ...) — Constructor nhận state ban đầu (GoF).
     *
     * @param initialState state ban đầu, được xác định bởi {@link OrderStateFactory}
     */
    public OrderTransitionContext(OrderState initialState,
                                  Order order,
                                  OrderRepository orderRepository,
                                  ProductService productService,
                                  OneXuService oneXuService,
                                  OrderStatusPublisher publisher) {
        this.order = order;
        this.orderRepository = orderRepository;
        this.productService = productService;
        this.oneXuService = oneXuService;
        this.publisher = publisher;
        changeState(initialState);  // gán state + setContext
    }

    /**
     * ① changeState(state) — Phương thức cho phép ConcreteState chuyển trạng thái (GoF mục ④).
     *
     * @param newState state mới cần chuyển sang
     */
    public void changeState(OrderState newState) {
        this.state = newState;
        newState.setContext(this);  // ③ gán context cho state mới
    }

    // ──────────────────────────────────────────────
    // ① Delegate methods — doThis() / doThat() (GoF)
    // Context delegate sang state.method()
    // ──────────────────────────────────────────────

    /** Delegate: xác nhận đơn hàng. */
    public void confirm() {
        state.confirm();
    }

    /** Delegate: hủy đơn bởi vendor. */
    public void cancelByVendor(User vendor) {
        state.cancelByVendor(vendor);
    }

    /** Delegate: cập nhật trạng thái tổng quát. */
    public void updateStatus(Order.OrderStatus newStatus, String source) {
        state.updateStatus(newStatus, source);
    }

    // ──────────────────────────────────────────────
    // Getter / helper cho ConcreteState sử dụng
    // ──────────────────────────────────────────────

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
