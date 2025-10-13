package vn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.entity.CartItemEntity;
import vn.entity.Product;
import vn.entity.User;

import java.util.List;
import java.util.Optional;

/**
 * Repository for CartItemEntity operations
 * @author OneShop Team
 */
@Repository
public interface CartItemRepository extends JpaRepository<CartItemEntity, Long> {
    
    /**
     * Find cart item by user and product
     */
    Optional<CartItemEntity> findByUserAndProduct(User user, Product product);
    
    /**
     * Find all cart items by user
     */
    List<CartItemEntity> findByUser(User user);
    
    /**
     * Find all cart items by user ID
     */
    @Query("SELECT ci FROM CartItemEntity ci WHERE ci.user.userId = :userId")
    List<CartItemEntity> findByUserId(@Param("userId") Long userId);
    
    /**
     * Delete cart item by user and product
     */
    @Modifying
    @Query("DELETE FROM CartItemEntity ci WHERE ci.user = :user AND ci.product = :product")
    void deleteByUserAndProduct(@Param("user") User user, @Param("product") Product product);
    
    /**
     * Delete all cart items by user
     */
    void deleteByUser(User user);
    
    /**
     * Count cart items by user
     */
    long countByUser(User user);
    
    /**
     * Check if cart item exists by user and product
     */
    boolean existsByUserAndProduct(User user, Product product);
    
    /**
     * Find cart items by user and selected status
     */
    List<CartItemEntity> findByUserAndSelected(User user, Boolean selected);
    
    /**
     * Count cart items by user and selected status
     */
    long countByUserAndSelected(User user, Boolean selected);
}
