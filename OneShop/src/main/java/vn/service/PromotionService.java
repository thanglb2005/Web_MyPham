package vn.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.entity.Promotion;

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
}
