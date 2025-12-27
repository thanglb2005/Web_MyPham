package vn.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.entity.FlashSale;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for FlashSale entity
 * @author OneShop Team
 */
@Repository
public interface FlashSaleRepository extends JpaRepository<FlashSale, Long> {
    
    // Find flash sales by status
    List<FlashSale> findByStatus(FlashSale.FlashSaleStatus status);
    
    // Find flash sales by status with pagination
    Page<FlashSale> findByStatus(FlashSale.FlashSaleStatus status, Pageable pageable);
    
    // Find active flash sales (currently running)
    @Query("SELECT fs FROM FlashSale fs WHERE fs.status = 'ACTIVE' " +
           "AND fs.startTime <= :now AND fs.endTime > :now")
    List<FlashSale> findActiveFlashSales(@Param("now") LocalDateTime now);
    
    // Find active flash sales with pagination
    @Query("SELECT fs FROM FlashSale fs WHERE fs.status = 'ACTIVE' " +
           "AND fs.startTime <= :now AND fs.endTime > :now")
    Page<FlashSale> findActiveFlashSales(@Param("now") LocalDateTime now, Pageable pageable);
    
    // Find upcoming flash sales (scheduled, starting within next hour)
    @Query("SELECT fs FROM FlashSale fs WHERE fs.status = 'SCHEDULED' " +
           "AND fs.startTime BETWEEN :now AND :nextHour")
    List<FlashSale> findUpcomingFlashSales(@Param("now") LocalDateTime now, 
                                          @Param("nextHour") LocalDateTime nextHour);
    
    // Find upcoming flash sales with pagination
    @Query("SELECT fs FROM FlashSale fs WHERE fs.status = 'SCHEDULED' " +
           "AND fs.startTime BETWEEN :now AND :nextHour")
    Page<FlashSale> findUpcomingFlashSales(@Param("now") LocalDateTime now, 
                                          @Param("nextHour") LocalDateTime nextHour,
                                          Pageable pageable);
    
    // Find flash sales that should be activated (scheduled and start time has passed)
    @Query("SELECT fs FROM FlashSale fs WHERE fs.status = 'SCHEDULED' " +
           "AND fs.startTime <= :now")
    List<FlashSale> findFlashSalesToActivate(@Param("now") LocalDateTime now);
    
    // Find flash sales that should be ended (active and end time has passed)
    @Query("SELECT fs FROM FlashSale fs WHERE fs.status = 'ACTIVE' " +
           "AND fs.endTime < :now")
    List<FlashSale> findFlashSalesToEnd(@Param("now") LocalDateTime now);
    
    // Find flash sale containing a specific product (active)
    @Query("SELECT DISTINCT fs FROM FlashSale fs JOIN fs.products fsp " +
           "WHERE fsp.product.productId = :productId " +
           "AND fs.status = 'ACTIVE' " +
           "AND fs.startTime <= :now AND fs.endTime > :now")
    Optional<FlashSale> findActiveFlashSaleByProduct(@Param("productId") Long productId, 
                                                     @Param("now") LocalDateTime now);
    
    // Find all flash sales containing a specific product
    @Query("SELECT DISTINCT fs FROM FlashSale fs JOIN fs.products fsp " +
           "WHERE fsp.product.productId = :productId")
    List<FlashSale> findFlashSalesByProduct(@Param("productId") Long productId);
    
    // Find flash sales by date range
    @Query("SELECT fs FROM FlashSale fs WHERE fs.startTime >= :startDate AND fs.endTime <= :endDate")
    List<FlashSale> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                                   @Param("endDate") LocalDateTime endDate);
    
    // Find flash sales by date range with pagination
    @Query("SELECT fs FROM FlashSale fs WHERE fs.startTime >= :startDate AND fs.endTime <= :endDate")
    Page<FlashSale> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                                   @Param("endDate") LocalDateTime endDate,
                                   Pageable pageable);
    
    // Search flash sales by name
    @Query("SELECT fs FROM FlashSale fs WHERE LOWER(fs.saleName) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<FlashSale> findByNameContainingIgnoreCase(@Param("name") String name);
    
    // Search flash sales by name with pagination
    @Query("SELECT fs FROM FlashSale fs WHERE LOWER(fs.saleName) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<FlashSale> findByNameContainingIgnoreCase(@Param("name") String name, Pageable pageable);
    
    // Count active flash sales
    @Query("SELECT COUNT(fs) FROM FlashSale fs WHERE fs.status = 'ACTIVE' " +
           "AND fs.startTime <= :now AND fs.endTime > :now")
    long countActiveFlashSales(@Param("now") LocalDateTime now);
    
    // Count flash sales by status
    long countByStatus(FlashSale.FlashSaleStatus status);
}

