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
    @Query(value = "SELECT * FROM products WHERE product_name LIKE %?1%", nativeQuery = true)
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
    
    @Query(value = "SELECT * FROM products WHERE product_id IN :ids", nativeQuery = true)
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
}

