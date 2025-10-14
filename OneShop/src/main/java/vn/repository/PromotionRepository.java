package vn.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.entity.Promotion;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Long> {
    
    // Find by promotion code
    Optional<Promotion> findByPromotionCode(String promotionCode);
    
    // Find active promotions
    @Query("SELECT p FROM Promotion p WHERE p.isActive = true AND p.startDate <= :now AND p.endDate >= :now")
    List<Promotion> findActivePromotions(@Param("now") LocalDateTime now);
    
    // Find active promotions with pagination
    @Query("SELECT p FROM Promotion p WHERE p.isActive = true AND p.startDate <= :now AND p.endDate >= :now")
    Page<Promotion> findActivePromotions(@Param("now") LocalDateTime now, Pageable pageable);
    
    // Find promotions by type
    List<Promotion> findByPromotionType(Promotion.PromotionType promotionType);
    
    // Find promotions by type with pagination
    Page<Promotion> findByPromotionType(Promotion.PromotionType promotionType, Pageable pageable);
    
    // Find promotions by name (case insensitive)
    @Query("SELECT p FROM Promotion p WHERE LOWER(p.promotionName) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Promotion> findByNameContainingIgnoreCase(@Param("name") String name);
    
    // Find promotions by name with pagination
    @Query("SELECT p FROM Promotion p WHERE LOWER(p.promotionName) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Promotion> findByNameContainingIgnoreCase(@Param("name") String name, Pageable pageable);
    
    // Find promotions by code (case insensitive)
    @Query("SELECT p FROM Promotion p WHERE LOWER(p.promotionCode) LIKE LOWER(CONCAT('%', :code, '%'))")
    List<Promotion> findByCodeContainingIgnoreCase(@Param("code") String code);
    
    // Find promotions by code with pagination
    @Query("SELECT p FROM Promotion p WHERE LOWER(p.promotionCode) LIKE LOWER(CONCAT('%', :code, '%'))")
    Page<Promotion> findByCodeContainingIgnoreCase(@Param("code") String code, Pageable pageable);
    
    // Find expiring promotions (within next 7 days)
    @Query("SELECT p FROM Promotion p WHERE p.isActive = true AND p.endDate BETWEEN :now AND :expiringDate")
    List<Promotion> findExpiringPromotions(@Param("now") LocalDateTime now, @Param("expiringDate") LocalDateTime expiringDate);
    
    // Find fully used promotions
    @Query("SELECT p FROM Promotion p WHERE p.usedCount >= p.usageLimit")
    List<Promotion> findFullyUsedPromotions();
    
    // Find expired promotions
    @Query("SELECT p FROM Promotion p WHERE p.endDate < :now")
    List<Promotion> findExpiredPromotions(@Param("now") LocalDateTime now);
    
    // Count active promotions
    @Query("SELECT COUNT(p) FROM Promotion p WHERE p.isActive = true AND p.startDate <= :now AND p.endDate >= :now")
    long countActivePromotions(@Param("now") LocalDateTime now);
    
    // Count promotions by type
    long countByPromotionType(Promotion.PromotionType promotionType);
    
    // Find promotions by status
    List<Promotion> findByIsActive(Boolean isActive);
    
    // Find promotions by status with pagination
    Page<Promotion> findByIsActive(Boolean isActive, Pageable pageable);
    
    // Find promotions by date range
    @Query("SELECT p FROM Promotion p WHERE p.startDate >= :startDate AND p.endDate <= :endDate")
    List<Promotion> findByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    // Find promotions by date range with pagination
    @Query("SELECT p FROM Promotion p WHERE p.startDate >= :startDate AND p.endDate <= :endDate")
    Page<Promotion> findByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, Pageable pageable);
    
    // Find promotions that are currently running
    @Query("SELECT p FROM Promotion p WHERE p.isActive = true AND p.startDate <= :now AND p.endDate >= :now AND p.usedCount < p.usageLimit")
    List<Promotion> findCurrentlyRunningPromotions(@Param("now") LocalDateTime now);
    
    // Find promotions that are currently running with pagination
    @Query("SELECT p FROM Promotion p WHERE p.isActive = true AND p.startDate <= :now AND p.endDate >= :now AND p.usedCount < p.usageLimit")
    Page<Promotion> findCurrentlyRunningPromotions(@Param("now") LocalDateTime now, Pageable pageable);
    
    // Search promotions by multiple criteria
    @Query("SELECT p FROM Promotion p WHERE " +
           "(:name IS NULL OR LOWER(p.promotionName) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:code IS NULL OR LOWER(p.promotionCode) LIKE LOWER(CONCAT('%', :code, '%'))) AND " +
           "(:type IS NULL OR p.promotionType = :type) AND " +
           "(:active IS NULL OR p.isActive = :active)")
    Page<Promotion> searchPromotions(@Param("name") String name, 
                                   @Param("code") String code, 
                                   @Param("type") Promotion.PromotionType type, 
                                   @Param("active") Boolean active, 
                                   Pageable pageable);
    
    // Find top used promotions
    @Query("SELECT p FROM Promotion p ORDER BY p.usedCount DESC")
    List<Promotion> findTopUsedPromotions(Pageable pageable);
    
    // Find least used promotions
    @Query("SELECT p FROM Promotion p ORDER BY p.usedCount ASC")
    List<Promotion> findLeastUsedPromotions(Pageable pageable);
    
    // Find promotions by usage percentage
    @Query("SELECT p FROM Promotion p WHERE (p.usedCount * 100.0 / p.usageLimit) >= :percentage")
    List<Promotion> findByUsagePercentage(@Param("percentage") double percentage);
    
    // Find promotions by usage percentage with pagination
    @Query("SELECT p FROM Promotion p WHERE (p.usedCount * 100.0 / p.usageLimit) >= :percentage")
    Page<Promotion> findByUsagePercentage(@Param("percentage") double percentage, Pageable pageable);
    
    // ===== SHOP-SPECIFIC PROMOTION METHODS =====
    
    // Find promotions by shop
    List<Promotion> findByShopShopId(Long shopId);
    
    // Find promotions by shop with pagination
    Page<Promotion> findByShopShopId(Long shopId, Pageable pageable);
    
    // Find active promotions by shop
    @Query("SELECT p FROM Promotion p WHERE p.shop.shopId = :shopId AND p.isActive = true AND p.startDate <= :now AND p.endDate >= :now")
    List<Promotion> findActivePromotionsByShop(@Param("shopId") Long shopId, @Param("now") LocalDateTime now);
    
    // Find active promotions by shop with pagination
    @Query("SELECT p FROM Promotion p WHERE p.shop.shopId = :shopId AND p.isActive = true AND p.startDate <= :now AND p.endDate >= :now")
    Page<Promotion> findActivePromotionsByShop(@Param("shopId") Long shopId, @Param("now") LocalDateTime now, Pageable pageable);
    
    // Find promotions by shop and type
    @Query("SELECT p FROM Promotion p WHERE p.shop.shopId = :shopId AND p.promotionType = :type")
    List<Promotion> findByShopAndType(@Param("shopId") Long shopId, @Param("type") Promotion.PromotionType type);
    
    // Find promotions by shop and type with pagination
    @Query("SELECT p FROM Promotion p WHERE p.shop.shopId = :shopId AND p.promotionType = :type")
    Page<Promotion> findByShopAndType(@Param("shopId") Long shopId, @Param("type") Promotion.PromotionType type, Pageable pageable);
    
    // Find promotions by shop and status
    @Query("SELECT p FROM Promotion p WHERE p.shop.shopId = :shopId AND p.isActive = :active")
    List<Promotion> findByShopAndStatus(@Param("shopId") Long shopId, @Param("active") Boolean active);
    
    // Find promotions by shop and status with pagination
    @Query("SELECT p FROM Promotion p WHERE p.shop.shopId = :shopId AND p.isActive = :active")
    Page<Promotion> findByShopAndStatus(@Param("shopId") Long shopId, @Param("active") Boolean active, Pageable pageable);
    
    // Find promotions by shop and promotion code
    @Query("SELECT p FROM Promotion p WHERE p.shop.shopId = :shopId AND p.promotionCode = :code")
    Optional<Promotion> findByShopAndCode(@Param("shopId") Long shopId, @Param("code") String code);
    
    // Find promotions by shop and name search
    @Query("SELECT p FROM Promotion p WHERE p.shop.shopId = :shopId AND LOWER(p.promotionName) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Promotion> findByShopAndNameContaining(@Param("shopId") Long shopId, @Param("name") String name);
    
    // Find promotions by shop and name search with pagination
    @Query("SELECT p FROM Promotion p WHERE p.shop.shopId = :shopId AND LOWER(p.promotionName) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Promotion> findByShopAndNameContaining(@Param("shopId") Long shopId, @Param("name") String name, Pageable pageable);
    
    // Count promotions by shop
    long countByShopShopId(Long shopId);
    
    // Count active promotions by shop
    @Query("SELECT COUNT(p) FROM Promotion p WHERE p.shop.shopId = :shopId AND p.isActive = true AND p.startDate <= :now AND p.endDate >= :now")
    long countActivePromotionsByShop(@Param("shopId") Long shopId, @Param("now") LocalDateTime now);
    
    // Find expiring promotions by shop
    @Query("SELECT p FROM Promotion p WHERE p.shop.shopId = :shopId AND p.isActive = true AND p.endDate BETWEEN :now AND :expiringDate")
    List<Promotion> findExpiringPromotionsByShop(@Param("shopId") Long shopId, @Param("now") LocalDateTime now, @Param("expiringDate") LocalDateTime expiringDate);
    
    // Find fully used promotions by shop
    @Query("SELECT p FROM Promotion p WHERE p.shop.shopId = :shopId AND p.usedCount >= p.usageLimit")
    List<Promotion> findFullyUsedPromotionsByShop(@Param("shopId") Long shopId);
    
    // Find expired promotions by shop
    @Query("SELECT p FROM Promotion p WHERE p.shop.shopId = :shopId AND p.endDate < :now")
    List<Promotion> findExpiredPromotionsByShop(@Param("shopId") Long shopId, @Param("now") LocalDateTime now);
    
    // Search promotions by shop with multiple criteria
    @Query("SELECT p FROM Promotion p WHERE p.shop.shopId = :shopId AND " +
           "(:name IS NULL OR LOWER(p.promotionName) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:code IS NULL OR LOWER(p.promotionCode) LIKE LOWER(CONCAT('%', :code, '%'))) AND " +
           "(:type IS NULL OR p.promotionType = :type) AND " +
           "(:active IS NULL OR p.isActive = :active)")
    Page<Promotion> searchPromotionsByShop(@Param("shopId") Long shopId,
                                          @Param("name") String name, 
                                          @Param("code") String code, 
                                          @Param("type") Promotion.PromotionType type, 
                                          @Param("active") Boolean active, 
                                          Pageable pageable);
}
