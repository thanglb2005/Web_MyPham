package vn.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.entity.Promotion;
import vn.repository.PromotionRepository;
import vn.service.PromotionService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PromotionServiceImpl implements PromotionService {
    
    @Autowired
    private PromotionRepository promotionRepository;
    
    // Basic CRUD operations
    @Override
    public List<Promotion> getAllPromotions() {
        return promotionRepository.findAll();
    }
    
    @Override
    public Page<Promotion> getPromotionsWithPagination(Pageable pageable) {
        return promotionRepository.findAll(pageable);
    }
    
    @Override
    public Optional<Promotion> getPromotionById(Long id) {
        return promotionRepository.findById(id);
    }
    
    @Override
    public Optional<Promotion> getPromotionByCode(String code) {
        return promotionRepository.findByPromotionCode(code);
    }
    
    @Override
    public Promotion savePromotion(Promotion promotion) {
        return promotionRepository.save(promotion);
    }
    
    @Override
    public void deletePromotion(Long id) {
        promotionRepository.deleteById(id);
    }
    
    @Override
    public Promotion updatePromotion(Promotion promotion) {
        return promotionRepository.save(promotion);
    }
    
    // Search and filter operations
    @Override
    public Page<Promotion> searchPromotionsByName(String name, Pageable pageable) {
        return promotionRepository.findByNameContainingIgnoreCase(name, pageable);
    }
    
    @Override
    public Page<Promotion> searchPromotionsByCode(String code, Pageable pageable) {
        return promotionRepository.findByCodeContainingIgnoreCase(code, pageable);
    }
    
    @Override
    public List<Promotion> getPromotionsByType(Promotion.PromotionType type) {
        return promotionRepository.findByPromotionType(type);
    }
    
    @Override
    public Page<Promotion> getPromotionsByType(Promotion.PromotionType type, Pageable pageable) {
        return promotionRepository.findByPromotionType(type, pageable);
    }
    
    @Override
    public List<Promotion> getActivePromotions() {
        return promotionRepository.findActivePromotions(LocalDateTime.now());
    }
    
    @Override
    public Page<Promotion> getActivePromotions(Pageable pageable) {
        return promotionRepository.findActivePromotions(LocalDateTime.now(), pageable);
    }
    
    @Override
    public List<Promotion> getInactivePromotions() {
        return promotionRepository.findByIsActive(false);
    }
    
    @Override
    public Page<Promotion> getInactivePromotions(Pageable pageable) {
        return promotionRepository.findByIsActive(false, pageable);
    }
    
    // Advanced search
    @Override
    public Page<Promotion> searchPromotions(String name, String code, Promotion.PromotionType type, Boolean active, Pageable pageable) {
        return promotionRepository.searchPromotions(name, code, type, active, pageable);
    }
    
    // Status and validation operations
    @Override
    public boolean isPromotionValid(String promotionCode) {
        Optional<Promotion> promotion = promotionRepository.findByPromotionCode(promotionCode);
        return promotion.isPresent() && promotion.get().isAvailable();
    }
    
    @Override
    public boolean isPromotionExpired(Long promotionId) {
        Optional<Promotion> promotion = promotionRepository.findById(promotionId);
        return promotion.isPresent() && promotion.get().isExpired();
    }
    
    @Override
    public boolean isPromotionFullyUsed(Long promotionId) {
        Optional<Promotion> promotion = promotionRepository.findById(promotionId);
        return promotion.isPresent() && promotion.get().isFullyUsed();
    }
    
    @Override
    public boolean isPromotionAvailable(Long promotionId) {
        Optional<Promotion> promotion = promotionRepository.findById(promotionId);
        return promotion.isPresent() && promotion.get().isAvailable();
    }
    
    // Statistics and analytics
    @Override
    public long getTotalPromotionCount() {
        return promotionRepository.count();
    }
    
    @Override
    public long getActivePromotionCount() {
        return promotionRepository.countActivePromotions(LocalDateTime.now());
    }
    
    @Override
    public long getInactivePromotionCount() {
        return promotionRepository.findByIsActive(false).size();
    }
    
    @Override
    public long getExpiredPromotionCount() {
        return promotionRepository.findExpiredPromotions(LocalDateTime.now()).size();
    }
    
    @Override
    public long getFullyUsedPromotionCount() {
        return promotionRepository.findFullyUsedPromotions().size();
    }
    
    @Override
    public long getPromotionCountByType(Promotion.PromotionType type) {
        return promotionRepository.countByPromotionType(type);
    }
    
    // Date-based operations
    @Override
    public List<Promotion> getExpiringPromotions(int days) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiringDate = now.plusDays(days);
        return promotionRepository.findExpiringPromotions(now, expiringDate);
    }
    
    @Override
    public List<Promotion> getExpiredPromotions() {
        return promotionRepository.findExpiredPromotions(LocalDateTime.now());
    }
    
    @Override
    public List<Promotion> getPromotionsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return promotionRepository.findByDateRange(startDate, endDate);
    }
    
    @Override
    public Page<Promotion> getPromotionsByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return promotionRepository.findByDateRange(startDate, endDate, pageable);
    }
    
    // Usage operations
    @Override
    public List<Promotion> getFullyUsedPromotions() {
        return promotionRepository.findFullyUsedPromotions();
    }
    
    @Override
    public List<Promotion> getPromotionsExpiringSoon() {
        return getExpiringPromotions(7);
    }
    
    @Override
    public List<Promotion> getTopUsedPromotions(int limit) {
        return promotionRepository.findTopUsedPromotions(PageRequest.of(0, limit));
    }
    
    @Override
    public List<Promotion> getLeastUsedPromotions(int limit) {
        return promotionRepository.findLeastUsedPromotions(PageRequest.of(0, limit));
    }
    
    @Override
    public List<Promotion> getPromotionsByUsagePercentage(double percentage) {
        return promotionRepository.findByUsagePercentage(percentage);
    }
    
    @Override
    public Page<Promotion> getPromotionsByUsagePercentage(double percentage, Pageable pageable) {
        return promotionRepository.findByUsagePercentage(percentage, pageable);
    }
    
    // Business operations
    @Override
    public boolean applyPromotion(String promotionCode, double orderAmount) {
        Optional<Promotion> promotionOpt = promotionRepository.findByPromotionCode(promotionCode);
        if (promotionOpt.isEmpty() || !promotionOpt.get().isAvailable()) {
            return false;
        }
        
        Promotion promotion = promotionOpt.get();
        if (orderAmount < promotion.getMinimumOrderAmount().doubleValue()) {
            return false;
        }
        
        promotion.setUsedCount(promotion.getUsedCount() + 1);
        promotionRepository.save(promotion);
        return true;
    }
    
    @Override
    public double calculateDiscount(String promotionCode, double orderAmount) {
        Optional<Promotion> promotionOpt = promotionRepository.findByPromotionCode(promotionCode);
        if (promotionOpt.isEmpty() || !promotionOpt.get().isAvailable()) {
            return 0.0;
        }
        
        Promotion promotion = promotionOpt.get();
        if (orderAmount < promotion.getMinimumOrderAmount().doubleValue()) {
            return 0.0;
        }
        
        double discount = 0.0;
        switch (promotion.getPromotionType()) {
            case PRODUCT_PERCENTAGE:
                discount = orderAmount * (promotion.getDiscountValue().doubleValue() / 100);
                break;
            case FIXED_AMOUNT:
                discount = promotion.getDiscountValue().doubleValue();
                break;
            case SHIPPING_DISCOUNT:
                discount = promotion.getDiscountValue().doubleValue();
                break;
        }
        
        // Apply maximum discount limit
        if (discount > promotion.getMaximumDiscountAmount().doubleValue()) {
            discount = promotion.getMaximumDiscountAmount().doubleValue();
        }
        
        return discount;
    }
    
    @Override
    public Promotion togglePromotionStatus(Long promotionId) {
        Optional<Promotion> promotionOpt = promotionRepository.findById(promotionId);
        if (promotionOpt.isEmpty()) {
            throw new RuntimeException("Promotion not found with id: " + promotionId);
        }
        
        Promotion promotion = promotionOpt.get();
        promotion.setIsActive(!promotion.getIsActive());
        return promotionRepository.save(promotion);
    }
    
    @Override
    public Promotion incrementUsage(Long promotionId) {
        Optional<Promotion> promotionOpt = promotionRepository.findById(promotionId);
        if (promotionOpt.isEmpty()) {
            throw new RuntimeException("Promotion not found with id: " + promotionId);
        }
        
        Promotion promotion = promotionOpt.get();
        promotion.setUsedCount(promotion.getUsedCount() + 1);
        return promotionRepository.save(promotion);
    }
    
    @Override
    public Promotion decrementUsage(Long promotionId) {
        Optional<Promotion> promotionOpt = promotionRepository.findById(promotionId);
        if (promotionOpt.isEmpty()) {
            throw new RuntimeException("Promotion not found with id: " + promotionId);
        }
        
        Promotion promotion = promotionOpt.get();
        if (promotion.getUsedCount() > 0) {
            promotion.setUsedCount(promotion.getUsedCount() - 1);
        }
        return promotionRepository.save(promotion);
    }
    
    // Validation operations
    @Override
    public boolean validatePromotionCode(String code) {
        return !promotionRepository.findByPromotionCode(code).isPresent();
    }
    
    @Override
    public boolean validatePromotionDates(LocalDateTime startDate, LocalDateTime endDate) {
        return endDate.isAfter(startDate);
    }
    
    @Override
    public boolean validatePromotionAmounts(double discountValue, double minimumOrder, double maximumDiscount) {
        return discountValue >= 0 && minimumOrder >= 0 && maximumDiscount >= 0 && discountValue <= maximumDiscount;
    }
    
    // Bulk operations
    @Override
    public List<Promotion> toggleMultiplePromotions(List<Long> promotionIds, boolean active) {
        List<Promotion> promotions = promotionRepository.findAllById(promotionIds);
        for (Promotion promotion : promotions) {
            promotion.setIsActive(active);
        }
        return promotionRepository.saveAll(promotions);
    }
    
    @Override
    public void deleteMultiplePromotions(List<Long> promotionIds) {
        promotionRepository.deleteAllById(promotionIds);
    }
    
    @Override
    public List<Promotion> activateExpiredPromotions() {
        List<Promotion> expiredPromotions = getExpiredPromotions();
        for (Promotion promotion : expiredPromotions) {
            promotion.setIsActive(false);
        }
        return promotionRepository.saveAll(expiredPromotions);
    }
    
    @Override
    public List<Promotion> deactivateExpiredPromotions() {
        return activateExpiredPromotions();
    }
    
    // Export and reporting
    @Override
    public List<Promotion> getPromotionsForExport() {
        return promotionRepository.findAll();
    }
    
    @Override
    public List<Promotion> getPromotionsByStatus(String status) {
        switch (status.toLowerCase()) {
            case "active":
                return getActivePromotions();
            case "inactive":
                return getInactivePromotions();
            case "expired":
                return getExpiredPromotions();
            case "fully_used":
                return getFullyUsedPromotions();
            default:
                return getAllPromotions();
        }
    }
    
    @Override
    public Page<Promotion> getPromotionsByStatus(String status, Pageable pageable) {
        switch (status.toLowerCase()) {
            case "active":
                return getActivePromotions(pageable);
            case "inactive":
                return getInactivePromotions(pageable);
            default:
                return getPromotionsWithPagination(pageable);
        }
    }
}
