package vn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.entity.Order;
import vn.entity.User;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Find orders by user
     */
    List<Order> findByUserOrderByOrderDateDesc(User user);

    /**
     * Find orders by status
     */
    List<Order> findByStatusOrderByOrderDateDesc(Order.OrderStatus status);

    /**
     * Get monthly order statistics for chart data
     * Returns: [year, month, orderCount, totalRevenue]
     */
    @Query("SELECT YEAR(o.orderDate) as year, MONTH(o.orderDate) as month, " +
           "COUNT(o) as orderCount, SUM(o.totalAmount) as totalRevenue " +
           "FROM Order o " +
           "WHERE o.status = 'DELIVERED' " +
           "GROUP BY YEAR(o.orderDate), MONTH(o.orderDate) " +
           "ORDER BY year, month")
    List<Object[]> getMonthlyOrderStatistics();

    /**
     * Get top customers by order value
     */
    @Query("SELECT o.user, SUM(o.totalAmount) as totalSpent, COUNT(o) as orderCount " +
           "FROM Order o WHERE o.status = 'DELIVERED' " +
           "GROUP BY o.user ORDER BY totalSpent DESC")
    List<Object[]> getTopCustomersByValue();

    /**
     * Get top customers by order count
     */
    @Query("SELECT o.user, COUNT(o) as orderCount, SUM(o.totalAmount) as totalSpent " +
           "FROM Order o WHERE o.status = 'DELIVERED' " +
           "GROUP BY o.user ORDER BY orderCount DESC")
    List<Object[]> getTopCustomersByCount();

    /**
     * Find orders by shipper
     */
    List<Order> findByShipperOrderByOrderDateDesc(User shipper);

    /**
     * Find orders by status and no shipper assigned
     */
    List<Order> findByStatusAndShipperIsNullOrderByOrderDateDesc(Order.OrderStatus status);

    /**
     * Get shipper statistics - orders by status
     * Returns: [status, count]
     */
    @Query("SELECT o.status, COUNT(o) " +
           "FROM Order o " +
           "WHERE o.shipper = :shipper " +
           "GROUP BY o.status")
    List<Object[]> getShipperOrderStatsByStatus(User shipper);

    /**
     * Get shipper monthly statistics
     * Returns: [year, month, totalOrders, deliveredOrders, totalAmount]
     */
    @Query("SELECT YEAR(o.orderDate) as year, MONTH(o.orderDate) as month, " +
           "COUNT(o) as totalOrders, " +
           "SUM(CASE WHEN o.status = 'DELIVERED' THEN 1 ELSE 0 END) as deliveredOrders, " +
           "SUM(CASE WHEN o.status = 'DELIVERED' THEN o.totalAmount ELSE 0 END) as totalAmount " +
           "FROM Order o " +
           "WHERE o.shipper = :shipper " +
           "GROUP BY YEAR(o.orderDate), MONTH(o.orderDate) " +
           "ORDER BY year DESC, month DESC")
    List<Object[]> getShipperMonthlyStatistics(User shipper);

    /**
     * Get shipper daily statistics for current month
     * Returns: [day, totalOrders, deliveredOrders]
     */
    @Query("SELECT DAY(o.orderDate) as day, " +
           "COUNT(o) as totalOrders, " +
           "SUM(CASE WHEN o.status = 'DELIVERED' THEN 1 ELSE 0 END) as deliveredOrders " +
           "FROM Order o " +
           "WHERE o.shipper = :shipper " +
           "AND YEAR(o.orderDate) = :year " +
           "AND MONTH(o.orderDate) = :month " +
           "GROUP BY DAY(o.orderDate) " +
           "ORDER BY day")
    List<Object[]> getShipperDailyStatistics(User shipper, int year, int month);

    /**
     * Count orders by shipper and status
     */
    long countByShipperAndStatus(User shipper, Order.OrderStatus status);

    /**
     * Get total delivered amount for shipper
     */
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) " +
           "FROM Order o " +
           "WHERE o.shipper = :shipper AND o.status = 'DELIVERED'")
    Double getTotalDeliveredAmountByShipper(User shipper);

    /**
     * Find available orders for shipper based on their assigned shops
     * Returns orders with status CONFIRMED, no shipper assigned, and from shops the shipper is assigned to
     */
    @Query("SELECT DISTINCT o FROM Order o " +
           "JOIN o.orderDetails od " +
           "JOIN od.product p " +
           "JOIN p.shop s " +
           "JOIN s.shippers shipper " +
           "WHERE o.status = :status " +
           "AND o.shipper IS NULL " +
           "AND shipper = :shipper " +
           "ORDER BY o.orderDate DESC")
    List<Order> findAvailableOrdersForShipper(User shipper, Order.OrderStatus status);

    /**
     * Find orders assigned to shipper (for their assigned shops)
     */
    @Query("SELECT DISTINCT o FROM Order o " +
           "WHERE o.shipper = :shipper " +
           "ORDER BY o.orderDate DESC")
    List<Order> findOrdersByShipper(User shipper);
}