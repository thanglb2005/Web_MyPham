package vn.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.dto.VendorPromotionForm;
import vn.entity.Promotion;
import vn.entity.Shop;
import vn.entity.User;
import vn.repository.PromotionRepository;
import vn.repository.ShopRepository;
import vn.service.PromotionService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PromotionServiceImpl implements PromotionService {
    
    @Autowired
    private PromotionRepository promotionRepository;
    
    @Autowired
    private ShopRepository shopRepository;
    
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
    
    @Override
    public Promotion updatePromotion(Long id, VendorPromotionForm form, User actor) {
        Promotion existing = promotionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Promotion not found with id: " + id));
        
        // Update fields from form
        existing.setPromotionName(form.getPromotionName());
        existing.setDescription(form.getDescription());
        existing.setPromotionCode(form.getPromotionCode());
        existing.setPromotionType(form.getPromotionType());
        existing.setDiscountValue(form.getDiscountValue());
        existing.setMinimumOrderAmount(form.getMinimumOrderAmount());
        existing.setMaximumDiscountAmount(form.getMaximumDiscountAmount());
        existing.setUsageLimit(form.getUsageLimit());
        existing.setStartDate(form.getStartDate());
        existing.setEndDate(form.getEndDate());
        existing.setIsActive(form.getIsActive());
        
        return promotionRepository.save(existing);
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
    public List<Promotion> getAllActivePromotions() {
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
            case PERCENTAGE:
            case PRODUCT_PERCENTAGE:
                discount = orderAmount * (promotion.getDiscountValue().doubleValue() / 100);
                break;
            case FIXED_AMOUNT:
                discount = promotion.getDiscountValue().doubleValue();
                break;
            case FREE_SHIPPING:
                discount = promotion.getDiscountValue().doubleValue();
                break;
            case BUY_X_GET_Y:
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
    public boolean validatePromotionCodeForShop(Long shopId, String code) {
        return !promotionRepository.findByShopAndCode(shopId, code).isPresent();
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
    
    // ===== SHOP-SPECIFIC PROMOTION METHODS =====
    
    @Override
    public List<Promotion> getPromotionsByShop(Long shopId) {
        return promotionRepository.findByShopShopId(shopId);
    }
    
    @Override
    public Page<Promotion> getPromotionsByShop(Long shopId, Pageable pageable) {
        return promotionRepository.findByShopShopId(shopId, pageable);
    }
    
    @Override
    public Optional<Promotion> getPromotionByShopAndId(Long shopId, Long promotionId) {
        return promotionRepository.findById(promotionId)
                .filter(p -> p.getShop().getShopId().equals(shopId));
    }
    
    @Override
    public Optional<Promotion> getPromotionByShopAndCode(Long shopId, String code) {
        return promotionRepository.findByShopAndCode(shopId, code);
    }
    
    @Override
    public Promotion createPromotionForShop(Long shopId, VendorPromotionForm form, User creator) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new RuntimeException("Shop not found with id: " + shopId));
        
        Promotion promotion = new Promotion();
        promotion.setPromotionName(form.getPromotionName());
        promotion.setDescription(form.getDescription());
        promotion.setPromotionCode(form.getPromotionCode());
        promotion.setPromotionType(form.getPromotionType());
        promotion.setDiscountValue(form.getDiscountValue());
        promotion.setMinimumOrderAmount(form.getMinimumOrderAmount());
        promotion.setMaximumDiscountAmount(form.getMaximumDiscountAmount());
        promotion.setUsageLimit(form.getUsageLimit());
        promotion.setStartDate(form.getStartDate());
        promotion.setEndDate(form.getEndDate());
        promotion.setIsActive(form.getIsActive());
        promotion.setShop(shop);
        promotion.setCreatedBy(creator);
        promotion.setUsedCount(0);
        
        return promotionRepository.save(promotion);
    }
    
    @Override
    public Promotion updatePromotionForShop(Long shopId, Long promotionId, VendorPromotionForm form, User updater) {
        Promotion promotion = getPromotionByShopAndId(shopId, promotionId)
                .orElseThrow(() -> new RuntimeException("Promotion not found for shop: " + shopId + " with id: " + promotionId));
        
        promotion.setPromotionName(form.getPromotionName());
        promotion.setDescription(form.getDescription());
        promotion.setPromotionCode(form.getPromotionCode());
        promotion.setPromotionType(form.getPromotionType());
        promotion.setDiscountValue(form.getDiscountValue());
        promotion.setMinimumOrderAmount(form.getMinimumOrderAmount());
        promotion.setMaximumDiscountAmount(form.getMaximumDiscountAmount());
        promotion.setUsageLimit(form.getUsageLimit());
        promotion.setStartDate(form.getStartDate());
        promotion.setEndDate(form.getEndDate());
        promotion.setIsActive(form.getIsActive());
        
        return promotionRepository.save(promotion);
    }
    
    @Override
    public void deletePromotionFromShop(Long shopId, Long promotionId) {
        Promotion promotion = getPromotionByShopAndId(shopId, promotionId)
                .orElseThrow(() -> new RuntimeException("Promotion not found for shop: " + shopId + " with id: " + promotionId));
        promotionRepository.delete(promotion);
    }
    
    @Override
    public Page<Promotion> searchPromotionsByShop(Long shopId, String name, String code, 
                                                Promotion.PromotionType type, Boolean active, Pageable pageable) {
        return promotionRepository.searchPromotionsByShop(shopId, name, code, type, active, pageable);
    }
    
    @Override
    public List<Promotion> getActivePromotionsByShop(Long shopId) {
        return promotionRepository.findActivePromotionsByShop(shopId, LocalDateTime.now());
    }
    
    @Override
    public Page<Promotion> getActivePromotionsByShop(Long shopId, Pageable pageable) {
        return promotionRepository.findActivePromotionsByShop(shopId, LocalDateTime.now(), pageable);
    }
    
    @Override
    public List<Promotion> getPromotionsByShopAndType(Long shopId, Promotion.PromotionType type) {
        return promotionRepository.findByShopAndType(shopId, type);
    }
    
    @Override
    public Page<Promotion> getPromotionsByShopAndType(Long shopId, Promotion.PromotionType type, Pageable pageable) {
        return promotionRepository.findByShopAndType(shopId, type, pageable);
    }
    
    @Override
    public List<Promotion> getPromotionsByShopAndStatus(Long shopId, Boolean active) {
        return promotionRepository.findByShopAndStatus(shopId, active);
    }
    
    @Override
    public Page<Promotion> getPromotionsByShopAndStatus(Long shopId, Boolean active, Pageable pageable) {
        return promotionRepository.findByShopAndStatus(shopId, active, pageable);
    }
    
    @Override
    public long getPromotionCountByShop(Long shopId) {
        return promotionRepository.countByShopShopId(shopId);
    }
    
    @Override
    public long getActivePromotionCountByShop(Long shopId) {
        return promotionRepository.countActivePromotionsByShop(shopId, LocalDateTime.now());
    }
    
    @Override
    public long getPromotionCountByShopAndType(Long shopId, Promotion.PromotionType type) {
        return promotionRepository.findByShopAndType(shopId, type).size();
    }
    
    @Override
    public List<Promotion> getExpiringPromotionsByShop(Long shopId, int days) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiringDate = now.plusDays(days);
        return promotionRepository.findExpiringPromotionsByShop(shopId, now, expiringDate);
    }
    
    @Override
    public List<Promotion> getExpiredPromotionsByShop(Long shopId) {
        return promotionRepository.findExpiredPromotionsByShop(shopId, LocalDateTime.now());
    }
    
    @Override
    public List<Promotion> getFullyUsedPromotionsByShop(Long shopId) {
        return promotionRepository.findFullyUsedPromotionsByShop(shopId);
    }
    
    @Override
    public boolean isPromotionValidForShop(Long shopId, String promotionCode) {
        Optional<Promotion> promotion = promotionRepository.findByShopAndCode(shopId, promotionCode);
        return promotion.isPresent() && promotion.get().isAvailable();
    }
    
    @Override
    public boolean isPromotionAvailableForShop(Long shopId, String promotionCode) {
        return isPromotionValidForShop(shopId, promotionCode);
    }
    
    @Override
    public double calculateDiscountForShop(Long shopId, String promotionCode, double orderAmount) {
        Optional<Promotion> promotionOpt = promotionRepository.findByShopAndCode(shopId, promotionCode);
        if (promotionOpt.isEmpty() || !promotionOpt.get().isAvailable()) {
            return 0.0;
        }
        
        Promotion promotion = promotionOpt.get();
        if (orderAmount < promotion.getMinimumOrderAmount().doubleValue()) {
            return 0.0;
        }
        
        double discount = 0.0;
        switch (promotion.getPromotionType()) {
            case PERCENTAGE:
            case PRODUCT_PERCENTAGE:
                discount = orderAmount * (promotion.getDiscountValue().doubleValue() / 100);
                break;
            case FIXED_AMOUNT:
                discount = promotion.getDiscountValue().doubleValue();
                break;
            case FREE_SHIPPING:
                discount = promotion.getDiscountValue().doubleValue();
                break;
            case BUY_X_GET_Y:
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
    public boolean applyPromotionForShop(Long shopId, String promotionCode, double orderAmount) {
        Optional<Promotion> promotionOpt = promotionRepository.findByShopAndCode(shopId, promotionCode);
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
    public Promotion incrementUsageForShop(Long shopId, Long promotionId) {
        Promotion promotion = getPromotionByShopAndId(shopId, promotionId)
                .orElseThrow(() -> new RuntimeException("Promotion not found for shop: " + shopId + " with id: " + promotionId));
        
        promotion.setUsedCount(promotion.getUsedCount() + 1);
        return promotionRepository.save(promotion);
    }
    
    @Override
    public Promotion togglePromotionStatusForShop(Long shopId, Long promotionId) {
        Promotion promotion = getPromotionByShopAndId(shopId, promotionId)
                .orElseThrow(() -> new RuntimeException("Promotion not found for shop: " + shopId + " with id: " + promotionId));
        
        promotion.setIsActive(!promotion.getIsActive());
        return promotionRepository.save(promotion);
    }
    
    @Override
    public List<Promotion> toggleMultiplePromotionsForShop(Long shopId, List<Long> promotionIds, boolean active) {
        List<Promotion> promotions = promotionRepository.findAllById(promotionIds);
        List<Promotion> shopPromotions = promotions.stream()
                .filter(p -> p.getShop().getShopId().equals(shopId))
                .toList();
        
        for (Promotion promotion : shopPromotions) {
            promotion.setIsActive(active);
        }
        return promotionRepository.saveAll(shopPromotions);
    }
    
    @Override
    public void deleteMultiplePromotionsFromShop(Long shopId, List<Long> promotionIds) {
        List<Promotion> promotions = promotionRepository.findAllById(promotionIds);
        List<Promotion> shopPromotions = promotions.stream()
                .filter(p -> p.getShop().getShopId().equals(shopId))
                .toList();
        
        promotionRepository.deleteAll(shopPromotions);
    }
}
