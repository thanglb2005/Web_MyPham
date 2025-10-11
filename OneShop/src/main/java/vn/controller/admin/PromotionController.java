package vn.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.entity.Promotion;
import vn.service.PromotionService;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class PromotionController {
    
    @Autowired
    private PromotionService promotionService;
    
    /**
     * Display all promotions with pagination
     */
    @GetMapping("/promotions")
    public String promotions(Model model, 
                           @RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "10") int size,
                           @RequestParam(defaultValue = "promotionId") String sortBy,
                           @RequestParam(defaultValue = "desc") String sortDir,
                           @RequestParam(required = false) String search) {
        
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<Promotion> promotions;
            if (search != null && !search.trim().isEmpty()) {
                promotions = promotionService.searchPromotionsByName(search, pageable);
                model.addAttribute("search", search);
            } else {
                promotions = promotionService.getPromotionsWithPagination(pageable);
            }
            
            model.addAttribute("promotions", promotions);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", promotions != null ? promotions.getTotalPages() : 0);
            model.addAttribute("totalElements", promotions != null ? promotions.getTotalElements() : 0);
            model.addAttribute("sortBy", sortBy);
            model.addAttribute("sortDir", sortDir);
            model.addAttribute("size", size);
            
            // Statistics
            model.addAttribute("activeCount", promotionService.getActivePromotionCount());
            model.addAttribute("expiringSoon", promotionService.getPromotionsExpiringSoon().size());
            model.addAttribute("fullyUsed", promotionService.getFullyUsedPromotions().size());
            
            return "admin/promotions";
        } catch (Exception e) {
            System.err.println("Error in promotions controller: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Có lỗi xảy ra khi tải dữ liệu khuyến mãi: " + e.getMessage());
            return "admin/promotions";
        }
    }
    
    /**
     * Display promotion form for adding new promotion
     */
    @GetMapping("/promotions/add")
    public String addPromotionForm(Model model) {
        model.addAttribute("promotion", new Promotion());
        model.addAttribute("promotionTypes", Promotion.PromotionType.values());
        return "admin/promotion-form";
    }
    
    /**
     * Handle adding new promotion
     */
    @PostMapping("/promotions/add")
    public String addPromotion(@ModelAttribute Promotion promotion, 
                              RedirectAttributes redirectAttributes) {
        try {
            // Validate promotion code uniqueness
            if (!promotionService.validatePromotionCode(promotion.getPromotionCode())) {
                redirectAttributes.addFlashAttribute("error", "Mã khuyến mãi đã tồn tại!");
                return "redirect:/admin/promotions/add";
            }
            
            // Validate dates
            if (!promotionService.validatePromotionDates(promotion.getStartDate(), promotion.getEndDate())) {
                redirectAttributes.addFlashAttribute("error", "Ngày kết thúc phải sau ngày bắt đầu!");
                return "redirect:/admin/promotions/add";
            }
            
            // Validate amounts
            if (!promotionService.validatePromotionAmounts(
                promotion.getDiscountValue().doubleValue(),
                promotion.getMinimumOrderAmount().doubleValue(),
                promotion.getMaximumDiscountAmount().doubleValue())) {
                redirectAttributes.addFlashAttribute("error", "Giá trị giảm không hợp lệ!");
                return "redirect:/admin/promotions/add";
            }
            
            promotionService.savePromotion(promotion);
            redirectAttributes.addFlashAttribute("success", "Thêm khuyến mãi thành công!");
            return "redirect:/admin/promotions";
        } catch (Exception e) {
            System.err.println("Error adding promotion: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi thêm khuyến mãi: " + e.getMessage());
            return "redirect:/admin/promotions/add";
        }
    }
    
    /**
     * Display promotion form for editing
     */
    @GetMapping("/promotions/edit/{id}")
    public String editPromotionForm(@PathVariable Long id, Model model) {
        try {
            Optional<Promotion> promotion = promotionService.getPromotionById(id);
            if (promotion.isPresent()) {
                model.addAttribute("promotion", promotion.get());
                model.addAttribute("promotionTypes", Promotion.PromotionType.values());
                return "admin/promotion-form";
            } else {
                return "redirect:/admin/promotions";
            }
        } catch (Exception e) {
            System.err.println("Error loading promotion for edit: " + e.getMessage());
            return "redirect:/admin/promotions";
        }
    }
    
    /**
     * Handle updating promotion
     */
    @PostMapping("/promotions/edit/{id}")
    public String updatePromotion(@PathVariable Long id, 
                                 @ModelAttribute Promotion promotion,
                                 RedirectAttributes redirectAttributes) {
        try {
            Optional<Promotion> existingPromotion = promotionService.getPromotionById(id);
            if (existingPromotion.isPresent()) {
                Promotion existing = existingPromotion.get();
                
                // Update fields
                existing.setPromotionName(promotion.getPromotionName());
                existing.setDescription(promotion.getDescription());
                existing.setPromotionCode(promotion.getPromotionCode());
                existing.setPromotionType(promotion.getPromotionType());
                existing.setDiscountValue(promotion.getDiscountValue());
                existing.setMinimumOrderAmount(promotion.getMinimumOrderAmount());
                existing.setMaximumDiscountAmount(promotion.getMaximumDiscountAmount());
                existing.setUsageLimit(promotion.getUsageLimit());
                existing.setStartDate(promotion.getStartDate());
                existing.setEndDate(promotion.getEndDate());
                existing.setIsActive(promotion.getIsActive());
                
                // Validate dates
                if (!promotionService.validatePromotionDates(existing.getStartDate(), existing.getEndDate())) {
                    redirectAttributes.addFlashAttribute("error", "Ngày kết thúc phải sau ngày bắt đầu!");
                    return "redirect:/admin/promotions/edit/" + id;
                }
                
                // Validate amounts
                if (!promotionService.validatePromotionAmounts(
                    existing.getDiscountValue().doubleValue(),
                    existing.getMinimumOrderAmount().doubleValue(),
                    existing.getMaximumDiscountAmount().doubleValue())) {
                    redirectAttributes.addFlashAttribute("error", "Giá trị giảm không hợp lệ!");
                    return "redirect:/admin/promotions/edit/" + id;
                }
                
                promotionService.updatePromotion(existing);
                redirectAttributes.addFlashAttribute("success", "Cập nhật khuyến mãi thành công!");
            } else {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy khuyến mãi!");
            }
            return "redirect:/admin/promotions";
        } catch (Exception e) {
            System.err.println("Error updating promotion: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi cập nhật khuyến mãi: " + e.getMessage());
            return "redirect:/admin/promotions/edit/" + id;
        }
    }
    
    /**
     * View promotion details
     */
    @GetMapping("/promotions/view/{id}")
    public String viewPromotion(@PathVariable Long id, Model model) {
        try {
            Optional<Promotion> promotion = promotionService.getPromotionById(id);
            if (promotion.isPresent()) {
                model.addAttribute("promotion", promotion.get());
                return "admin/promotion-detail";
            } else {
                return "redirect:/admin/promotions";
            }
        } catch (Exception e) {
            System.err.println("Error viewing promotion: " + e.getMessage());
            return "redirect:/admin/promotions";
        }
    }
    
    /**
     * Toggle promotion status (active/inactive)
     */
    @GetMapping("/promotions/toggle/{id}")
    public String togglePromotion(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Promotion promotion = promotionService.togglePromotionStatus(id);
            String status = promotion.getIsActive() ? "kích hoạt" : "tạm dừng";
            redirectAttributes.addFlashAttribute("success", "Đã " + status + " khuyến mãi thành công!");
        } catch (Exception e) {
            System.err.println("Error toggling promotion: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi thay đổi trạng thái khuyến mãi: " + e.getMessage());
        }
        return "redirect:/admin/promotions";
    }
    
    /**
     * Delete promotion
     */
    @GetMapping("/promotions/delete/{id}")
    public String deletePromotion(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            promotionService.deletePromotion(id);
            redirectAttributes.addFlashAttribute("success", "Xóa khuyến mãi thành công!");
        } catch (Exception e) {
            System.err.println("Error deleting promotion: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi xóa khuyến mãi: " + e.getMessage());
        }
        return "redirect:/admin/promotions";
    }
    
    /**
     * Bulk operations
     */
    @PostMapping("/promotions/bulk-action")
    public String bulkAction(@RequestParam String action, 
                           @RequestParam List<Long> promotionIds,
                           RedirectAttributes redirectAttributes) {
        try {
            switch (action) {
                case "activate":
                    promotionService.toggleMultiplePromotions(promotionIds, true);
                    redirectAttributes.addFlashAttribute("success", "Đã kích hoạt " + promotionIds.size() + " khuyến mãi!");
                    break;
                case "deactivate":
                    promotionService.toggleMultiplePromotions(promotionIds, false);
                    redirectAttributes.addFlashAttribute("success", "Đã tạm dừng " + promotionIds.size() + " khuyến mãi!");
                    break;
                case "delete":
                    promotionService.deleteMultiplePromotions(promotionIds);
                    redirectAttributes.addFlashAttribute("success", "Đã xóa " + promotionIds.size() + " khuyến mãi!");
                    break;
                default:
                    redirectAttributes.addFlashAttribute("error", "Hành động không hợp lệ!");
            }
        } catch (Exception e) {
            System.err.println("Error in bulk action: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi thực hiện hành động: " + e.getMessage());
        }
        return "redirect:/admin/promotions";
    }
    
    /**
     * Get promotions by status
     */
    @GetMapping("/promotions/status/{status}")
    public String getPromotionsByStatus(@PathVariable String status, 
                                     @RequestParam(defaultValue = "0") int page,
                                     @RequestParam(defaultValue = "10") int size,
                                     Model model) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Promotion> promotions = promotionService.getPromotionsByStatus(status, pageable);
            
            model.addAttribute("promotions", promotions);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", promotions.getTotalPages());
            model.addAttribute("totalElements", promotions.getTotalElements());
            model.addAttribute("status", status);
            model.addAttribute("size", size);
            
            return "admin/promotions";
        } catch (Exception e) {
            System.err.println("Error getting promotions by status: " + e.getMessage());
            return "redirect:/admin/promotions";
        }
    }
    
    /**
     * Get promotions by type
     */
    @GetMapping("/promotions/type/{type}")
    public String getPromotionsByType(@PathVariable String type, 
                                    @RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "10") int size,
                                    Model model) {
        try {
            Promotion.PromotionType promotionType = Promotion.PromotionType.valueOf(type.toUpperCase());
            Pageable pageable = PageRequest.of(page, size);
            Page<Promotion> promotions = promotionService.getPromotionsByType(promotionType, pageable);
            
            model.addAttribute("promotions", promotions);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", promotions.getTotalPages());
            model.addAttribute("totalElements", promotions.getTotalElements());
            model.addAttribute("type", type);
            model.addAttribute("size", size);
            
            return "admin/promotions";
        } catch (Exception e) {
            System.err.println("Error getting promotions by type: " + e.getMessage());
            return "redirect:/admin/promotions";
        }
    }
    
    /**
     * Active promotions
     */
    @GetMapping("/promotions/active")
    public String activePromotions(Model model, 
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Promotion> promotions = promotionService.getActivePromotions(pageable);
            
            model.addAttribute("promotions", promotions);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", promotions.getTotalPages());
            model.addAttribute("totalElements", promotions.getTotalElements());
            model.addAttribute("size", size);
            model.addAttribute("filter", "active");
            
            return "admin/promotions";
        } catch (Exception e) {
            System.err.println("Error loading active promotions: " + e.getMessage());
            return "redirect:/admin/promotions";
        }
    }
    
    /**
     * Expired promotions
     */
    @GetMapping("/promotions/expired")
    public String expiredPromotions(Model model, 
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            List<Promotion> expiredList = promotionService.getExpiredPromotions();
            
            // Convert to page manually
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), expiredList.size());
            List<Promotion> pageContent = expiredList.subList(start, end);
            Page<Promotion> promotions = new org.springframework.data.domain.PageImpl<>(pageContent, pageable, expiredList.size());
            
            model.addAttribute("promotions", promotions);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", promotions.getTotalPages());
            model.addAttribute("totalElements", promotions.getTotalElements());
            model.addAttribute("size", size);
            model.addAttribute("filter", "expired");
            
            return "admin/promotions";
        } catch (Exception e) {
            System.err.println("Error loading expired promotions: " + e.getMessage());
            return "redirect:/admin/promotions";
        }
    }
}
