package vn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.entity.Order;
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
     * Get product sales statistics with calculated fields
     * Returns: productId, productName, totalQuantity, totalRevenue, avgPrice, minPrice, maxPrice
     */
    @Query("SELECT od.product.productId, od.product.productName, SUM(od.quantity) as totalQuantity, " +
           "SUM(od.totalPrice) as totalRevenue, " +
           "AVG(od.unitPrice) as avgPrice, " +
           "MIN(od.unitPrice) as minPrice, " +
           "MAX(od.unitPrice) as maxPrice " +
           "FROM OrderDetail od GROUP BY od.product.productId, od.product.productName ORDER BY totalRevenue DESC")
    List<Object[]> getProductSalesStatistics();

    /**
     * Get category sales statistics with calculated fields
     * Returns: categoryName, totalQuantity, totalRevenue, avgPrice, minPrice, maxPrice
     */
    @Query("SELECT c.categoryName, SUM(od.quantity) as totalQuantity, SUM(od.totalPrice) as totalRevenue, " +
           "AVG(od.unitPrice) as avgPrice, " +
           "MIN(od.unitPrice) as minPrice, " +
           "MAX(od.unitPrice) as maxPrice " +
           "FROM OrderDetail od JOIN od.product p JOIN p.category c " +
           "GROUP BY c.categoryId, c.categoryName ORDER BY totalRevenue DESC")
    List<Object[]> getCategorySalesStatistics();

    /**
     * Get brand sales statistics with calculated fields
     * Returns: brandName, totalQuantity, totalRevenue, avgPrice, minPrice, maxPrice
     */
    @Query("SELECT b.brandName, SUM(od.quantity) as totalQuantity, SUM(od.totalPrice) as totalRevenue, " +
           "AVG(od.unitPrice) as avgPrice, " +
           "MIN(od.unitPrice) as minPrice, " +
           "MAX(od.unitPrice) as maxPrice " +
           "FROM OrderDetail od JOIN od.product p JOIN p.brand b " +
           "GROUP BY b.brandId, b.brandName ORDER BY totalRevenue DESC")
    List<Object[]> getBrandSalesStatistics();

    /**
     * Get monthly sales statistics with calculated fields
     * Returns: year, month, totalQuantity, totalRevenue, avgPrice, minPrice, maxPrice
     */
    @Query("SELECT YEAR(od.order.orderDate) as year, MONTH(od.order.orderDate) as month, " +
           "SUM(od.quantity) as totalQuantity, SUM(od.totalPrice) as totalRevenue, " +
           "AVG(od.unitPrice) as avgPrice, " +
           "MIN(od.unitPrice) as minPrice, " +
           "MAX(od.unitPrice) as maxPrice " +
           "FROM OrderDetail od WHERE od.order.status = 'DELIVERED' " +
           "GROUP BY YEAR(od.order.orderDate), MONTH(od.order.orderDate) ORDER BY year, month")
    List<Object[]> getMonthlySalesStatistics();

    /**
     * Get quarterly sales statistics with calculated fields
     * Returns: year, quarter, totalQuantity, totalRevenue, avgPrice, minPrice, maxPrice
     */
    @Query("SELECT YEAR(od.order.orderDate) as year, " +
           "CASE WHEN MONTH(od.order.orderDate) BETWEEN 1 AND 3 THEN 1 " +
           "     WHEN MONTH(od.order.orderDate) BETWEEN 4 AND 6 THEN 2 " +
           "     WHEN MONTH(od.order.orderDate) BETWEEN 7 AND 9 THEN 3 " +
           "     ELSE 4 END as quarter, " +
           "SUM(od.quantity) as totalQuantity, SUM(od.totalPrice) as totalRevenue, " +
           "AVG(od.unitPrice) as avgPrice, " +
           "MIN(od.unitPrice) as minPrice, " +
           "MAX(od.unitPrice) as maxPrice " +
           "FROM OrderDetail od WHERE od.order.status = 'DELIVERED' " +
           "GROUP BY YEAR(od.order.orderDate), " +
           "CASE WHEN MONTH(od.order.orderDate) BETWEEN 1 AND 3 THEN 1 " +
           "     WHEN MONTH(od.order.orderDate) BETWEEN 4 AND 6 THEN 2 " +
           "     WHEN MONTH(od.order.orderDate) BETWEEN 7 AND 9 THEN 3 " +
           "     ELSE 4 END ORDER BY year, quarter")
    List<Object[]> getQuarterlySalesStatistics();

    /**
     * Get yearly sales statistics with calculated fields
     * Returns: year, totalQuantity, totalRevenue, avgPrice, minPrice, maxPrice
     */
    @Query("SELECT YEAR(od.order.orderDate) as year, " +
           "SUM(od.quantity) as totalQuantity, SUM(od.totalPrice) as totalRevenue, " +
           "AVG(od.unitPrice) as avgPrice, " +
           "MIN(od.unitPrice) as minPrice, " +
           "MAX(od.unitPrice) as maxPrice " +
           "FROM OrderDetail od WHERE od.order.status = 'DELIVERED' " +
           "GROUP BY YEAR(od.order.orderDate) ORDER BY year")
    List<Object[]> getYearlySalesStatistics();

    /**
     * Get user statistics with calculated fields
     * Returns: userId, name, totalOrders, totalRevenue, avgOrderValue, lastPurchaseDate, customerStatus
     */
    @Query("SELECT u.userId, u.name, COUNT(DISTINCT od.order) as totalOrders, " +
           "SUM(od.totalPrice) as totalRevenue, " +
           "AVG(od.order.totalAmount) as avgOrderValue, " +
           "MAX(od.order.orderDate) as lastPurchaseDate, " +
           "CASE WHEN COUNT(DISTINCT od.order) >= 10 THEN 'Khách hàng thường xuyên' " +
           "     WHEN COUNT(DISTINCT od.order) >= 3 THEN 'Khách hàng thỉnh thoảng' " +
           "     ELSE 'Khách hàng mới' END as customerStatus " +
           "FROM OrderDetail od JOIN od.order o JOIN o.user u " +
           "WHERE o.status = 'DELIVERED' " +
           "GROUP BY u.userId, u.name, u.email ORDER BY totalRevenue DESC")
    List<Object[]> getUserStatistics();

    @Query("SELECT COUNT(DISTINCT od.order.orderId) FROM OrderDetail od " +
           "WHERE od.product.shop.shopId = :shopId " +
           "AND od.order.status = vn.entity.Order$OrderStatus.DELIVERED")
    Long countDistinctOrdersByShop(@Param("shopId") Long shopId);

    @Query("SELECT SUM(od.totalPrice) FROM OrderDetail od " +
           "WHERE od.product.shop.shopId = :shopId " +
           "AND od.order.status = vn.entity.Order$OrderStatus.DELIVERED")
    Double sumRevenueByShop(@Param("shopId") Long shopId);

    @Query("SELECT COUNT(DISTINCT od.order.orderId) FROM OrderDetail od " +
           "WHERE od.product.shop.shopId = :shopId " +
           "AND od.order.status = :status")
    Long countDistinctOrdersByShopAndStatus(@Param("shopId") Long shopId,
                                            @Param("status") Order.OrderStatus status);

    @Query("SELECT SUM(od.quantity) FROM OrderDetail od " +
           "WHERE od.product.shop.shopId = :shopId " +
           "AND od.order.status = :status")
    Long sumQuantityByShopAndStatus(@Param("shopId") Long shopId,
                                    @Param("status") Order.OrderStatus status);
    
    // ========== FILTERED STATISTICS (WITH DATE RANGE) ==========
    
    /**
     * Get product sales statistics with date filter
     */
    @Query("SELECT od.product.productId, od.product.productName, SUM(od.quantity) as totalQuantity, " +
           "SUM(od.totalPrice) as totalRevenue, " +
           "AVG(od.unitPrice) as avgPrice, " +
           "MIN(od.unitPrice) as minPrice, " +
           "MAX(od.unitPrice) as maxPrice " +
           "FROM OrderDetail od WHERE od.order.orderDate BETWEEN :startDate AND :endDate " +
           "GROUP BY od.product.productId, od.product.productName ORDER BY totalRevenue DESC")
    List<Object[]> getProductSalesStatisticsByDateRange(@Param("startDate") java.time.LocalDateTime startDate, 
                                                         @Param("endDate") java.time.LocalDateTime endDate);

    /**
     * Get category sales statistics with date filter
     */
    @Query("SELECT c.categoryName, SUM(od.quantity) as totalQuantity, SUM(od.totalPrice) as totalRevenue, " +
           "AVG(od.unitPrice) as avgPrice, " +
           "MIN(od.unitPrice) as minPrice, " +
           "MAX(od.unitPrice) as maxPrice " +
           "FROM OrderDetail od JOIN od.product p JOIN p.category c " +
           "WHERE od.order.orderDate BETWEEN :startDate AND :endDate " +
           "GROUP BY c.categoryId, c.categoryName ORDER BY totalRevenue DESC")
    List<Object[]> getCategorySalesStatisticsByDateRange(@Param("startDate") java.time.LocalDateTime startDate, 
                                                          @Param("endDate") java.time.LocalDateTime endDate);

    /**
     * Get brand sales statistics with date filter
     */
    @Query("SELECT b.brandName, SUM(od.quantity) as totalQuantity, SUM(od.totalPrice) as totalRevenue, " +
           "AVG(od.unitPrice) as avgPrice, " +
           "MIN(od.unitPrice) as minPrice, " +
           "MAX(od.unitPrice) as maxPrice " +
           "FROM OrderDetail od JOIN od.product p JOIN p.brand b " +
           "WHERE od.order.orderDate BETWEEN :startDate AND :endDate " +
           "GROUP BY b.brandId, b.brandName ORDER BY totalRevenue DESC")
    List<Object[]> getBrandSalesStatisticsByDateRange(@Param("startDate") java.time.LocalDateTime startDate, 
                                                       @Param("endDate") java.time.LocalDateTime endDate);

    /**
     * Get monthly sales statistics with date filter
     */
    @Query("SELECT YEAR(od.order.orderDate) as year, MONTH(od.order.orderDate) as month, " +
           "SUM(od.quantity) as totalQuantity, SUM(od.totalPrice) as totalRevenue, " +
           "AVG(od.unitPrice) as avgPrice, " +
           "MIN(od.unitPrice) as minPrice, " +
           "MAX(od.unitPrice) as maxPrice " +
           "FROM OrderDetail od WHERE od.order.status = 'DELIVERED' " +
           "AND od.order.orderDate BETWEEN :startDate AND :endDate " +
           "GROUP BY YEAR(od.order.orderDate), MONTH(od.order.orderDate) ORDER BY year, month")
    List<Object[]> getMonthlySalesStatisticsByDateRange(@Param("startDate") java.time.LocalDateTime startDate, 
                                                         @Param("endDate") java.time.LocalDateTime endDate);

    /**
     * Get quarterly sales statistics with date filter
     */
    @Query("SELECT YEAR(od.order.orderDate) as year, " +
           "CASE WHEN MONTH(od.order.orderDate) BETWEEN 1 AND 3 THEN 1 " +
           "     WHEN MONTH(od.order.orderDate) BETWEEN 4 AND 6 THEN 2 " +
           "     WHEN MONTH(od.order.orderDate) BETWEEN 7 AND 9 THEN 3 " +
           "     ELSE 4 END as quarter, " +
           "SUM(od.quantity) as totalQuantity, SUM(od.totalPrice) as totalRevenue, " +
           "AVG(od.unitPrice) as avgPrice, " +
           "MIN(od.unitPrice) as minPrice, " +
           "MAX(od.unitPrice) as maxPrice " +
           "FROM OrderDetail od WHERE od.order.status = 'DELIVERED' " +
           "AND od.order.orderDate BETWEEN :startDate AND :endDate " +
           "GROUP BY YEAR(od.order.orderDate), " +
           "CASE WHEN MONTH(od.order.orderDate) BETWEEN 1 AND 3 THEN 1 " +
           "     WHEN MONTH(od.order.orderDate) BETWEEN 4 AND 6 THEN 2 " +
           "     WHEN MONTH(od.order.orderDate) BETWEEN 7 AND 9 THEN 3 " +
           "     ELSE 4 END ORDER BY year, quarter")
    List<Object[]> getQuarterlySalesStatisticsByDateRange(@Param("startDate") java.time.LocalDateTime startDate, 
                                                           @Param("endDate") java.time.LocalDateTime endDate);

    /**
     * Get yearly sales statistics with date filter
     */
    @Query("SELECT YEAR(od.order.orderDate) as year, " +
           "SUM(od.quantity) as totalQuantity, SUM(od.totalPrice) as totalRevenue, " +
           "AVG(od.unitPrice) as avgPrice, " +
           "MIN(od.unitPrice) as minPrice, " +
           "MAX(od.unitPrice) as maxPrice " +
           "FROM OrderDetail od WHERE od.order.status = 'DELIVERED' " +
           "AND od.order.orderDate BETWEEN :startDate AND :endDate " +
           "GROUP BY YEAR(od.order.orderDate) ORDER BY year")
    List<Object[]> getYearlySalesStatisticsByDateRange(@Param("startDate") java.time.LocalDateTime startDate, 
                                                        @Param("endDate") java.time.LocalDateTime endDate);

    /**
     * Get user statistics with date filter
     */
    @Query("SELECT u.userId, u.name, COUNT(DISTINCT od.order) as totalOrders, " +
           "SUM(od.totalPrice) as totalRevenue, " +
           "AVG(od.order.totalAmount) as avgOrderValue, " +
           "MAX(od.order.orderDate) as lastPurchaseDate, " +
           "CASE WHEN COUNT(DISTINCT od.order) >= 10 THEN 'Khách hàng thường xuyên' " +
           "     WHEN COUNT(DISTINCT od.order) >= 3 THEN 'Khách hàng thỉnh thoảng' " +
           "     ELSE 'Khách hàng mới' END as customerStatus " +
           "FROM OrderDetail od JOIN od.order o JOIN o.user u " +
           "WHERE o.status = 'DELIVERED' AND o.orderDate BETWEEN :startDate AND :endDate " +
           "GROUP BY u.userId, u.name, u.email ORDER BY totalRevenue DESC")
    List<Object[]> getUserStatisticsByDateRange(@Param("startDate") java.time.LocalDateTime startDate, 
                                                 @Param("endDate") java.time.LocalDateTime endDate);
}
