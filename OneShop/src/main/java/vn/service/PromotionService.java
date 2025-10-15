package vn.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.dto.VendorPromotionForm;
import vn.entity.Promotion;
import vn.entity.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PromotionService {
    
    // Basic CRUD operations
    List<Promotion> getAllPromotions();
    Page<Promotion> getPromotionsWithPagination(Pageable pageable);
    Optional<Promotion> getPromotionById(Long id);
    Optional<Promotion> getPromotionByCode(String code);
    Promotion savePromotion(Promotion promotion);
    void deletePromotion(Long id);
    Promotion updatePromotion(Promotion promotion);
    Promotion updatePromotion(Long id, VendorPromotionForm form, User actor);
    
    // Search and filter operations
    Page<Promotion> searchPromotionsByName(String name, Pageable pageable);
    Page<Promotion> searchPromotionsByCode(String code, Pageable pageable);
    List<Promotion> getPromotionsByType(Promotion.PromotionType type);
    Page<Promotion> getPromotionsByType(Promotion.PromotionType type, Pageable pageable);
    List<Promotion> getActivePromotions();
    Page<Promotion> getActivePromotions(Pageable pageable);
    List<Promotion> getInactivePromotions();
    Page<Promotion> getInactivePromotions(Pageable pageable);
    
    // Advanced search
    Page<Promotion> searchPromotions(String name, String code, Promotion.PromotionType type, Boolean active, Pageable pageable);
    
    // Status and validation operations
    boolean isPromotionValid(String promotionCode);
    boolean isPromotionExpired(Long promotionId);
    boolean isPromotionFullyUsed(Long promotionId);
    boolean isPromotionAvailable(Long promotionId);
    
    // Statistics and analytics
    long getTotalPromotionCount();
    long getActivePromotionCount();
    long getInactivePromotionCount();
    long getExpiredPromotionCount();
    long getFullyUsedPromotionCount();
    long getPromotionCountByType(Promotion.PromotionType type);
    
    // Date-based operations
    List<Promotion> getExpiringPromotions(int days);
    List<Promotion> getExpiredPromotions();
    List<Promotion> getPromotionsByDateRange(LocalDateTime startDate, LocalDateTime endDate);
    Page<Promotion> getPromotionsByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    
    // Usage operations
    List<Promotion> getFullyUsedPromotions();
    List<Promotion> getPromotionsExpiringSoon();
    List<Promotion> getTopUsedPromotions(int limit);
    List<Promotion> getLeastUsedPromotions(int limit);
    List<Promotion> getPromotionsByUsagePercentage(double percentage);
    Page<Promotion> getPromotionsByUsagePercentage(double percentage, Pageable pageable);
    
    // Business operations
    boolean applyPromotion(String promotionCode, double orderAmount);
    double calculateDiscount(String promotionCode, double orderAmount);
    Promotion togglePromotionStatus(Long promotionId);
    Promotion incrementUsage(Long promotionId);
    Promotion decrementUsage(Long promotionId);
    
    // Validation operations
    boolean validatePromotionCode(String code);
    boolean validatePromotionCodeForShop(Long shopId, String code);
    boolean validatePromotionDates(LocalDateTime startDate, LocalDateTime endDate);
    boolean validatePromotionAmounts(double discountValue, double minimumOrder, double maximumDiscount);
    
    // Bulk operations
    List<Promotion> toggleMultiplePromotions(List<Long> promotionIds, boolean active);
    void deleteMultiplePromotions(List<Long> promotionIds);
    List<Promotion> activateExpiredPromotions();
    List<Promotion> deactivateExpiredPromotions();
    
    // Export and reporting
    List<Promotion> getPromotionsForExport();
    List<Promotion> getPromotionsByStatus(String status);
    Page<Promotion> getPromotionsByStatus(String status, Pageable pageable);
    
    // ===== SHOP-SPECIFIC PROMOTION METHODS =====
    
    // Shop promotion CRUD operations
    List<Promotion> getPromotionsByShop(Long shopId);
    Page<Promotion> getPromotionsByShop(Long shopId, Pageable pageable);
    Optional<Promotion> getPromotionByShopAndId(Long shopId, Long promotionId);
    Optional<Promotion> getPromotionByShopAndCode(Long shopId, String code);
    Promotion createPromotionForShop(Long shopId, VendorPromotionForm form, User creator);
    Promotion updatePromotionForShop(Long shopId, Long promotionId, VendorPromotionForm form, User updater);
    void deletePromotionFromShop(Long shopId, Long promotionId);
    
    // Shop promotion search and filter
    Page<Promotion> searchPromotionsByShop(Long shopId, String name, String code, 
                                          Promotion.PromotionType type, Boolean active, Pageable pageable);
    List<Promotion> getActivePromotionsByShop(Long shopId);
    Page<Promotion> getActivePromotionsByShop(Long shopId, Pageable pageable);
    List<Promotion> getPromotionsByShopAndType(Long shopId, Promotion.PromotionType type);
    Page<Promotion> getPromotionsByShopAndType(Long shopId, Promotion.PromotionType type, Pageable pageable);
    List<Promotion> getPromotionsByShopAndStatus(Long shopId, Boolean active);
    Page<Promotion> getPromotionsByShopAndStatus(Long shopId, Boolean active, Pageable pageable);
    
    // Shop promotion statistics
    long getPromotionCountByShop(Long shopId);
    long getActivePromotionCountByShop(Long shopId);
    long getPromotionCountByShopAndType(Long shopId, Promotion.PromotionType type);
    
    // Shop promotion date operations
    List<Promotion> getExpiringPromotionsByShop(Long shopId, int days);
    List<Promotion> getExpiredPromotionsByShop(Long shopId);
    List<Promotion> getFullyUsedPromotionsByShop(Long shopId);
    
    // Shop promotion validation
    boolean isPromotionValidForShop(Long shopId, String promotionCode);
    boolean isPromotionAvailableForShop(Long shopId, String promotionCode);
    double calculateDiscountForShop(Long shopId, String promotionCode, double orderAmount);
    
    // Shop promotion business operations
    boolean applyPromotionForShop(Long shopId, String promotionCode, double orderAmount);
    Promotion incrementUsageForShop(Long shopId, Long promotionId);
    Promotion togglePromotionStatusForShop(Long shopId, Long promotionId);
    
    // Shop promotion bulk operations
    List<Promotion> toggleMultiplePromotionsForShop(Long shopId, List<Long> promotionIds, boolean active);
    void deleteMultiplePromotionsFromShop(Long shopId, List<Long> promotionIds);
}
