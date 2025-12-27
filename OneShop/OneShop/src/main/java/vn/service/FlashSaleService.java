package vn.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.entity.FlashSale;
import vn.entity.FlashSaleProduct;
import vn.entity.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for Flash Sale management
 * @author OneShop Team
 */
public interface FlashSaleService {
    
    // ===== CRUD Operations =====
    
    /**
     * Get all flash sales
     */
    List<FlashSale> getAllFlashSales();
    
    /**
     * Get flash sales with pagination
     */
    Page<FlashSale> getFlashSalesWithPagination(Pageable pageable);
    
    /**
     * Get flash sale by ID
     */
    Optional<FlashSale> getFlashSaleById(Long id);
    
    /**
     * Create new flash sale
     */
    FlashSale createFlashSale(FlashSale flashSale, User creator);
    
    /**
     * Update flash sale
     */
    FlashSale updateFlashSale(Long id, FlashSale flashSale);
    
    /**
     * Delete flash sale
     */
    void deleteFlashSale(Long id);
    
    // ===== Product Management =====
    
    /**
     * Add product to flash sale
     */
    FlashSaleProduct addProductToFlashSale(Long flashSaleId, Long productId, 
                                          BigDecimal flashSalePrice, 
                                          Integer quantityLimit,
                                          Integer maxQuantityPerUser,
                                          Integer displayOrder);
    
    /**
     * Remove product from flash sale
     */
    void removeProductFromFlashSale(Long flashSaleId, Long productId);
    
    /**
     * Update product in flash sale
     */
    FlashSaleProduct updateFlashSaleProduct(Long flashSaleProductId, 
                                           BigDecimal flashSalePrice,
                                           Integer quantityLimit,
                                           Integer maxQuantityPerUser,
                                           Integer displayOrder);
    
    /**
     * Get products in flash sale
     */
    List<FlashSaleProduct> getProductsByFlashSaleId(Long flashSaleId);
    
    /**
     * Get products in flash sale with pagination
     */
    Page<FlashSaleProduct> getProductsByFlashSaleId(Long flashSaleId, Pageable pageable);
    
    /**
     * Get flash sale product by ID
     */
    Optional<FlashSaleProduct> getFlashSaleProductById(Long flashSaleProductId);
    
    /**
     * Get flash sale product by flash sale ID and product ID
     */
    Optional<FlashSaleProduct> getFlashSaleProduct(Long flashSaleId, Long productId);
    
    // ===== Status Management =====
    
    /**
     * Activate flash sale (change status to ACTIVE)
     */
    FlashSale activateFlashSale(Long id);
    
    /**
     * Cancel flash sale (change status to CANCELLED)
     */
    FlashSale cancelFlashSale(Long id);
    
    /**
     * End flash sale (change status to ENDED)
     */
    FlashSale endFlashSale(Long id);
    
    /**
     * Schedule flash sale (change status to SCHEDULED)
     */
    FlashSale scheduleFlashSale(Long id);
    
    // ===== Auto Scheduler =====
    
    /**
     * Check and activate scheduled flash sales (called by scheduler)
     */
    void checkAndActivateScheduledFlashSales();
    
    /**
     * Check and end active flash sales (called by scheduler)
     */
    void checkAndEndActiveFlashSales();
    
    // ===== Query Operations =====
    
    /**
     * Get active flash sales (currently running)
     */
    List<FlashSale> getActiveFlashSales();
    
    /**
     * Get active flash sales with pagination
     */
    Page<FlashSale> getActiveFlashSales(Pageable pageable);
    
    /**
     * Get upcoming flash sales (scheduled, starting soon)
     */
    List<FlashSale> getUpcomingFlashSales();
    
    /**
     * Get upcoming flash sales with pagination
     */
    Page<FlashSale> getUpcomingFlashSales(Pageable pageable);
    
    /**
     * Get flash sales by status
     */
    List<FlashSale> getFlashSalesByStatus(FlashSale.FlashSaleStatus status);
    
    /**
     * Get flash sales by status with pagination
     */
    Page<FlashSale> getFlashSalesByStatus(FlashSale.FlashSaleStatus status, Pageable pageable);
    
    /**
     * Search flash sales by name
     */
    Page<FlashSale> searchFlashSalesByName(String name, Pageable pageable);
    
    /**
     * Get flash sales by date range
     */
    List<FlashSale> getFlashSalesByDateRange(LocalDateTime startDate, LocalDateTime endDate);
    
    // ===== Validation & Business Logic =====
    
    /**
     * Check if user can purchase product in flash sale
     */
    boolean canUserPurchase(Long flashSaleId, Long productId, Long userId, Integer quantity);
    
    /**
     * Check if product is in any active flash sale
     */
    boolean isProductInFlashSale(Long productId);
    
    /**
     * Get flash sale price for product (if in active flash sale)
     */
    BigDecimal getFlashSalePrice(Long productId);
    
    /**
     * Get flash sale product for product (if in active flash sale)
     */
    Optional<FlashSaleProduct> getActiveFlashSaleProduct(Long productId);
    
    /**
     * Validate flash sale dates
     */
    boolean validateFlashSaleDates(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * Validate flash sale product data
     */
    boolean validateFlashSaleProduct(Long productId, BigDecimal flashSalePrice, Integer quantityLimit);
    
    // ===== Business Operations =====
    
    /**
     * Record flash sale purchase (called when order is created)
     */
    void recordFlashSalePurchase(Long flashSaleId, Long orderId, Long productId, 
                                Long userId, Integer quantity, BigDecimal flashSalePrice);
    
    /**
     * Get total sold quantity for product in flash sale
     */
    Integer getTotalSoldQuantity(Long flashSaleId, Long productId);
    
    /**
     * Get user purchased quantity for product in flash sale
     */
    Integer getUserPurchasedQuantity(Long flashSaleId, Long productId, Long userId);
    
    // ===== Statistics =====
    
    /**
     * Count total flash sales
     */
    long getTotalFlashSaleCount();
    
    /**
     * Count active flash sales
     */
    long getActiveFlashSaleCount();
    
    /**
     * Count flash sales by status
     */
    long getFlashSaleCountByStatus(FlashSale.FlashSaleStatus status);
}

