package vn.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.entity.FlashSaleProduct;

import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for FlashSaleProduct entity
 * @author OneShop Team
 */
@Repository
public interface FlashSaleProductRepository extends JpaRepository<FlashSaleProduct, Long> {
    
    // Find products by flash sale ID
    List<FlashSaleProduct> findByFlashSaleFlashSaleId(Long flashSaleId);
    
    // Find products by flash sale ID with pagination
    Page<FlashSaleProduct> findByFlashSaleFlashSaleId(Long flashSaleId, Pageable pageable);
    
    // Find active products by flash sale ID
    @Query("SELECT fsp FROM FlashSaleProduct fsp WHERE fsp.flashSale.flashSaleId = :flashSaleId " +
           "AND fsp.isActive = true")
    List<FlashSaleProduct> findActiveProductsByFlashSaleId(@Param("flashSaleId") Long flashSaleId);
    
    // Find product in flash sale
    @Query("SELECT fsp FROM FlashSaleProduct fsp WHERE fsp.flashSale.flashSaleId = :flashSaleId " +
           "AND fsp.product.productId = :productId")
    Optional<FlashSaleProduct> findByFlashSaleIdAndProductId(@Param("flashSaleId") Long flashSaleId, 
                                                             @Param("productId") Long productId);
    
    // Find product in active flash sale (for checking if product is in any active flash sale)
    @Query("SELECT fsp FROM FlashSaleProduct fsp WHERE fsp.product.productId = :productId " +
           "AND fsp.isActive = true " +
           "AND fsp.flashSale.status = vn.entity.FlashSale.FlashSaleStatus.ACTIVE " +
           "AND fsp.flashSale.startTime <= :now AND fsp.flashSale.endTime > :now")
    Optional<FlashSaleProduct> findByProductIdAndActive(@Param("productId") Long productId, 
                                                       @Param("now") LocalDateTime now);
    
    // Find all active products in flash sales (for homepage display)
    @Query("SELECT fsp FROM FlashSaleProduct fsp WHERE fsp.isActive = true " +
           "AND fsp.flashSale.status = vn.entity.FlashSale.FlashSaleStatus.ACTIVE " +
           "AND fsp.flashSale.startTime <= :now AND fsp.flashSale.endTime > :now " +
           "ORDER BY fsp.displayOrder ASC, fsp.flashSale.startTime ASC")
    List<FlashSaleProduct> findActiveProductsInActiveFlashSales(@Param("now") LocalDateTime now);
    
    // Find active products in specific flash sale with pagination
    @Query("SELECT fsp FROM FlashSaleProduct fsp WHERE fsp.flashSale.flashSaleId = :flashSaleId " +
           "AND fsp.isActive = true " +
           "ORDER BY fsp.displayOrder ASC")
    Page<FlashSaleProduct> findActiveProductsByFlashSaleId(@Param("flashSaleId") Long flashSaleId, 
                                                           Pageable pageable);
    
    // Find products with remaining quantity > 0
    @Query("SELECT fsp FROM FlashSaleProduct fsp WHERE fsp.flashSale.flashSaleId = :flashSaleId " +
           "AND fsp.isActive = true " +
           "AND (fsp.quantityLimit - fsp.soldQuantity) > 0")
    List<FlashSaleProduct> findAvailableProductsByFlashSaleId(@Param("flashSaleId") Long flashSaleId);
    
    // Lock product for update (for concurrency control)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT fsp FROM FlashSaleProduct fsp WHERE fsp.flashSaleProductId = :id")
    Optional<FlashSaleProduct> findByIdForUpdate(@Param("id") Long id);
    
    // Count products in flash sale
    long countByFlashSaleFlashSaleId(Long flashSaleId);
    
    // Count active products in flash sale
    @Query("SELECT COUNT(fsp) FROM FlashSaleProduct fsp WHERE fsp.flashSale.flashSaleId = :flashSaleId " +
           "AND fsp.isActive = true")
    long countActiveProductsByFlashSaleId(@Param("flashSaleId") Long flashSaleId);
}

