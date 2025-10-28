package vn.controller.vendor;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.dto.VendorPromotionForm;
import vn.entity.Promotion;
import vn.entity.Shop;
import vn.entity.User;
import vn.service.PromotionService;
import vn.service.ShopService;
import vn.util.UserUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/vendor/promotions")
public class VendorPromotionController {
    
    @Autowired
    private PromotionService promotionService;
    
    @Autowired
    private ShopService shopService;
    
    // Danh sách khuyến mãi của shop
    @GetMapping
    public String listPromotions(@RequestParam(value = "shopId", required = false) Long shopId,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "10") int size,
                                @RequestParam(required = false) String name,
                                @RequestParam(required = false) String code,
                                @RequestParam(required = false) String type,
                                @RequestParam(required = false) String status,
                                HttpSession session,
                                Model model) {
        
        User vendor = UserUtils.getCurrentUser(session);
        if (vendor == null || !UserUtils.isVendor(vendor)) {
            return "redirect:/login";
        }
        
        List<Shop> vendorShops = shopService.findAllByVendor(vendor);
        if (vendorShops.isEmpty()) {
            model.addAttribute("error", "Bạn chưa có shop nào để quản lý khuyến mãi.");
            return "vendor/promotions/list";
        }
        
        final Long currentShopId = shopId;
        Shop selectedShop = null;
        if (currentShopId != null) {
            selectedShop = vendorShops.stream()
                                    .filter(s -> s.getShopId().equals(currentShopId))
                                    .findFirst()
                                    .orElse(null);
        }
        
        // Nếu không có shopId hoặc shopId không hợp lệ, chọn shop đầu tiên
        if (selectedShop == null) {
            selectedShop = vendorShops.get(0);
        }
        
        Long finalShopId = selectedShop.getShopId();
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        
        Promotion.PromotionType promotionType = null;
        if (type != null && !type.isEmpty()) {
            try {
                promotionType = Promotion.PromotionType.valueOf(type.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Invalid type, ignore
            }
        }
        
        Boolean activeStatus = null;
        if (status != null && !status.isEmpty()) {
            if ("active".equals(status)) {
                activeStatus = true;
            } else if ("inactive".equals(status)) {
                activeStatus = false;
            }
        }
        
        Page<Promotion> promotions = promotionService.searchPromotionsByShop(
                finalShopId, name, code, promotionType, activeStatus, pageable);
        
        // Thống kê
        long totalPromotions = promotionService.getPromotionCountByShop(finalShopId);
        long activePromotions = promotionService.getActivePromotionCountByShop(finalShopId);
        List<Promotion> expiringPromotions = promotionService.getExpiringPromotionsByShop(finalShopId, 7);
        List<Promotion> expiredPromotions = promotionService.getExpiredPromotionsByShop(finalShopId);
        List<Promotion> fullyUsedPromotions = promotionService.getFullyUsedPromotionsByShop(finalShopId);
        
        model.addAttribute("promotions", promotions);
        model.addAttribute("vendorShops", vendorShops);
        model.addAttribute("selectedShop", selectedShop);
        model.addAttribute("selectedShopId", finalShopId);
        model.addAttribute("totalPromotions", totalPromotions);
        model.addAttribute("activePromotions", activePromotions);
        model.addAttribute("expiringPromotions", expiringPromotions);
        model.addAttribute("expiredPromotions", expiredPromotions);
        model.addAttribute("fullyUsedPromotions", fullyUsedPromotions);
        model.addAttribute("promotionTypes", Promotion.PromotionType.values());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", promotions.getTotalPages());
        model.addAttribute("name", name);
        model.addAttribute("code", code);
        model.addAttribute("type", type);
        model.addAttribute("status", status);
        
        return "vendor/promotions/list";
    }
    
    // Form tạo khuyến mãi mới
    @GetMapping("/create")
    public String createPromotionForm(@RequestParam(required = false) Long shopId,
                                     HttpSession session, 
                                     Model model) {
        User vendor = UserUtils.getCurrentUser(session);
        if (vendor == null || !UserUtils.isVendor(vendor)) {
            return "redirect:/login";
        }
        
        List<Long> shopIds = shopService.findShopIdsByVendor(vendor);
        if (shopIds.isEmpty()) {
            return "redirect:/vendor/shops";
        }
        
        // Nếu không có shopId từ param, lấy shop đầu tiên
        if (shopId == null) {
            shopId = shopIds.get(0);
        }
        
        // Kiểm tra shop có thuộc vendor không
        if (!shopIds.contains(shopId)) {
            return "redirect:/vendor/shops";
        }
        
        Shop shop = shopService.findById(shopId).orElse(null);
        
        VendorPromotionForm form = new VendorPromotionForm();
        form.setStartDate(LocalDateTime.now());
        form.setEndDate(LocalDateTime.now().plusDays(30));
        form.setUsageLimit(100);
        form.setMinimumOrderAmount(java.math.BigDecimal.valueOf(0));
        form.setMaximumDiscountAmount(java.math.BigDecimal.valueOf(1000000));
        
        model.addAttribute("promotionForm", form);
        model.addAttribute("shop", shop);
        model.addAttribute("shopId", shopId);
        model.addAttribute("promotionTypes", new Promotion.PromotionType[]{
                Promotion.PromotionType.PERCENTAGE,
                Promotion.PromotionType.FIXED_AMOUNT,
                Promotion.PromotionType.FREE_SHIPPING,
                Promotion.PromotionType.BUY_X_GET_Y
        });
        
        return "vendor/promotions/form";
    }
    
    // Xử lý tạo khuyến mãi mới
    @PostMapping("/create")
    public String createPromotion(@Valid @ModelAttribute("promotionForm") VendorPromotionForm form,
                                 BindingResult result,
                                 @RequestParam(required = false) Long shopId,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {
        
        User vendor = UserUtils.getCurrentUser(session);
        if (vendor == null || !UserUtils.isVendor(vendor)) {
            return "redirect:/login";
        }
        
        List<Long> shopIds = shopService.findShopIdsByVendor(vendor);
        if (shopIds.isEmpty()) {
            return "redirect:/vendor/shops";
        }
        
        // Nếu không có shopId từ param, lấy shop đầu tiên
        if (shopId == null) {
            shopId = shopIds.get(0);
        }
        
        // Kiểm tra shop có thuộc vendor không
        if (!shopIds.contains(shopId)) {
            return "redirect:/vendor/shops";
        }
        
        Shop shop = shopService.findById(shopId).orElse(null);
        
        // Trim và validate promotion code
        if (form.getPromotionCode() != null) {
            form.setPromotionCode(form.getPromotionCode().trim().toUpperCase());
        }
        
        // Validation
        if (result.hasErrors()) {
            model.addAttribute("shop", shop);
            model.addAttribute("shopId", shopId);
            model.addAttribute("promotionTypes", new Promotion.PromotionType[]{
                    Promotion.PromotionType.PERCENTAGE,
                    Promotion.PromotionType.FIXED_AMOUNT,
                    Promotion.PromotionType.FREE_SHIPPING,
                    Promotion.PromotionType.BUY_X_GET_Y
            });
            return "vendor/promotions/form";
        }
        
        // Kiểm tra mã khuyến mãi trùng lặp
        if (!promotionService.validatePromotionCodeForShop(shopId, form.getPromotionCode())) {
            result.rejectValue("promotionCode", "error.promotionCode", "Mã khuyến mãi đã tồn tại trong shop này");
            model.addAttribute("shop", shop);
            model.addAttribute("shopId", shopId);
            model.addAttribute("promotionTypes", new Promotion.PromotionType[]{
                    Promotion.PromotionType.PERCENTAGE,
                    Promotion.PromotionType.FIXED_AMOUNT,
                    Promotion.PromotionType.FREE_SHIPPING,
                    Promotion.PromotionType.BUY_X_GET_Y
            });
            return "vendor/promotions/form";
        }
        
        // Kiểm tra mã khuyến mãi trùng lặp toàn hệ thống (tránh lỗi UNIQUE ở DB)
        if (!promotionService.validatePromotionCode(form.getPromotionCode())) {
            result.rejectValue("promotionCode", "error.promotionCode", "Mã khuyến mãi đã tồn tại");
            model.addAttribute("shop", shop);
            model.addAttribute("shopId", shopId);
            model.addAttribute("promotionTypes", new Promotion.PromotionType[]{
                    Promotion.PromotionType.PERCENTAGE,
                    Promotion.PromotionType.FIXED_AMOUNT,
                    Promotion.PromotionType.FREE_SHIPPING,
                    Promotion.PromotionType.BUY_X_GET_Y
            });
            return "vendor/promotions/form";
        }

        // Kiểm tra ngày tháng
        if (!form.isValidDateRange()) {
            result.rejectValue("endDate", "error.endDate", "Ngày kết thúc phải sau ngày bắt đầu");
            model.addAttribute("shop", shop);
            model.addAttribute("shopId", shopId);
            model.addAttribute("promotionTypes", new Promotion.PromotionType[]{
                    Promotion.PromotionType.PERCENTAGE,
                    Promotion.PromotionType.FIXED_AMOUNT,
                    Promotion.PromotionType.FREE_SHIPPING,
                    Promotion.PromotionType.BUY_X_GET_Y
            });
            return "vendor/promotions/form";
        }
        
        // Kiểm tra giá trị giảm giá
        if (!form.isValidDiscountAmounts()) {
            result.rejectValue("discountValue", "error.discountValue", "Giá trị giảm giá không hợp lệ");
            model.addAttribute("shop", shop);
            model.addAttribute("shopId", shopId);
            model.addAttribute("promotionTypes", new Promotion.PromotionType[]{
                    Promotion.PromotionType.PERCENTAGE,
                    Promotion.PromotionType.FIXED_AMOUNT,
                    Promotion.PromotionType.FREE_SHIPPING,
                    Promotion.PromotionType.BUY_X_GET_Y
            });
            return "vendor/promotions/form";
        }
        
        try {
            promotionService.createPromotionForShop(shopId, form, vendor);
            redirectAttributes.addFlashAttribute("success", "Tạo khuyến mãi thành công!");
            return "redirect:/vendor/promotions?shopId=" + shopId;
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error creating promotion: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
            return "redirect:/vendor/promotions/create?shopId=" + shopId;
        }
    }
    
    // Form chỉnh sửa khuyến mãi
    @GetMapping("/edit/{id}")
    public String editPromotionForm(@PathVariable Long id,
                                   @RequestParam(required = false) Long shopId,
                                   HttpSession session,
                                   Model model) {
        
        User vendor = UserUtils.getCurrentUser(session);
        if (vendor == null || !UserUtils.isVendor(vendor)) {
            return "redirect:/login";
        }
        
        List<Long> shopIds = shopService.findShopIdsByVendor(vendor);
        if (shopIds.isEmpty()) {
            return "redirect:/vendor/shops";
        }
        
        if (shopId == null) {
            shopId = shopIds.get(0);
        }
        if (!shopIds.contains(shopId)) {
            return "redirect:/vendor/promotions";
        }

        Optional<Promotion> promotionOpt = promotionService.getPromotionByShopAndId(shopId, id);
        
        if (promotionOpt.isEmpty()) {
            return "redirect:/vendor/promotions";
        }
        
        Promotion promotion = promotionOpt.get();
        Shop shop = promotion.getShop();
        
        VendorPromotionForm form = new VendorPromotionForm();
        form.setPromotionName(promotion.getPromotionName());
        form.setDescription(promotion.getDescription());
        form.setPromotionCode(promotion.getPromotionCode());
        form.setPromotionType(promotion.getPromotionType());
        form.setDiscountValue(promotion.getDiscountValue());
        form.setMinimumOrderAmount(promotion.getMinimumOrderAmount());
        form.setMaximumDiscountAmount(promotion.getMaximumDiscountAmount());
        form.setUsageLimit(promotion.getUsageLimit());
        form.setStartDate(promotion.getStartDate());
        form.setEndDate(promotion.getEndDate());
        form.setIsActive(promotion.getIsActive());
        
        model.addAttribute("promotionForm", form);
        model.addAttribute("promotion", promotion);
        model.addAttribute("shop", shop);
        model.addAttribute("shopId", shopId);
        model.addAttribute("promotionTypes", new Promotion.PromotionType[]{
                Promotion.PromotionType.PERCENTAGE,
                Promotion.PromotionType.FIXED_AMOUNT,
                Promotion.PromotionType.FREE_SHIPPING,
                Promotion.PromotionType.BUY_X_GET_Y
        });
        model.addAttribute("isEdit", true);
        
        return "vendor/promotions/form";
    }
    
    // Xử lý chỉnh sửa khuyến mãi
    @PostMapping("/edit/{id}")
    public String editPromotion(@PathVariable Long id,
                               @Valid @ModelAttribute("promotionForm") VendorPromotionForm form,
                               BindingResult result,
                               @RequestParam(required = false) Long shopId,
                               HttpSession session,
                               RedirectAttributes redirectAttributes,
                               Model model) {
        
        User vendor = UserUtils.getCurrentUser(session);
        if (vendor == null || !UserUtils.isVendor(vendor)) {
            return "redirect:/login";
        }
        
        List<Long> shopIds = shopService.findShopIdsByVendor(vendor);
        if (shopIds.isEmpty()) {
            return "redirect:/vendor/shops";
        }
        
        if (shopId == null) {
            shopId = shopIds.get(0);
        }
        if (!shopIds.contains(shopId)) {
            return "redirect:/vendor/promotions";
        }
        Optional<Promotion> promotionOpt = promotionService.getPromotionByShopAndId(shopId, id);
        
        if (promotionOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Khuyến mãi không tồn tại");
            return "redirect:/vendor/promotions";
        }
        
        Promotion promotion = promotionOpt.get();
        Shop shop = promotion.getShop();
        
        // Trim và validate promotion code
        if (form.getPromotionCode() != null) {
            form.setPromotionCode(form.getPromotionCode().trim().toUpperCase());
        }
        
        // Validation
        if (result.hasErrors()) {
            model.addAttribute("promotion", promotion);
            model.addAttribute("shop", shop);
            model.addAttribute("shopId", shopId);
            model.addAttribute("promotionTypes", new Promotion.PromotionType[]{
                    Promotion.PromotionType.PERCENTAGE,
                    Promotion.PromotionType.FIXED_AMOUNT,
                    Promotion.PromotionType.FREE_SHIPPING,
                    Promotion.PromotionType.BUY_X_GET_Y
            });
            model.addAttribute("isEdit", true);
            return "vendor/promotions/form";
        }
        
        // Kiểm tra mã khuyến mãi trùng lặp (trừ mã hiện tại)
        if (!promotion.getPromotionCode().equals(form.getPromotionCode()) && 
            !promotionService.validatePromotionCodeForShop(shopId, form.getPromotionCode())) {
            result.rejectValue("promotionCode", "error.promotionCode", "Mã khuyến mãi đã tồn tại trong shop này");
            model.addAttribute("promotion", promotion);
            model.addAttribute("shop", shop);
            model.addAttribute("shopId", shopId);
            model.addAttribute("promotionTypes", new Promotion.PromotionType[]{
                    Promotion.PromotionType.PERCENTAGE,
                    Promotion.PromotionType.FIXED_AMOUNT,
                    Promotion.PromotionType.FREE_SHIPPING,
                    Promotion.PromotionType.BUY_X_GET_Y
            });
            model.addAttribute("isEdit", true);
            return "vendor/promotions/form";
        }
        
        // Kiểm tra ngày tháng
        if (!form.isValidDateRange()) {
            result.rejectValue("endDate", "error.endDate", "Ngày kết thúc phải sau ngày bắt đầu");
            model.addAttribute("promotion", promotion);
            model.addAttribute("shop", shop);
            model.addAttribute("shopId", shopId);
            model.addAttribute("promotionTypes", new Promotion.PromotionType[]{
                    Promotion.PromotionType.PERCENTAGE,
                    Promotion.PromotionType.FIXED_AMOUNT,
                    Promotion.PromotionType.FREE_SHIPPING,
                    Promotion.PromotionType.BUY_X_GET_Y
            });
            model.addAttribute("isEdit", true);
            return "vendor/promotions/form";
        }
        
        // Kiểm tra giá trị giảm giá
        if (!form.isValidDiscountAmounts()) {
            result.rejectValue("discountValue", "error.discountValue", "Giá trị giảm giá không hợp lệ");
            model.addAttribute("promotion", promotion);
            model.addAttribute("shop", shop);
            model.addAttribute("shopId", shopId);
            model.addAttribute("promotionTypes", new Promotion.PromotionType[]{
                    Promotion.PromotionType.PERCENTAGE,
                    Promotion.PromotionType.FIXED_AMOUNT,
                    Promotion.PromotionType.FREE_SHIPPING,
                    Promotion.PromotionType.BUY_X_GET_Y
            });
            model.addAttribute("isEdit", true);
            return "vendor/promotions/form";
        }
        
        try {
            promotionService.updatePromotionForShop(shopId, id, form, vendor);
            redirectAttributes.addFlashAttribute("success", "Cập nhật khuyến mãi thành công!");
            return "redirect:/vendor/promotions?shopId=" + shopId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
            return "redirect:/vendor/promotions/edit/" + id + "?shopId=" + shopId;
        }
    }
    
    // Xóa khuyến mãi
    @PostMapping("/delete/{id}")
    public String deletePromotion(@PathVariable Long id,
                                 @RequestParam(required = false) Long shopId,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        
        User vendor = UserUtils.getCurrentUser(session);
        if (vendor == null || !UserUtils.isVendor(vendor)) {
            return "redirect:/login";
        }
        
        List<Long> shopIds = shopService.findShopIdsByVendor(vendor);
        if (shopIds.isEmpty()) {
            return "redirect:/vendor/shops";
        }
        
        if (shopId == null) {
            shopId = shopIds.get(0);
        }
        if (!shopIds.contains(shopId)) {
            return "redirect:/vendor/promotions";
        }
        
        try {
            promotionService.deletePromotionFromShop(shopId, id);
            redirectAttributes.addFlashAttribute("success", "Xóa khuyến mãi thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }
        
        return "redirect:/vendor/promotions?shopId=" + shopId;
    }
    
    // Toggle trạng thái khuyến mãi
    @PostMapping("/toggle/{id}")
    public String togglePromotionStatus(@PathVariable Long id,
                                       @RequestParam(required = false) Long shopId,
                                       HttpSession session,
                                       RedirectAttributes redirectAttributes) {
        
        User vendor = UserUtils.getCurrentUser(session);
        if (vendor == null || !UserUtils.isVendor(vendor)) {
            return "redirect:/login";
        }
        
        List<Long> shopIds = shopService.findShopIdsByVendor(vendor);
        if (shopIds.isEmpty()) {
            return "redirect:/vendor/shops";
        }
        
        if (shopId == null) {
            shopId = shopIds.get(0);
        }
        if (!shopIds.contains(shopId)) {
            return "redirect:/vendor/promotions";
        }
        
        try {
            Promotion promotion = promotionService.togglePromotionStatusForShop(shopId, id);
            String message = promotion.getIsActive() ? "Kích hoạt khuyến mãi thành công!" : "Tắt khuyến mãi thành công!";
            redirectAttributes.addFlashAttribute("success", message);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }
        
        return "redirect:/vendor/promotions?shopId=" + shopId;
    }
    
    // Chi tiết khuyến mãi
    @GetMapping("/detail/{id}")
    public String promotionDetail(@PathVariable Long id,
                                 @RequestParam(required = false) Long shopId,
                                 HttpSession session,
                                 Model model) {
        
        User vendor = UserUtils.getCurrentUser(session);
        if (vendor == null || !UserUtils.isVendor(vendor)) {
            return "redirect:/login";
        }
        
        List<Long> shopIds = shopService.findShopIdsByVendor(vendor);
        if (shopIds.isEmpty()) {
            return "redirect:/vendor/shops";
        }
        
        if (shopId == null) {
            shopId = shopIds.get(0);
        }
        if (!shopIds.contains(shopId)) {
            return "redirect:/vendor/promotions";
        }
        Optional<Promotion> promotionOpt = promotionService.getPromotionByShopAndId(shopId, id);
        
        if (promotionOpt.isEmpty()) {
            return "redirect:/vendor/promotions";
        }
        
        Promotion promotion = promotionOpt.get();
        Shop shop = promotion.getShop();
        
        model.addAttribute("promotion", promotion);
        model.addAttribute("shop", shop);
        
        return "vendor/promotions/detail";
    }

    /**
     * Bulk actions: activate, deactivate, delete
     */
    @PostMapping("/bulk-action")
    public String bulkAction(@RequestParam("action") String action,
                             @RequestParam(value = "ids", required = false) List<Long> ids,
                             @RequestParam(required = false) Long shopId,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {

        User vendor = UserUtils.getCurrentUser(session);
        if (vendor == null || !UserUtils.isVendor(vendor)) {
            return "redirect:/login";
        }

        if (ids == null || ids.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng chọn ít nhất một khuyến mãi");
            return "redirect:/vendor/promotions";
        }

        List<Long> shopIds = shopService.findShopIdsByVendor(vendor);
        if (shopIds.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Bạn chưa có shop nào");
            return "redirect:/vendor/promotions";
        }

        if (shopId == null) {
            shopId = shopIds.get(0);
        }
        if (!shopIds.contains(shopId)) {
            redirectAttributes.addFlashAttribute("error", "Shop không hợp lệ");
            return "redirect:/vendor/promotions";
        }

        try {
            switch (action) {
                case "activate":
                    promotionService.toggleMultiplePromotionsForShop(shopId, ids, true);
                    redirectAttributes.addFlashAttribute("success", "Đã kích hoạt các khuyến mãi đã chọn");
                    break;
                case "deactivate":
                    promotionService.toggleMultiplePromotionsForShop(shopId, ids, false);
                    redirectAttributes.addFlashAttribute("success", "Đã tạm dừng các khuyến mãi đã chọn");
                    break;
                case "delete":
                    promotionService.deleteMultiplePromotionsFromShop(shopId, ids);
                    redirectAttributes.addFlashAttribute("success", "Đã xóa các khuyến mãi đã chọn");
                    break;
                default:
                    redirectAttributes.addFlashAttribute("error", "Hành động không hợp lệ");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }

        return "redirect:/vendor/promotions?shopId=" + shopId;
    }
}
