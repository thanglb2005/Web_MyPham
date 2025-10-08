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
}