package vn.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.entity.Product;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    // List product by category
    @Query(value = "SELECT * FROM products WHERE category_id = ?", nativeQuery = true)
    List<Product> listProductByCategory(Long categoryId);
    
    // Top 10 product by category
    @Query(value = "SELECT * FROM products AS p WHERE p.category_id = ?", nativeQuery = true)
    List<Product> listProductByCategory10(Long categoryId);
    
    // List product new
    @Query(value = "SELECT * FROM products ORDER BY entered_date DESC", nativeQuery = true)
    List<Product> listProductNew20();
    
    // Search Product
    @Query(value = "SELECT * FROM products WHERE product_name LIKE CONCAT('%', ?1, '%')", nativeQuery = true)
    List<Product> searchProduct(String productName);
    
    // Count quantity by product
    @Query(value = "SELECT c.category_id, c.category_name, COUNT(*) AS SoLuong " +
                   "FROM products p " +
                   "JOIN categories c ON p.category_id = c.category_id " +
                   "GROUP BY c.category_id, c.category_name", nativeQuery = true)
    List<Object[]> listCategoryByProductName();
    
    // Top 20 product best sale
    @Query(value = "SELECT p.product_id, COUNT(*) AS SoLuong " +
                   "FROM order_details p " +
                   "JOIN products c ON p.product_id = c.product_id " +
                   "GROUP BY p.product_id " +
                   "ORDER BY SoLuong DESC", nativeQuery = true)
    List<Object[]> bestSaleProduct20();
    
    @Query(value = "SELECT * FROM products WHERE product_id IN (:ids)", nativeQuery = true)
    List<Product> findByInventoryIds(@Param("ids") List<Long> listProductId);
    
    // Additional methods for admin management
    List<Product> findByStatus(Boolean status);
    
    Page<Product> findByProductNameContainingIgnoreCase(String productName, Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.category.categoryId = :categoryId")
    List<Product> findByCategoryId(@Param("categoryId") Long categoryId);
    
    @Query("SELECT p FROM Product p WHERE p.brand.brandId = :brandId")
    List<Product> findByBrandId(@Param("brandId") Long brandId);
    
    @Query("SELECT p FROM Product p WHERE p.status = true ORDER BY p.enteredDate DESC")
    List<Product> findActiveProductsOrderByDate();
    
    Optional<Product> findByProductName(String productName);

    // ========== STATISTICS QUERIES ==========

    /**
     * Get top selling products
     * Returns: product_id, product_name, total_quantity_sold, total_revenue
     */
    @Query(value = "SELECT p.product_id, p.product_name, " +
            "SUM(od.quantity) as total_quantity_sold, " +
            "SUM(od.quantity * od.unit_price) as total_revenue " +
            "FROM order_details od " +
            "INNER JOIN products p ON od.product_id = p.product_id " +
            "INNER JOIN orders o ON od.order_id = o.order_id " +
            "WHERE o.status IN (1, 2) " +
            "GROUP BY p.product_id, p.product_name " +
            "ORDER BY total_quantity_sold DESC " +
            "OFFSET 0 ROWS FETCH NEXT :limit ROWS ONLY", nativeQuery = true)
    List<Object[]> getTopSellingProducts(@Param("limit") int limit);

    /**
     * Get favorite products statistics
     * Returns: category_name, brand_name, favorite_count, total_products
     */
    @Query(value = "SELECT c.category_name, b.brand_name, " +
            "COUNT(CASE WHEN p.favorite = 1 THEN 1 END) as favorite_count, " +
            "COUNT(p.product_id) as total_products " +
            "FROM products p " +
            "INNER JOIN categories c ON p.category_id = c.category_id " +
            "INNER JOIN brands b ON p.brand_id = b.brand_id " +
            "GROUP BY c.category_id, c.category_name, b.brand_id, b.brand_name " +
            "ORDER BY favorite_count DESC", nativeQuery = true)
    List<Object[]> getFavoriteProductsStatistics();

    /**
     * Get product inventory statistics
     * Returns: category_name, brand_name, total_quantity, total_value, avg_price
     */
    @Query(value = "SELECT c.category_name, b.brand_name, " +
            "SUM(p.quantity) as total_quantity, " +
            "SUM(p.quantity * p.price) as total_value, " +
            "AVG(p.price) as avg_price " +
            "FROM products p " +
            "INNER JOIN categories c ON p.category_id = c.category_id " +
            "INNER JOIN brands b ON p.brand_id = b.brand_id " +
            "WHERE p.status = 1 " +
            "GROUP BY c.category_id, c.category_name, b.brand_id, b.brand_name " +
            "ORDER BY total_value DESC", nativeQuery = true)
    List<Object[]> getInventoryStatistics();

    /**
     * Get products with low stock
     * Returns: product_name, category_name, brand_name, current_quantity, price
     */
    @Query(value = "SELECT p.product_name, c.category_name, b.brand_name, " +
            "p.quantity, p.price " +
            "FROM products p " +
            "INNER JOIN categories c ON p.category_id = c.category_id " +
            "INNER JOIN brands b ON p.brand_id = b.brand_id " +
            "WHERE p.quantity < 10 AND p.status = 1 " +
            "ORDER BY p.quantity ASC", nativeQuery = true)
    List<Object[]> getLowStockProducts();

    /**
     * Get products expiring soon
     * Returns: product_name, category_name, brand_name, expiry_date, quantity
     */
    @Query(value = "SELECT p.product_name, c.category_name, b.brand_name, " +
            "p.expiry_date, p.quantity " +
            "FROM products p " +
            "INNER JOIN categories c ON p.category_id = c.category_id " +
            "INNER JOIN brands b ON p.brand_id = b.brand_id " +
            "WHERE p.expiry_date BETWEEN CAST(GETDATE() AS DATE) AND CAST(DATEADD(day, 30, GETDATE()) AS DATE) " +
            "AND p.status = 1 " +
            "ORDER BY p.expiry_date ASC", nativeQuery = true)
    List<Object[]> getProductsExpiringSoon();

    /**
     * Get product discount statistics
     * Returns: category_name, brand_name, avg_discount, max_discount, products_with_discount
     */
    @Query(value = "SELECT c.category_name, b.brand_name, " +
            "AVG(p.discount) as avg_discount, " +
            "MAX(p.discount) as max_discount, " +
            "COUNT(CASE WHEN p.discount > 0 THEN 1 END) as products_with_discount " +
            "FROM products p " +
            "INNER JOIN categories c ON p.category_id = c.category_id " +
            "INNER JOIN brands b ON p.brand_id = b.brand_id " +
            "WHERE p.status = 1 " +
            "GROUP BY c.category_id, c.category_name, b.brand_id, b.brand_name " +
            "ORDER BY avg_discount DESC", nativeQuery = true)
    List<Object[]> getDiscountStatistics();

    /**
     * Get product price range statistics
     * Returns: category_name, brand_name, min_price, max_price, avg_price, product_count
     */
    @Query(value = "SELECT c.category_name, b.brand_name, " +
            "MIN(p.price) as min_price, " +
            "MAX(p.price) as max_price, " +
            "AVG(p.price) as avg_price, " +
            "COUNT(p.product_id) as product_count " +
            "FROM products p " +
            "INNER JOIN categories c ON p.category_id = c.category_id " +
            "INNER JOIN brands b ON p.brand_id = b.brand_id " +
            "WHERE p.status = 1 " +
            "GROUP BY c.category_id, c.category_name, b.brand_id, b.brand_name " +
            "ORDER BY avg_price DESC", nativeQuery = true)
    List<Object[]> getPriceRangeStatistics();

    /**
     * Get products by manufacture date statistics
     * Returns: year, month, product_count, total_value
     */
    @Query(value = "SELECT YEAR(p.manufacture_date) as year, " +
            "MONTH(p.manufacture_date) as month, " +
            "COUNT(p.product_id) as product_count, " +
            "SUM(p.quantity * p.price) as total_value " +
            "FROM products p " +
            "WHERE p.status = 1 AND p.manufacture_date IS NOT NULL " +
            "GROUP BY YEAR(p.manufacture_date), MONTH(p.manufacture_date) " +
            "ORDER BY year DESC, month DESC", nativeQuery = true)
    List<Object[]> getManufactureDateStatistics();

    /**
     * Get products by expiry date statistics
     * Returns: year, month, product_count, total_value
     */
    @Query(value = "SELECT YEAR(p.expiry_date) as year, " +
            "MONTH(p.expiry_date) as month, " +
            "COUNT(p.product_id) as product_count, " +
            "SUM(p.quantity * p.price) as total_value " +
            "FROM products p " +
            "WHERE p.status = 1 AND p.expiry_date IS NOT NULL " +
            "GROUP BY YEAR(p.expiry_date), MONTH(p.expiry_date) " +
            "ORDER BY year ASC, month ASC", nativeQuery = true)
    List<Object[]> getExpiryDateStatistics();

    /**
     * Get list of discounted products
     * Returns: product_name, category_name, brand_name, discount
     */
    @Query(value = "SELECT p.product_name, c.category_name, b.brand_name, p.discount " +
            "FROM products p " +
            "INNER JOIN categories c ON p.category_id = c.category_id " +
            "INNER JOIN brands b ON p.brand_id = b.brand_id " +
            "WHERE p.status = 1 AND p.discount > 0 " +
            "ORDER BY p.discount DESC, p.product_name ASC", nativeQuery = true)
    List<Object[]> getDiscountedProducts();



    

    /**
     * List products that currently have a discount (> 0)
     * Returns: product_name, category_name, brand_name, discount
     */
    

    /** Count new products entered in last 30 days */
    @Query(value = "SELECT COUNT(*) FROM products p WHERE p.status = 1 AND p.entered_date >= CAST(DATEADD(day, -30, GETDATE()) AS DATE)", nativeQuery = true)
    int countNewProductsLast30Days();

    /**
     * Slow moving or unsold products in last 30 days
     * Returns: product_name, sold_30_days, current_stock
     */
    @Query(value = "SELECT p.product_name, " +
            "ISNULL(SUM(CASE WHEN o.order_date >= DATEADD(day,-30,GETDATE()) AND o.status IN (1,2) THEN od.quantity ELSE 0 END),0) AS sold_30d, " +
            "p.quantity AS stock " +
            "FROM products p " +
            "LEFT JOIN order_details od ON p.product_id = od.product_id " +
            "LEFT JOIN orders o ON od.order_id = o.order_id " +
            "WHERE p.status = 1 " +
            "GROUP BY p.product_name, p.quantity " +
            "HAVING ISNULL(SUM(CASE WHEN o.order_date >= DATEADD(day,-30,GETDATE()) AND o.status IN (1,2) THEN od.quantity ELSE 0 END),0) <= 5 " +
            "ORDER BY sold_30d ASC, stock DESC", nativeQuery = true)
    List<Object[]> getSlowMovingProducts30Days();

    /**
     * Compare monthly quantities this month vs previous month
     * Returns: product_name, qty_this_month, qty_prev_month
     */
    @Query(value = "SELECT p.product_name, " +
            "ISNULL(SUM(CASE WHEN YEAR(o.order_date)=YEAR(GETDATE()) AND MONTH(o.order_date)=MONTH(GETDATE()) AND o.status IN (1,2) THEN od.quantity END),0) AS qty_this, " +
            "ISNULL(SUM(CASE WHEN YEAR(o.order_date)=YEAR(DATEADD(month,-1,GETDATE())) AND MONTH(o.order_date)=MONTH(DATEADD(month,-1,GETDATE())) AND o.status IN (1,2) THEN od.quantity END),0) AS qty_prev " +
            "FROM products p " +
            "LEFT JOIN order_details od ON p.product_id = od.product_id " +
            "LEFT JOIN orders o ON od.order_id = o.order_id " +
            "GROUP BY p.product_name", nativeQuery = true)
    List<Object[]> getProductMonthlyTrend();

    /**
     * Get newest active products
     * Returns: product_name, category_name, brand_name, entered_date, quantity
     */
    @Query(value = "SELECT p.product_name, c.category_name, b.brand_name, p.entered_date, p.quantity " +
            "FROM products p " +
            "INNER JOIN categories c ON p.category_id = c.category_id " +
            "INNER JOIN brands b ON p.brand_id = b.brand_id " +
            "WHERE p.status = 1 " +
            "ORDER BY p.entered_date DESC", nativeQuery = true)
    List<Object[]> getNewestActiveProducts();

        /**
         * Detailed inventory per product for server-side table
         * Returns: category_name, product_name, brand_name, quantity, price, total_value
         */
        @Query(value = "SELECT c.category_name, p.product_name, b.brand_name, p.quantity, p.price, (p.quantity * p.price) AS total_value " +
                        "FROM products p " +
                        "INNER JOIN categories c ON p.category_id = c.category_id " +
                        "INNER JOIN brands b ON p.brand_id = b.brand_id " +
                        "WHERE p.status = 1 " +
                        "ORDER BY c.category_name ASC, p.product_name ASC", nativeQuery = true)
        List<Object[]> getInventoryDetailsPerProduct();

            /**
             * Pageable searchable inventory details filtered by optional category
             * Returns rows: category_name, product_name, brand_name, quantity, price, total_value
             */
            @Query(value = "SELECT c.category_name, p.product_name, b.brand_name, p.quantity, p.price, (p.quantity * p.price) AS total_value " +
                    "FROM products p " +
                    "INNER JOIN categories c ON p.category_id = c.category_id " +
                    "INNER JOIN brands b ON p.brand_id = b.brand_id " +
                    "WHERE p.status = 1 " +
                    "AND (:categoryId IS NULL OR p.category_id = :categoryId) " +
                    "AND (:search IS NULL OR p.product_name LIKE CONCAT('%', :search, '%') " +
                    "     OR c.category_name LIKE CONCAT('%', :search, '%') " +
                    "     OR b.brand_name LIKE CONCAT('%', :search, '%')) " +
                    "ORDER BY c.category_name ASC, p.product_name ASC",
                    countQuery = "SELECT COUNT(*) " +
                            "FROM products p " +
                            "INNER JOIN categories c ON p.category_id = c.category_id " +
                            "INNER JOIN brands b ON p.brand_id = b.brand_id " +
                            "WHERE p.status = 1 " +
                            "AND (:categoryId IS NULL OR p.category_id = :categoryId) " +
                            "AND (:search IS NULL OR p.product_name LIKE CONCAT('%', :search, '%') " +
                            "     OR c.category_name LIKE CONCAT('%', :search, '%') " +
                            "     OR b.brand_name LIKE CONCAT('%', :search, '%'))",
                    nativeQuery = true)
    org.springframework.data.domain.Page<Object[]> getInventoryDetailsPage(
                    @Param("categoryId") Long categoryId,
                    @Param("search") String search,
                    org.springframework.data.domain.Pageable pageable);

    List<Product> findByShopShopId(Long shopId);

    Page<Product> findByShopShopId(Long shopId, Pageable pageable);

    Optional<Product> findByProductIdAndShopShopId(Long productId, Long shopId);

    long countByShopShopId(Long shopId);

    Page<Product> findByShopShopIdAndProductNameContainingIgnoreCase(Long shopId, String productName, Pageable pageable);

    @Query(value = "SELECT s.shop_id, s.shop_name, s.status, u.name AS vendor_name, " +
            "COUNT(p.product_id) AS product_count, " +
            "ISNULL(SUM(p.quantity), 0) AS total_quantity, " +
            "ISNULL(SUM(ISNULL(p.quantity, 0) * ISNULL(p.price, 0)), 0) AS total_inventory_value " +
            "FROM shops s " +
            "LEFT JOIN [user] u ON s.vendor_id = u.user_id " +
            "LEFT JOIN products p ON s.shop_id = p.shop_id " +
            "GROUP BY s.shop_id, s.shop_name, s.status, u.name " +
            "ORDER BY s.shop_name", nativeQuery = true)
    List<Object[]> getShopProductStatistics();

    @Query(value = "SELECT DISTINCT s.shop_id, s.shop_name FROM shops s ORDER BY s.shop_name", nativeQuery = true)
    List<Object[]> findAllShops();
    
    // Get top 20 products with discount
    List<Product> findTop20ByDiscountGreaterThanOrderByDiscountDesc(Integer discount);
}

