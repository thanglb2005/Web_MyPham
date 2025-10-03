package vn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.entity.Order;

import java.util.List;

/**
 * Repository for Order entity
 * @author OneShop Team
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query(value = "SELECT * FROM orders WHERE user_id = ?1", nativeQuery = true)
    List<Order> findOrderByUserId(Long userId);
    
    @Query(value = "SELECT * FROM orders ORDER BY order_date DESC", nativeQuery = true)
    List<Order> findAllOrderByOrderDateDesc();
    
    @Query(value = "SELECT * FROM orders WHERE status = ?1", nativeQuery = true)
    List<Order> findByStatus(Integer status);

    // ========== STATISTICS QUERIES ==========

    /**
     * Get order status statistics
     * Returns: status, status_name, order_count, total_amount
     */
    @Query(value = "SELECT o.status, " +
            "CASE " +
            "WHEN o.status = 0 THEN 'Chờ xác nhận' " +
            "WHEN o.status = 1 THEN 'Đã xác nhận' " +
            "WHEN o.status = 2 THEN 'Đã giao hàng' " +
            "WHEN o.status = 3 THEN 'Đã hủy' " +
            "ELSE 'Không xác định' " +
            "END as status_name, " +
            "COUNT(o.order_id) as order_count, " +
            "SUM(o.amount) as total_amount " +
            "FROM orders o " +
            "GROUP BY o.status " +
            "ORDER BY o.status", nativeQuery = true)
    List<Object[]> getOrderStatusStatistics();

    /**
     * Get daily order statistics
     * Returns: order_date, order_count, total_amount, avg_amount
     */
    @Query(value = "SELECT CONVERT(DATE, o.order_date) as order_date, " +
            "COUNT(o.order_id) as order_count, " +
            "SUM(o.amount) as total_amount, " +
            "AVG(o.amount) as avg_amount " +
            "FROM orders o " +
            "GROUP BY CONVERT(DATE, o.order_date) " +
            "ORDER BY order_date DESC", nativeQuery = true)
    List<Object[]> getDailyOrderStatistics();

    /**
     * Get monthly order statistics
     * Returns: year, month, order_count, total_amount, avg_amount
     */
    @Query(value = "SELECT YEAR(o.order_date) as year, " +
            "MONTH(o.order_date) as month, " +
            "COUNT(o.order_id) as order_count, " +
            "SUM(o.amount) as total_amount, " +
            "AVG(o.amount) as avg_amount " +
            "FROM orders o " +
            "GROUP BY YEAR(o.order_date), MONTH(o.order_date) " +
            "ORDER BY year DESC, month DESC", nativeQuery = true)
    List<Object[]> getMonthlyOrderStatistics();

    /**
     * Get yearly order statistics
     * Returns: year, order_count, total_amount, avg_amount
     */
    @Query(value = "SELECT YEAR(o.order_date) as year, " +
            "COUNT(o.order_id) as order_count, " +
            "SUM(o.amount) as total_amount, " +
            "AVG(o.amount) as avg_amount " +
            "FROM orders o " +
            "GROUP BY YEAR(o.order_date) " +
            "ORDER BY year DESC", nativeQuery = true)
    List<Object[]> getYearlyOrderStatistics();

    /**
     * Get quarterly order statistics
     * Returns: year, quarter, order_count, total_amount, avg_amount
     */
    @Query(value = "SELECT YEAR(o.order_date) as year, " +
            "CASE " +
            "WHEN MONTH(o.order_date) BETWEEN 1 AND 3 THEN 1 " +
            "WHEN MONTH(o.order_date) BETWEEN 4 AND 6 THEN 2 " +
            "WHEN MONTH(o.order_date) BETWEEN 7 AND 9 THEN 3 " +
            "WHEN MONTH(o.order_date) BETWEEN 10 AND 12 THEN 4 " +
            "END as quarter, " +
            "COUNT(o.order_id) as order_count, " +
            "SUM(o.amount) as total_amount, " +
            "AVG(o.amount) as avg_amount " +
            "FROM orders o " +
            "GROUP BY YEAR(o.order_date), CASE " +
            "WHEN MONTH(o.order_date) BETWEEN 1 AND 3 THEN 1 " +
            "WHEN MONTH(o.order_date) BETWEEN 4 AND 6 THEN 2 " +
            "WHEN MONTH(o.order_date) BETWEEN 7 AND 9 THEN 3 " +
            "WHEN MONTH(o.order_date) BETWEEN 10 AND 12 THEN 4 " +
            "END " +
            "ORDER BY year DESC, quarter DESC", nativeQuery = true)
    List<Object[]> getQuarterlyOrderStatistics();

    /**
     * Get top customers by order value
     * Returns: user_id, name, order_count, total_spent, avg_order_value
     */
    @Query(value = "SELECT u.user_id, u.name, " +
            "COUNT(o.order_id) as order_count, " +
            "SUM(o.amount) as total_spent, " +
            "AVG(o.amount) as avg_order_value " +
            "FROM orders o " +
            "INNER JOIN [user] u ON o.user_id = u.user_id " +
            "WHERE o.status IN (1, 2) " +
            "GROUP BY u.user_id, u.name " +
            "ORDER BY total_spent DESC", nativeQuery = true)
    List<Object[]> getTopCustomersByValue();

    /**
     * Get top customers by order count
     * Returns: user_id, name, order_count, total_spent, avg_order_value
     */
    @Query(value = "SELECT u.user_id, u.name, " +
            "COUNT(o.order_id) as order_count, " +
            "SUM(o.amount) as total_spent, " +
            "AVG(o.amount) as avg_order_value " +
            "FROM orders o " +
            "INNER JOIN [user] u ON o.user_id = u.user_id " +
            "WHERE o.status IN (1, 2) " +
            "GROUP BY u.user_id, u.name " +
            "ORDER BY order_count DESC", nativeQuery = true)
    List<Object[]> getTopCustomersByCount();

    /**
     * Get order completion rate statistics
     * Returns: total_orders, completed_orders, cancelled_orders, completion_rate
     */
    @Query(value = "SELECT COUNT(*) as total_orders, " +
            "COUNT(CASE WHEN status IN (1, 2) THEN 1 END) as completed_orders, " +
            "COUNT(CASE WHEN status = 3 THEN 1 END) as cancelled_orders, " +
            "ROUND(COUNT(CASE WHEN status IN (1, 2) THEN 1 END) * 100.0 / COUNT(*), 2) as completion_rate " +
            "FROM orders", nativeQuery = true)
    List<Object[]> getOrderCompletionRateStatistics();

    /**
     * Get average order processing time
     * Returns: status, avg_processing_days
     */
    @Query(value = "SELECT o.status, " +
            "AVG(DATEDIFF(day, o.order_date, GETDATE())) as avg_processing_days " +
            "FROM orders o " +
            "WHERE o.status IN (1, 2) " +
            "GROUP BY o.status", nativeQuery = true)
    List<Object[]> getAverageOrderProcessingTime();
}
