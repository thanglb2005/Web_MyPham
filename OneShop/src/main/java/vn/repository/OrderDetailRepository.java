package vn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.entity.OrderDetail;

import java.util.List;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {

    /**
     * Find order details by order ID
     */
    @Query("SELECT od FROM OrderDetail od WHERE od.order.orderId = :orderId ORDER BY od.orderDetailId ASC")
    List<OrderDetail> findByOrderId(@Param("orderId") Long orderId);

    /**
     * Find order details by product ID
     */
    @Query("SELECT od FROM OrderDetail od WHERE od.product.productId = :productId ORDER BY od.orderDetailId DESC")
    List<OrderDetail> findByProductId(@Param("productId") Long productId);

    /**
     * Get product sales statistics
     */
    @Query("SELECT od.product.productName, SUM(od.quantity) as totalQuantity, SUM(od.totalPrice) as totalRevenue " +
           "FROM OrderDetail od GROUP BY od.product.productId, od.product.productName ORDER BY totalRevenue DESC")
    List<Object[]> getProductSalesStatistics();

    /**
     * Get category sales statistics
     */
    @Query("SELECT c.categoryName, SUM(od.quantity) as totalQuantity, SUM(od.totalPrice) as totalRevenue " +
           "FROM OrderDetail od JOIN od.product p JOIN p.category c GROUP BY c.categoryId, c.categoryName ORDER BY totalRevenue DESC")
    List<Object[]> getCategorySalesStatistics();

    /**
     * Get brand sales statistics
     */
    @Query("SELECT b.brandName, SUM(od.quantity) as totalQuantity, SUM(od.totalPrice) as totalRevenue " +
           "FROM OrderDetail od JOIN od.product p JOIN p.brand b GROUP BY b.brandId, b.brandName ORDER BY totalRevenue DESC")
    List<Object[]> getBrandSalesStatistics();

    /**
     * Get monthly sales statistics
     */
    @Query("SELECT YEAR(od.order.orderDate) as year, MONTH(od.order.orderDate) as month, " +
           "SUM(od.quantity) as totalQuantity, SUM(od.totalPrice) as totalRevenue " +
           "FROM OrderDetail od WHERE od.order.status = 'DELIVERED' " +
           "GROUP BY YEAR(od.order.orderDate), MONTH(od.order.orderDate) ORDER BY year, month")
    List<Object[]> getMonthlySalesStatistics();

    /**
     * Get quarterly sales statistics
     */
    @Query("SELECT YEAR(od.order.orderDate) as year, " +
           "CASE WHEN MONTH(od.order.orderDate) BETWEEN 1 AND 3 THEN 1 " +
           "     WHEN MONTH(od.order.orderDate) BETWEEN 4 AND 6 THEN 2 " +
           "     WHEN MONTH(od.order.orderDate) BETWEEN 7 AND 9 THEN 3 " +
           "     ELSE 4 END as quarter, " +
           "SUM(od.quantity) as totalQuantity, SUM(od.totalPrice) as totalRevenue " +
           "FROM OrderDetail od WHERE od.order.status = 'DELIVERED' " +
           "GROUP BY YEAR(od.order.orderDate), " +
           "CASE WHEN MONTH(od.order.orderDate) BETWEEN 1 AND 3 THEN 1 " +
           "     WHEN MONTH(od.order.orderDate) BETWEEN 4 AND 6 THEN 2 " +
           "     WHEN MONTH(od.order.orderDate) BETWEEN 7 AND 9 THEN 3 " +
           "     ELSE 4 END ORDER BY year, quarter")
    List<Object[]> getQuarterlySalesStatistics();

    /**
     * Get yearly sales statistics
     */
    @Query("SELECT YEAR(od.order.orderDate) as year, " +
           "SUM(od.quantity) as totalQuantity, SUM(od.totalPrice) as totalRevenue " +
           "FROM OrderDetail od WHERE od.order.status = 'DELIVERED' " +
           "GROUP BY YEAR(od.order.orderDate) ORDER BY year")
    List<Object[]> getYearlySalesStatistics();

    /**
     * Get user statistics
     */
    @Query("SELECT u.name, u.email, COUNT(DISTINCT od.order) as totalOrders, " +
           "SUM(od.quantity) as totalQuantity, SUM(od.totalPrice) as totalRevenue " +
           "FROM OrderDetail od JOIN od.order o JOIN o.user u " +
           "WHERE o.status = 'DELIVERED' " +
           "GROUP BY u.userId, u.name, u.email ORDER BY totalRevenue DESC")
    List<Object[]> getUserStatistics();
}