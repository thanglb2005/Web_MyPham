package vn.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.entity.Order;
import vn.entity.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {


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
     * Get monthly order statistics for a specific shop
     * Returns: [year, month, orderCount, totalRevenue]
     */
    @Query("SELECT YEAR(o.orderDate) as year, MONTH(o.orderDate) as month, " +
           "COUNT(o) as orderCount, SUM(o.totalAmount) as totalRevenue " +
           "FROM Order o " +
           "WHERE o.status = 'DELIVERED' AND o.shop.shopId = :shopId " +
           "GROUP BY YEAR(o.orderDate), MONTH(o.orderDate) " +
           "ORDER BY year, month")
    List<Object[]> getMonthlyOrderStatisticsByShop(@Param("shopId") Long shopId);

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
     * Find orders by user
     */
    List<Order> findByUserOrderByOrderDateDesc(User user);

    /**
     * Find orders by user with pagination
     */
    @Query("SELECT o FROM Order o WHERE o.user = :user ORDER BY o.orderDate DESC")
    Page<Order> findByUserOrderByOrderDateDescPageable(@Param("user") User user, Pageable pageable);

    /**
     * Find orders by user and status with pagination
     */
    Page<Order> findByUserAndStatusOrderByOrderDateDesc(User user, Order.OrderStatus status, Pageable pageable);

    /**
     * Count orders by user and status
     */
    long countByUserAndStatus(User user, Order.OrderStatus status);

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


    // ===== VENDOR ORDER MANAGEMENT METHODS =====

    /**
     * Find orders by shop IDs with status filter
     */
    @Query("SELECT o FROM Order o WHERE o.shop.shopId IN :shopIds " +
           "AND (:status IS NULL OR o.status = :status) " +
           "ORDER BY o.orderDate DESC")
    Page<Order> findByShopIdInAndStatus(@Param("shopIds") List<Long> shopIds, 
                                       @Param("status") Order.OrderStatus status, 
                                       Pageable pageable);

    /**
     * Find orders by shop IDs with search term
     */
    @Query("SELECT o FROM Order o WHERE o.shop.shopId IN :shopIds " +
           "AND (:search IS NULL OR STR(o.orderId) LIKE %:search%) " +
           "ORDER BY o.orderDate DESC")
    Page<Order> findByShopIdInAndOrderIdContaining(@Param("shopIds") List<Long> shopIds, 
                                                  @Param("search") String search, 
                                                  Pageable pageable);

    /**
     * Find orders by shop IDs with status and search term
     */
    @Query("SELECT o FROM Order o WHERE o.shop.shopId IN :shopIds " +
           "AND (:status IS NULL OR o.status = :status) " +
           "AND (:search IS NULL OR STR(o.orderId) LIKE %:search%) " +
           "ORDER BY o.orderDate DESC")
    Page<Order> findByShopIdInAndStatusAndOrderIdContaining(@Param("shopIds") List<Long> shopIds, 
                                                          @Param("status") Order.OrderStatus status, 
                                                          @Param("search") String search, 
                                                          Pageable pageable);

    /**
     * Find orders by shop IDs only
     */
    @Query("SELECT o FROM Order o WHERE o.shop.shopId IN :shopIds " +
           "ORDER BY o.orderDate DESC")
    Page<Order> findByShopIdIn(@Param("shopIds") List<Long> shopIds, Pageable pageable);

    // Direct queries using Order.shop mapping (does not require orderDetails)
    @Query("SELECT o FROM Order o WHERE o.shop.shopId IN :shopIds AND (:status IS NULL OR o.status = :status) ORDER BY o.orderDate DESC")
    Page<Order> findByShopIdInAndStatusDirect(@Param("shopIds") List<Long> shopIds,
                                              @Param("status") Order.OrderStatus status,
                                              Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.shop.shopId IN :shopIds ORDER BY o.orderDate DESC")
    Page<Order> findByShopIdInDirect(@Param("shopIds") List<Long> shopIds, Pageable pageable);

    /**
     * Find order by ID and shop IDs (for vendor access control)
     */
    @Query("SELECT o FROM Order o WHERE o.orderId = :orderId AND o.shop.shopId IN :shopIds")
    Optional<Order> findByIdAndShopIdIn(@Param("orderId") Long orderId, @Param("shopIds") List<Long> shopIds);

    // ===== Direct versions using Order.shop mapping =====
    @Query("SELECT COUNT(o) FROM Order o WHERE o.shop.shopId IN :shopIds")
    Long countByShopIdInDirect(@Param("shopIds") List<Long> shopIds);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.shop.shopId IN :shopIds AND (:status IS NULL OR o.status = :status)")
    Long countByShopIdInAndStatusDirect(@Param("shopIds") List<Long> shopIds,
                                        @Param("status") Order.OrderStatus status);

    @Query("SELECT o FROM Order o WHERE o.orderId = :orderId AND o.shop.shopId IN :shopIds")
    Optional<Order> findByIdAndShopIdInDirect(@Param("orderId") Long orderId, @Param("shopIds") List<Long> shopIds);

    @Query("SELECT o FROM Order o WHERE o.shop.shopId IN :shopIds " +
           "AND (:search IS NULL OR STR(o.orderId) LIKE %:search%) " +
           "ORDER BY o.orderDate DESC")
    Page<Order> findByShopIdInAndOrderIdContainingDirect(@Param("shopIds") List<Long> shopIds,
                                                         @Param("search") String search,
                                                         Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.shop.shopId IN :shopIds " +
           "AND (:status IS NULL OR o.status = :status) " +
           "AND (:search IS NULL OR STR(o.orderId) LIKE %:search%) " +
           "ORDER BY o.orderDate DESC")
    Page<Order> findByShopIdInAndStatusAndOrderIdContainingDirect(@Param("shopIds") List<Long> shopIds,
                                                                  @Param("status") Order.OrderStatus status,
                                                                  @Param("search") String search,
                                                                  Pageable pageable);

    @Query("SELECT DISTINCT o FROM Order o " +
           "LEFT JOIN FETCH o.orderDetails od " +
           "LEFT JOIN FETCH od.product p " +
           "WHERE o.orderId = :orderId AND o.shop.shopId IN :shopIds")
    Optional<Order> findWithDetailsByIdAndShopIds(@Param("orderId") Long orderId,
                                                  @Param("shopIds") List<Long> shopIds);

    // ===== Revenue by shop (DELIVERED) =====
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.status = 'DELIVERED' AND o.shop.shopId = :shopId")
    Double sumDeliveredRevenueByShopId(@Param("shopId") Long shopId);

    /**
     * Count orders by shop IDs and status
     */
    @Query("SELECT COUNT(o) FROM Order o WHERE o.shop.shopId IN :shopIds " +
           "AND (:status IS NULL OR o.status = :status)")
    Long countByShopIdInAndStatus(@Param("shopIds") List<Long> shopIds, @Param("status") Order.OrderStatus status);

    /**
     * Count orders by shop IDs
     */
    @Query("SELECT COUNT(o) FROM Order o WHERE o.shop.shopId IN :shopIds")
    Long countByShopIdIn(@Param("shopIds") List<Long> shopIds);

    @Query("SELECT o.status, COUNT(o) FROM Order o WHERE o.shop.shopId IN :shopIds GROUP BY o.status")
    List<Object[]> countOrdersByStatus(@Param("shopIds") List<Long> shopIds);

    Page<Order> findByShopShopIdInOrderByOrderDateDesc(List<Long> shopIds, Pageable pageable);

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

    /**
     * Find orders that are overdue (past estimated delivery date and still shipping or already marked overdue)
     */
    @Query("SELECT o FROM Order o " +
           "WHERE (o.status = 'SHIPPING' OR o.status = 'OVERDUE') " +
           "AND o.estimatedDeliveryDate IS NOT NULL " +
           "AND o.estimatedDeliveryDate < :currentTime " +
           "ORDER BY o.estimatedDeliveryDate ASC")
    List<Order> findOverdueOrders(@Param("currentTime") java.time.LocalDateTime currentTime);

    /**
     * Find overdue orders for a specific shipper
     */
    @Query("SELECT o FROM Order o " +
           "WHERE o.shipper = :shipper " +
           "AND (o.status = 'SHIPPING' OR o.status = 'OVERDUE') " +
           "AND o.estimatedDeliveryDate IS NOT NULL " +
           "AND o.estimatedDeliveryDate < :currentTime " +
           "ORDER BY o.estimatedDeliveryDate ASC")
    List<Order> findOverdueOrdersByShipper(@Param("shipper") User shipper, 
                                          @Param("currentTime") java.time.LocalDateTime currentTime);

    /**
     * Count overdue orders for a specific shipper
     */
    @Query("SELECT COUNT(o) FROM Order o " +
           "WHERE o.shipper = :shipper " +
           "AND (o.status = 'SHIPPING' OR o.status = 'OVERDUE') " +
           "AND o.estimatedDeliveryDate IS NOT NULL " +
           "AND o.estimatedDeliveryDate < :currentTime")
    long countOverdueOrdersByShipper(@Param("shipper") User shipper, 
                                    @Param("currentTime") java.time.LocalDateTime currentTime);

    /**
     * Find orders that need to be marked as overdue (currently SHIPPING but past estimated delivery date)
     */
    @Query("SELECT o FROM Order o " +
           "WHERE o.status = 'SHIPPING' " +
           "AND o.estimatedDeliveryDate IS NOT NULL " +
           "AND o.estimatedDeliveryDate < :currentTime " +
           "ORDER BY o.estimatedDeliveryDate ASC")
    List<Order> findOrdersToMarkOverdue(@Param("currentTime") java.time.LocalDateTime currentTime);
}
