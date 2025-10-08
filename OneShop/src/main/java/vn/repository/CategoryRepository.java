package vn.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.entity.Category;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Category findByCategoryName(String categoryName);
    Page<Category> findByCategoryNameContainingIgnoreCase(String categoryName, Pageable pageable);

    /**
     * 1. Thống kê số lượng sản phẩm theo danh mục
     */
    @Query("SELECT c.categoryName, COUNT(p.productId) " +
           "FROM Category c " +
           "LEFT JOIN Product p ON c.categoryId = p.category.categoryId " +
           "GROUP BY c.categoryName " +
           "ORDER BY COUNT(p.productId) DESC")
    List<Object[]> getProductCountByCategory();

    /**
     * 2. Thống kê doanh thu theo danh mục (chỉ sản phẩm có trong order)
     */
    @Query("SELECT c.categoryName, COALESCE(SUM(od.unitPrice * od.quantity), 0) " +
           "FROM Category c " +
           "LEFT JOIN Product p ON c.categoryId = p.category.categoryId " +
           "LEFT JOIN OrderDetail od ON od.product.productId = p.productId " +
           "GROUP BY c.categoryName " +
           "ORDER BY COALESCE(SUM(od.unitPrice * od.quantity), 0) DESC")
    List<Object[]> getRevenueByCategory();

    /**
     * 3. Thống kê đánh giá trung bình theo danh mục (giả lập từ favorite)
     */
    @Query("SELECT c.categoryName, AVG(CASE WHEN p.favorite = true THEN 5.0 ELSE 3.0 END), COUNT(p.productId) " +
           "FROM Category c " +
           "LEFT JOIN Product p ON c.categoryId = p.category.categoryId " +
           "GROUP BY c.categoryName " +
           "ORDER BY AVG(CASE WHEN p.favorite = true THEN 5.0 ELSE 3.0 END) DESC")
    List<Object[]> getRatingByCategory();

    /**
     * 4. Thống kê lượt yêu thích theo danh mục (sử dụng trường favorite trong Product)
     */
    @Query("SELECT c.categoryName, COUNT(CASE WHEN p.favorite = true THEN 1 END) " +
           "FROM Category c " +
           "LEFT JOIN Product p ON c.categoryId = p.category.categoryId " +
           "GROUP BY c.categoryName " +
           "ORDER BY COUNT(CASE WHEN p.favorite = true THEN 1 END) DESC")
    List<Object[]> getFavoritesByCategory();

    /**
     * 5. Thống kê tổng hợp nhiều tiêu chí
     */
    @Query("SELECT c.categoryName, " +
           "COUNT(p.productId), " +
           "COALESCE(SUM(od.unitPrice * od.quantity), 0), " +
           "COUNT(CASE WHEN p.favorite = true THEN 1 END), " +
           "AVG(CASE WHEN p.favorite = true THEN 5.0 ELSE 3.0 END), " +
           "COUNT(p.productId), " +
           "c.categoryId, " +
           "c.categoryImage " +
           "FROM Category c " +
           "LEFT JOIN Product p ON c.categoryId = p.category.categoryId " +
           "LEFT JOIN OrderDetail od ON od.product.productId = p.productId " +
           "GROUP BY c.categoryName, c.categoryId, c.categoryImage " +
           "ORDER BY COALESCE(SUM(od.unitPrice * od.quantity), 0) DESC")
    List<Object[]> getComprehensiveCategoryStats();

    /**
     * Thống kê doanh thu theo danh mục trong khoảng thời gian
     */
    @Query("SELECT c.categoryName, COALESCE(SUM(od.unitPrice * od.quantity), 0) " +
           "FROM Category c " +
           "LEFT JOIN Product p ON c.categoryId = p.category.categoryId " +
           "LEFT JOIN OrderDetail od ON od.product.productId = p.productId " +
           "LEFT JOIN Order o ON od.order.orderId = o.orderId " +
           "WHERE o.orderDate BETWEEN :startDate AND :endDate " +
           "GROUP BY c.categoryName " +
           "ORDER BY COALESCE(SUM(od.unitPrice * od.quantity), 0) DESC")
    List<Object[]> getRevenueByCategoryInDateRange(@Param("startDate") String startDate, 
                                                  @Param("endDate") String endDate);

    /**
     * Thống kê số lượng sản phẩm theo danh mục có trạng thái active
     */
    @Query("SELECT c.categoryName, COUNT(p.productId) " +
           "FROM Category c " +
           "LEFT JOIN Product p ON c.categoryId = p.category.categoryId AND p.status = true " +
           "GROUP BY c.categoryName " +
           "ORDER BY COUNT(p.productId) DESC")
    List<Object[]> getActiveProductCountByCategory();

    /**
     * Thống kê doanh thu theo danh mục có trạng thái active
     */
    @Query("SELECT c.categoryName, COALESCE(SUM(od.unitPrice * od.quantity), 0) " +
           "FROM Category c " +
           "LEFT JOIN Product p ON c.categoryId = p.category.categoryId " +
           "LEFT JOIN OrderDetail od ON od.product.productId = p.productId " +
           "WHERE p.status = true " +
           "GROUP BY c.categoryName " +
           "ORDER BY COALESCE(SUM(od.unitPrice * od.quantity), 0) DESC")
    List<Object[]> getActiveRevenueByCategory();
}
