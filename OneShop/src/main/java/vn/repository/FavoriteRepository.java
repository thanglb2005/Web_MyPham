package vn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.entity.Favorite;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Favorite entity
 * @author OneShop Team
 */
@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    /**
     * Find favorite by product ID and user ID
     */
    @Query("SELECT f FROM Favorite f WHERE f.product.productId = :productId AND f.user.userId = :userId")
    Optional<Favorite> findByProductIdAndUserId(@Param("productId") Long productId, @Param("userId") Long userId);

    /**
     * Get all favorites by user ID
     */
    @Query("SELECT f FROM Favorite f WHERE f.user.userId = :userId ORDER BY f.favoriteId DESC")
    List<Favorite> findAllByUserId(@Param("userId") Long userId);

    /**
     * Count favorites by user ID
     */
    @Query("SELECT COUNT(f) FROM Favorite f WHERE f.user.userId = :userId")
    Integer countByUserId(@Param("userId") Long userId);

    /**
     * Check if product is favorited by user
     */
    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END FROM Favorite f WHERE f.product.productId = :productId AND f.user.userId = :userId")
    Boolean existsByProductIdAndUserId(@Param("productId") Long productId, @Param("userId") Long userId);

    /**
     * Delete favorite by product ID and user ID
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM Favorite f WHERE f.product.productId = :productId AND f.user.userId = :userId")
    void deleteByProductIdAndUserId(@Param("productId") Long productId, @Param("userId") Long userId);
}
