package vn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.entity.OrderDetail;

import java.util.List;

/**
 * Repository for OrderDetail entity
 * @author OneShop Team
 */
@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {

    @Query(value = "SELECT * FROM order_details WHERE order_id = ?1", nativeQuery = true)
    List<OrderDetail> findByOrderId(Long orderId);
    
    @Query(value = "SELECT * FROM order_details WHERE product_id = ?1", nativeQuery = true)
    List<OrderDetail> findByProductId(Long productId);

    // ========== STATISTICS QUERIES ==========

    /**
     * Statistics by product sold
     * Returns: product_id, product_name, total_quantity, total_revenue, avg_price, min_price, max_price
     */
    @Query(value = "SELECT p.product_id, " +
            "p.product_name, " +
            "SUM(od.quantity) as total_quantity, " +
            "SUM(od.quantity * od.price) as total_revenue, " +
            "AVG(od.price) as avg_price, " +
            "MIN(od.price) as min_price, " +
            "MAX(od.price) as max_price " +
            "FROM order_details od " +
            "INNER JOIN products p ON od.product_id = p.product_id " +
            "INNER JOIN orders o ON od.order_id = o.order_id " +
            "WHERE o.status IN (1, 2) " + // Only confirmed and delivered orders
            "GROUP BY p.product_id, p.product_name " +
            "ORDER BY total_revenue DESC", nativeQuery = true)
    List<Object[]> getProductSalesStatistics();

    /**
     * Statistics by category sold
     * Returns: category_name, total_quantity, total_revenue, avg_price, min_price, max_price
     */
    @Query(value = "SELECT c.category_name, " +
            "SUM(od.quantity) as total_quantity, " +
            "SUM(od.quantity * od.price) as total_revenue, " +
            "AVG(od.price) as avg_price, " +
            "MIN(od.price) as min_price, " +
            "MAX(od.price) as max_price " +
            "FROM order_details od " +
            "INNER JOIN products p ON od.product_id = p.product_id " +
            "INNER JOIN categories c ON p.category_id = c.category_id " +
            "INNER JOIN orders o ON od.order_id = o.order_id " +
            "WHERE o.status IN (1, 2) " +
            "GROUP BY c.category_id, c.category_name " +
            "ORDER BY total_revenue DESC", nativeQuery = true)
    List<Object[]> getCategorySalesStatistics();

    /**
     * Statistics by brand sold
     * Returns: brand_name, total_quantity, total_revenue, avg_price, min_price, max_price
     */
    @Query(value = "SELECT b.brand_name, " +
            "SUM(od.quantity) as total_quantity, " +
            "SUM(od.quantity * od.price) as total_revenue, " +
            "AVG(od.price) as avg_price, " +
            "MIN(od.price) as min_price, " +
            "MAX(od.price) as max_price " +
            "FROM order_details od " +
            "INNER JOIN products p ON od.product_id = p.product_id " +
            "INNER JOIN brands b ON p.brand_id = b.brand_id " +
            "INNER JOIN orders o ON od.order_id = o.order_id " +
            "WHERE o.status IN (1, 2) " +
            "GROUP BY b.brand_id, b.brand_name " +
            "ORDER BY total_revenue DESC", nativeQuery = true)
    List<Object[]> getBrandSalesStatistics();

    /**
     * Statistics of products sold by year
     * Returns: year, total_quantity, total_revenue, avg_price, min_price, max_price
     */
    @Query(value = "SELECT YEAR(o.order_date) as year, " +
            "SUM(od.quantity) as total_quantity, " +
            "SUM(od.quantity * od.price) as total_revenue, " +
            "AVG(od.price) as avg_price, " +
            "MIN(od.price) as min_price, " +
            "MAX(od.price) as max_price " +
            "FROM order_details od " +
            "INNER JOIN orders o ON od.order_id = o.order_id " +
            "WHERE o.status IN (1, 2) " +
            "GROUP BY YEAR(o.order_date) " +
            "ORDER BY year DESC", nativeQuery = true)
    List<Object[]> getYearlySalesStatistics();

    /**
     * Statistics of products sold by month
     * Returns: month, total_quantity, total_revenue, avg_price, min_price, max_price
     */
    @Query(value = "SELECT MONTH(o.order_date) as month, " +
            "SUM(od.quantity) as total_quantity, " +
            "SUM(od.quantity * od.price) as total_revenue, " +
            "AVG(od.price) as avg_price, " +
            "MIN(od.price) as min_price, " +
            "MAX(od.price) as max_price " +
            "FROM order_details od " +
            "INNER JOIN orders o ON od.order_id = o.order_id " +
            "WHERE o.status IN (1, 2) " +
            "GROUP BY MONTH(o.order_date) " +
            "ORDER BY month", nativeQuery = true)
    List<Object[]> getMonthlySalesStatistics();

    /**
     * Statistics of products sold by quarter
     * Returns: quarter, total_quantity, total_revenue, avg_price, min_price, max_price
     */
    @Query(value = "SELECT CASE " +
            "WHEN MONTH(o.order_date) BETWEEN 1 AND 3 THEN 1 " +
            "WHEN MONTH(o.order_date) BETWEEN 4 AND 6 THEN 2 " +
            "WHEN MONTH(o.order_date) BETWEEN 7 AND 9 THEN 3 " +
            "WHEN MONTH(o.order_date) BETWEEN 10 AND 12 THEN 4 " +
            "END as quarter, " +
            "SUM(od.quantity) as total_quantity, " +
            "SUM(od.quantity * od.price) as total_revenue, " +
            "AVG(od.price) as avg_price, " +
            "MIN(od.price) as min_price, " +
            "MAX(od.price) as max_price " +
            "FROM order_details od " +
            "INNER JOIN orders o ON od.order_id = o.order_id " +
            "WHERE o.status IN (1, 2) " +
            "GROUP BY CASE " +
            "WHEN MONTH(o.order_date) BETWEEN 1 AND 3 THEN 1 " +
            "WHEN MONTH(o.order_date) BETWEEN 4 AND 6 THEN 2 " +
            "WHEN MONTH(o.order_date) BETWEEN 7 AND 9 THEN 3 " +
            "WHEN MONTH(o.order_date) BETWEEN 10 AND 12 THEN 4 " +
            "END " +
            "ORDER BY quarter", nativeQuery = true)
    List<Object[]> getQuarterlySalesStatistics();

    /**
     * Statistics by user
     * Returns: user_id, name, total_orders, total_spent, avg_order_value
     */
    @Query(value = "SELECT u.user_id, u.name, " +
            "COUNT(DISTINCT o.order_id) as total_orders, " +
            "SUM(o.amount) as total_spent, " +
            "AVG(o.amount) as avg_order_value " +
            "FROM orders o " +
            "INNER JOIN [user] u ON o.user_id = u.user_id " +
            "WHERE o.status IN (1, 2) " +
            "GROUP BY u.user_id, u.name " +
            "ORDER BY total_spent DESC", nativeQuery = true)
    List<Object[]> getUserStatistics();

    /**
     * Statistics by date range
     * Returns: date, total_quantity, total_revenue, order_count
     */
    @Query(value = "SELECT CONVERT(DATE, o.order_date) as order_date, " +
            "SUM(od.quantity) as total_quantity, " +
            "SUM(od.quantity * od.price) as total_revenue, " +
            "COUNT(DISTINCT o.order_id) as order_count " +
            "FROM order_details od " +
            "INNER JOIN orders o ON od.order_id = o.order_id " +
            "WHERE o.status IN (1, 2) " +
            "GROUP BY CONVERT(DATE, o.order_date) " +
            "ORDER BY order_date DESC", nativeQuery = true)
    List<Object[]> getDailySalesStatistics();

    /**
     * Statistics by product category and brand combination
     * Returns: category_name, brand_name, total_quantity, total_revenue
     */
    @Query(value = "SELECT c.category_name, b.brand_name, " +
            "SUM(od.quantity) as total_quantity, " +
            "SUM(od.quantity * od.price) as total_revenue " +
            "FROM order_details od " +
            "INNER JOIN products p ON od.product_id = p.product_id " +
            "INNER JOIN categories c ON p.category_id = c.category_id " +
            "INNER JOIN brands b ON p.brand_id = b.brand_id " +
            "INNER JOIN orders o ON od.order_id = o.order_id " +
            "WHERE o.status IN (1, 2) " +
            "GROUP BY c.category_id, c.category_name, b.brand_id, b.brand_name " +
            "ORDER BY total_revenue DESC", nativeQuery = true)
    List<Object[]> getCategoryBrandStatistics();
}
