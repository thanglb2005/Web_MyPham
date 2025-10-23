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
     * Find order details with product information by order ID
     * Returns: [orderDetailId, productName, quantity, unitPrice, totalPrice, productImage, shopName]
     */
    @Query("SELECT od.orderDetailId, od.product.productName, od.quantity, od.unitPrice, od.totalPrice, od.product.productImage, od.product.shop.shopName " +
           "FROM OrderDetail od WHERE od.order.orderId = :orderId ORDER BY od.orderDetailId ASC")
    List<Object[]> findOrderDetailsByOrderId(@Param("orderId") Long orderId);

    /**
     * Eagerly load product and shop to avoid lazy-loading issues in views
     */
    @Query("SELECT DISTINCT od FROM OrderDetail od " +
           "LEFT JOIN FETCH od.product p " +
           "LEFT JOIN FETCH p.shop s " +
           "WHERE od.order.orderId = :orderId ORDER BY od.orderDetailId ASC")
    List<OrderDetail> findByOrderIdWithProductAndShop(@Param("orderId") Long orderId);

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
    @Query("SELECT u.userId, u.name, u.email, " +
           "COUNT(DISTINCT od.order) as totalOrders, " +
           "SUM(od.totalPrice) as totalRevenue, " +
           "AVG(od.order.totalAmount) as avgOrderValue, " +
           "MAX(od.order.orderDate) as lastPurchaseDate, " +
           "CASE WHEN COUNT(DISTINCT od.order) >= 10 THEN 'Khách hàng thường xuyên' " +
           "     WHEN COUNT(DISTINCT od.order) >= 3 THEN 'Khách hàng thỉnh thoảng' " +
           "     ELSE 'Khách hàng mới' END as customerStatus " +
           "FROM OrderDetail od JOIN od.order o JOIN o.user u " +
           "WHERE o.status = vn.entity.Order$OrderStatus.DELIVERED " +
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
    @Query("SELECT u.userId, u.name, u.email, COUNT(DISTINCT od.order) as totalOrders, " +
           "SUM(od.totalPrice) as totalRevenue, " +
           "AVG(od.order.totalAmount) as avgOrderValue, " +
           "MAX(od.order.orderDate) as lastPurchaseDate, " +
           "CASE WHEN COUNT(DISTINCT od.order) >= 10 THEN 'Khách hàng thường xuyên' " +
           "     WHEN COUNT(DISTINCT od.order) >= 3 THEN 'Khách hàng thỉnh thoảng' " +
           "     ELSE 'Khách hàng mới' END as customerStatus " +
           "FROM OrderDetail od JOIN od.order o JOIN o.user u " +
           "WHERE o.status = vn.entity.Order$OrderStatus.DELIVERED AND o.orderDate BETWEEN :startDate AND :endDate " +
           "GROUP BY u.userId, u.name, u.email ORDER BY totalRevenue DESC")
    List<Object[]> getUserStatisticsByDateRange(@Param("startDate") java.time.LocalDateTime startDate, 
                                                 @Param("endDate") java.time.LocalDateTime endDate);

    // ================= SHOP FILTER SUPPORT =================
    // Products by shop
    @Query("SELECT od.product.productId, od.product.productName, SUM(od.quantity), SUM(od.totalPrice), AVG(od.unitPrice), MIN(od.unitPrice), MAX(od.unitPrice) " +
           "FROM OrderDetail od WHERE od.product.shop.shopId = :shopId GROUP BY od.product.productId, od.product.productName ORDER BY SUM(od.totalPrice) DESC")
    List<Object[]> getProductSalesStatisticsByShop(@Param("shopId") Long shopId);

    @Query("SELECT od.product.productId, od.product.productName, SUM(od.quantity), SUM(od.totalPrice), AVG(od.unitPrice), MIN(od.unitPrice), MAX(od.unitPrice) " +
           "FROM OrderDetail od WHERE od.product.shop.shopId = :shopId AND od.order.orderDate BETWEEN :startDate AND :endDate " +
           "GROUP BY od.product.productId, od.product.productName ORDER BY SUM(od.totalPrice) DESC")
    List<Object[]> getProductSalesStatisticsByShopAndDateRange(@Param("shopId") Long shopId,
                                                               @Param("startDate") java.time.LocalDateTime startDate,
                                                               @Param("endDate") java.time.LocalDateTime endDate);

    // Categories by shop
    @Query("SELECT c.categoryName, SUM(od.quantity), SUM(od.totalPrice), AVG(od.unitPrice), MIN(od.unitPrice), MAX(od.unitPrice) " +
           "FROM OrderDetail od JOIN od.product p JOIN p.category c WHERE p.shop.shopId = :shopId " +
           "GROUP BY c.categoryId, c.categoryName ORDER BY SUM(od.totalPrice) DESC")
    List<Object[]> getCategorySalesStatisticsByShop(@Param("shopId") Long shopId);

    @Query("SELECT c.categoryName, SUM(od.quantity), SUM(od.totalPrice), AVG(od.unitPrice), MIN(od.unitPrice), MAX(od.unitPrice) " +
           "FROM OrderDetail od JOIN od.product p JOIN p.category c WHERE p.shop.shopId = :shopId " +
           "AND od.order.orderDate BETWEEN :startDate AND :endDate " +
           "GROUP BY c.categoryId, c.categoryName ORDER BY SUM(od.totalPrice) DESC")
    List<Object[]> getCategorySalesStatisticsByShopAndDateRange(@Param("shopId") Long shopId,
                                                                @Param("startDate") java.time.LocalDateTime startDate,
                                                                @Param("endDate") java.time.LocalDateTime endDate);

    // Yearly by shop
    @Query("SELECT YEAR(od.order.orderDate), SUM(od.quantity), SUM(od.totalPrice), AVG(od.unitPrice), MIN(od.unitPrice), MAX(od.unitPrice) " +
           "FROM OrderDetail od WHERE od.order.status = 'DELIVERED' AND od.product.shop.shopId = :shopId " +
           "GROUP BY YEAR(od.order.orderDate) ORDER BY YEAR(od.order.orderDate)")
    List<Object[]> getYearlySalesStatisticsByShop(@Param("shopId") Long shopId);

    @Query("SELECT YEAR(od.order.orderDate), SUM(od.quantity), SUM(od.totalPrice), AVG(od.unitPrice), MIN(od.unitPrice), MAX(od.unitPrice) " +
           "FROM OrderDetail od WHERE od.order.status = 'DELIVERED' AND od.product.shop.shopId = :shopId " +
           "AND od.order.orderDate BETWEEN :startDate AND :endDate " +
           "GROUP BY YEAR(od.order.orderDate) ORDER BY YEAR(od.order.orderDate)")
    List<Object[]> getYearlySalesStatisticsByShopAndDateRange(@Param("shopId") Long shopId,
                                                              @Param("startDate") java.time.LocalDateTime startDate,
                                                              @Param("endDate") java.time.LocalDateTime endDate);

    // Monthly by shop
    @Query("SELECT YEAR(od.order.orderDate), MONTH(od.order.orderDate), SUM(od.quantity), SUM(od.totalPrice), AVG(od.unitPrice), MIN(od.unitPrice), MAX(od.unitPrice) " +
           "FROM OrderDetail od WHERE od.order.status = 'DELIVERED' AND od.product.shop.shopId = :shopId " +
           "GROUP BY YEAR(od.order.orderDate), MONTH(od.order.orderDate) ORDER BY YEAR(od.order.orderDate), MONTH(od.order.orderDate)")
    List<Object[]> getMonthlySalesStatisticsByShop(@Param("shopId") Long shopId);

    @Query("SELECT YEAR(od.order.orderDate), MONTH(od.order.orderDate), SUM(od.quantity), SUM(od.totalPrice), AVG(od.unitPrice), MIN(od.unitPrice), MAX(od.unitPrice) " +
           "FROM OrderDetail od WHERE od.order.status = 'DELIVERED' AND od.product.shop.shopId = :shopId " +
           "AND od.order.orderDate BETWEEN :startDate AND :endDate " +
           "GROUP BY YEAR(od.order.orderDate), MONTH(od.order.orderDate) ORDER BY YEAR(od.order.orderDate), MONTH(od.order.orderDate)")
    List<Object[]> getMonthlySalesStatisticsByShopAndDateRange(@Param("shopId") Long shopId,
                                                               @Param("startDate") java.time.LocalDateTime startDate,
                                                               @Param("endDate") java.time.LocalDateTime endDate);

    // Quarterly by shop
    @Query("SELECT YEAR(od.order.orderDate), CASE WHEN MONTH(od.order.orderDate) BETWEEN 1 AND 3 THEN 1 WHEN MONTH(od.order.orderDate) BETWEEN 4 AND 6 THEN 2 WHEN MONTH(od.order.orderDate) BETWEEN 7 AND 9 THEN 3 ELSE 4 END, " +
           "SUM(od.quantity), SUM(od.totalPrice), AVG(od.unitPrice), MIN(od.unitPrice), MAX(od.unitPrice) " +
           "FROM OrderDetail od WHERE od.order.status = 'DELIVERED' AND od.product.shop.shopId = :shopId " +
           "GROUP BY YEAR(od.order.orderDate), CASE WHEN MONTH(od.order.orderDate) BETWEEN 1 AND 3 THEN 1 WHEN MONTH(od.order.orderDate) BETWEEN 4 AND 6 THEN 2 WHEN MONTH(od.order.orderDate) BETWEEN 7 AND 9 THEN 3 ELSE 4 END " +
           "ORDER BY YEAR(od.order.orderDate)")
    List<Object[]> getQuarterlySalesStatisticsByShop(@Param("shopId") Long shopId);

    @Query("SELECT YEAR(od.order.orderDate), CASE WHEN MONTH(od.order.orderDate) BETWEEN 1 AND 3 THEN 1 WHEN MONTH(od.order.orderDate) BETWEEN 4 AND 6 THEN 2 WHEN MONTH(od.order.orderDate) BETWEEN 7 AND 9 THEN 3 ELSE 4 END, " +
           "SUM(od.quantity), SUM(od.totalPrice), AVG(od.unitPrice), MIN(od.unitPrice), MAX(od.unitPrice) " +
           "FROM OrderDetail od WHERE od.order.status = 'DELIVERED' AND od.product.shop.shopId = :shopId AND od.order.orderDate BETWEEN :startDate AND :endDate " +
           "GROUP BY YEAR(od.order.orderDate), CASE WHEN MONTH(od.order.orderDate) BETWEEN 1 AND 3 THEN 1 WHEN MONTH(od.order.orderDate) BETWEEN 4 AND 6 THEN 2 WHEN MONTH(od.order.orderDate) BETWEEN 7 AND 9 THEN 3 ELSE 4 END " +
           "ORDER BY YEAR(od.order.orderDate)")
    List<Object[]> getQuarterlySalesStatisticsByShopAndDateRange(@Param("shopId") Long shopId,
                                                                 @Param("startDate") java.time.LocalDateTime startDate,
                                                                 @Param("endDate") java.time.LocalDateTime endDate);

    // Users by shop
    @Query("SELECT u.userId, u.name, u.email, COUNT(DISTINCT od.order), SUM(od.totalPrice), AVG(od.order.totalAmount), MAX(od.order.orderDate), " +
           "CASE WHEN COUNT(DISTINCT od.order) >= 10 THEN 'Khách hàng thường xuyên' WHEN COUNT(DISTINCT od.order) >= 3 THEN 'Khách hàng thỉnh thoảng' ELSE 'Khách hàng mới' END " +
           "FROM OrderDetail od JOIN od.order o JOIN o.user u WHERE o.status = vn.entity.Order$OrderStatus.DELIVERED AND od.product.shop.shopId = :shopId " +
           "GROUP BY u.userId, u.name, u.email ORDER BY SUM(od.totalPrice) DESC")
    List<Object[]> getUserStatisticsByShop(@Param("shopId") Long shopId);

    @Query("SELECT u.userId, u.name, u.email, COUNT(DISTINCT od.order), SUM(od.totalPrice), AVG(od.order.totalAmount), MAX(od.order.orderDate), " +
           "CASE WHEN COUNT(DISTINCT od.order) >= 10 THEN 'Khách hàng thường xuyên' WHEN COUNT(DISTINCT od.order) >= 3 THEN 'Khách hàng thỉnh thoảng' ELSE 'Khách hàng mới' END " +
           "FROM OrderDetail od JOIN od.order o JOIN o.user u WHERE o.status = vn.entity.Order$OrderStatus.DELIVERED AND od.product.shop.shopId = :shopId AND o.orderDate BETWEEN :startDate AND :endDate " +
           "GROUP BY u.userId, u.name, u.email ORDER BY SUM(od.totalPrice) DESC")
    List<Object[]> getUserStatisticsByShopAndDateRange(@Param("shopId") Long shopId,
                                                       @Param("startDate") java.time.LocalDateTime startDate,
                                                       @Param("endDate") java.time.LocalDateTime endDate);

    // Brands by shop
    @Query("SELECT b.brandName, SUM(od.quantity), SUM(od.totalPrice), AVG(od.unitPrice), MIN(od.unitPrice), MAX(od.unitPrice) " +
           "FROM OrderDetail od JOIN od.product p JOIN p.brand b WHERE p.shop.shopId = :shopId " +
           "GROUP BY b.brandId, b.brandName ORDER BY SUM(od.totalPrice) DESC")
    List<Object[]> getBrandSalesStatisticsByShop(@Param("shopId") Long shopId);

    @Query("SELECT b.brandName, SUM(od.quantity), SUM(od.totalPrice), AVG(od.unitPrice), MIN(od.unitPrice), MAX(od.unitPrice) " +
           "FROM OrderDetail od JOIN od.product p JOIN p.brand b WHERE p.shop.shopId = :shopId AND od.order.orderDate BETWEEN :startDate AND :endDate " +
           "GROUP BY b.brandId, b.brandName ORDER BY SUM(od.totalPrice) DESC")
    List<Object[]> getBrandSalesStatisticsByShopAndDateRange(@Param("shopId") Long shopId,
                                                             @Param("startDate") java.time.LocalDateTime startDate,
                                                             @Param("endDate") java.time.LocalDateTime endDate);
}
