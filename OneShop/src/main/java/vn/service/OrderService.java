package vn.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.entity.CartItem;
import vn.entity.Order;
import vn.entity.User;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface OrderService {
    Order createOrder(User user, String customerName, String customerEmail, String customerPhone,
                      String shippingAddress, String note, Order.PaymentMethod paymentMethod,
                      Map<Long, CartItem> cartItems);
    
    Order createOrder(User user, String customerName, String customerEmail, String customerPhone,
                      String shippingAddress, String note, Order.PaymentMethod paymentMethod,
                      Map<Long, CartItem> cartItems, String promotionCode, Double discountAmount);

    Order getOrderById(Long orderId);

    Collection<Order> getOrdersByUser(User user);

    void updateOrderStatus(Long orderId, Order.OrderStatus newStatus);

    void assignShipper(Long orderId, User shipper);

    void updateOrder(Order order);

    void deleteOrder(Long orderId);

    // ===== VENDOR ORDER MANAGEMENT METHODS =====

    /**
     * Find orders by shop IDs with status filter
     */
    Page<Order> findByShopIdInAndStatus(List<Long> shopIds, Order.OrderStatus status, Pageable pageable);

    /**
     * Find orders by shop IDs with search term
     */
    Page<Order> findByShopIdInAndOrderIdContaining(List<Long> shopIds, String search, Pageable pageable);

    /**
     * Find orders by shop IDs with status and search term
     */
    Page<Order> findByShopIdInAndStatusAndOrderIdContaining(List<Long> shopIds, Order.OrderStatus status, String search, Pageable pageable);

    /**
     * Find orders by shop IDs only
     */
    Page<Order> findByShopIdIn(List<Long> shopIds, Pageable pageable);

    // ===== Direct queries using orders.shop_id mapping (không thay thế logic cũ) =====
    Page<Order> findByShopIdInAndStatusDirect(List<Long> shopIds, Order.OrderStatus status, Pageable pageable);
    Page<Order> findByShopIdInDirect(List<Long> shopIds, Pageable pageable);
    Page<Order> findByShopIdInAndOrderIdContainingDirect(List<Long> shopIds, String search, Pageable pageable);
    Page<Order> findByShopIdInAndStatusAndOrderIdContainingDirect(List<Long> shopIds, Order.OrderStatus status, String search, Pageable pageable);
    Long countByShopIdInAndStatusDirect(List<Long> shopIds, Order.OrderStatus status);

    /**
     * Find order by ID and shop IDs (for vendor access control)
     */
    Optional<Order> findByIdAndShopIdIn(Long orderId, List<Long> shopIds);

    Optional<Order> findWithDetailsByIdAndShopIdIn(Long orderId, List<Long> shopIds);

    /**
     * Count orders by shop IDs and status
     */
    Long countByShopIdInAndStatus(List<Long> shopIds, Order.OrderStatus status);

    /**
     * Count orders by shop IDs
     */
    Long countByShopIdIn(List<Long> shopIds);

    Optional<Order> findById(Long orderId);

    void confirmOrder(Long orderId);

    void cancelOrder(Long orderId, User vendor);

    List<Object[]> countOrdersByStatus(List<Long> shopIds);

    // ===== OVERDUE DELIVERY MANAGEMENT METHODS =====

    /**
     * Find all overdue orders (past estimated delivery date and still shipping)
     */
    List<Order> findOverdueOrders();

    /**
     * Find overdue orders for a specific shipper
     */
    List<Order> findOverdueOrdersByShipper(User shipper);

    /**
     * Count overdue orders for a specific shipper
     */
    long countOverdueOrdersByShipper(User shipper);

    /**
     * Mark orders as overdue (automatically called by scheduled task)
     */
    void markOverdueOrders();

    /**
     * Check if an order is overdue
     */
    boolean isOrderOverdue(Order order);

    /**
     * Find orders that need to be marked as overdue (currently SHIPPING but past estimated delivery date)
     */
    List<Order> findOrdersToMarkOverdue();
}
