package vn.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.servlet.http.HttpSession;
import vn.entity.FlashSale;
import vn.entity.FlashSaleProduct;
import vn.entity.Product;
import vn.entity.User;
import vn.repository.ProductRepository;
import vn.service.CategoryService;
import vn.service.FlashSaleService;
import vn.service.ProductService;
import vn.service.ShopService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Admin controller for Flash Sale management
 * @author OneShop Team
 */
@Controller
@RequestMapping("/admin/flash-sales")
public class AdminFlashSaleController {
    
    @Autowired
    private FlashSaleService flashSaleService;
    
    @Autowired
    private ProductService productService;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private ShopService shopService;
    
    @Autowired
    private CategoryService categoryService;
    
    /**
     * Display all flash sales with pagination
     */
    @GetMapping
    public String listFlashSales(Model model,
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "10") int size,
                                 @RequestParam(defaultValue = "flashSaleId") String sortBy,
                                 @RequestParam(defaultValue = "desc") String sortDir,
                                 @RequestParam(required = false) String search,
                                 @RequestParam(required = false) String status) {
        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<FlashSale> flashSales;
            if (search != null && !search.trim().isEmpty()) {
                flashSales = flashSaleService.searchFlashSalesByName(search, pageable);
                model.addAttribute("search", search);
            } else if (status != null && !status.trim().isEmpty()) {
                try {
                    FlashSale.FlashSaleStatus statusEnum = FlashSale.FlashSaleStatus.valueOf(status.toUpperCase());
                    flashSales = flashSaleService.getFlashSalesByStatus(statusEnum, pageable);
                    model.addAttribute("status", status);
                } catch (IllegalArgumentException e) {
                    flashSales = flashSaleService.getFlashSalesWithPagination(pageable);
                }
            } else {
                flashSales = flashSaleService.getFlashSalesWithPagination(pageable);
            }
            
            model.addAttribute("flashSales", flashSales);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", flashSales.getTotalPages());
            model.addAttribute("totalElements", flashSales.getTotalElements());
            model.addAttribute("sortBy", sortBy);
            model.addAttribute("sortDir", sortDir);
            model.addAttribute("size", size);
            model.addAttribute("statuses", FlashSale.FlashSaleStatus.values());
            
            // Statistics
            model.addAttribute("totalCount", flashSaleService.getTotalFlashSaleCount());
            model.addAttribute("activeCount", flashSaleService.getActiveFlashSaleCount());
            model.addAttribute("draftCount", flashSaleService.getFlashSaleCountByStatus(FlashSale.FlashSaleStatus.DRAFT));
            model.addAttribute("scheduledCount", flashSaleService.getFlashSaleCountByStatus(FlashSale.FlashSaleStatus.SCHEDULED));
            
            return "admin/flash-sales/list";
        } catch (Exception e) {
            System.err.println("Error in flash sales controller: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Có lỗi xảy ra khi tải dữ liệu flash sale: " + e.getMessage());
            return "admin/flash-sales/list";
        }
    }
    
    /**
     * Display flash sale form for adding new flash sale
     */
    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("flashSale", new FlashSale());
        model.addAttribute("statuses", FlashSale.FlashSaleStatus.values());
        return "admin/flash-sales/form";
    }
    
    /**
     * Handle creating new flash sale
     */
    @PostMapping("/create")
    public String create(@ModelAttribute FlashSale flashSale,
                        HttpSession session,
                        RedirectAttributes redirectAttributes) {
        try {
            User user = (User) session.getAttribute("user");
            if (user == null) {
                return "redirect:/login";
            }
            
            flashSaleService.createFlashSale(flashSale, user);
            redirectAttributes.addFlashAttribute("success", "Tạo flash sale thành công!");
            return "redirect:/admin/flash-sales";
        } catch (Exception e) {
            System.err.println("Error creating flash sale: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi tạo flash sale: " + e.getMessage());
            return "redirect:/admin/flash-sales/create";
        }
    }
    
    /**
     * Display flash sale form for editing
     */
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        try {
            Optional<FlashSale> flashSaleOpt = flashSaleService.getFlashSaleById(id);
            if (flashSaleOpt.isPresent()) {
                FlashSale flashSale = flashSaleOpt.get();
                model.addAttribute("flashSale", flashSale);
                model.addAttribute("statuses", FlashSale.FlashSaleStatus.values());
                return "admin/flash-sales/form";
            } else {
                return "redirect:/admin/flash-sales";
            }
        } catch (Exception e) {
            System.err.println("Error loading flash sale for edit: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/admin/flash-sales";
        }
    }
    
    /**
     * Handle updating flash sale
     */
    @PostMapping("/edit/{id}")
    public String update(@PathVariable Long id,
                        @ModelAttribute FlashSale flashSale,
                        RedirectAttributes redirectAttributes) {
        try {
            // Get existing flash sale to check status before update
            java.util.Optional<FlashSale> existingOpt = flashSaleService.getFlashSaleById(id);
            String previousStatus = existingOpt.isPresent() ? existingOpt.get().getStatus().name() : "";
            
            FlashSale updated = flashSaleService.updateFlashSale(id, flashSale);
            
            // Check if status was auto-updated
            String message = "Cập nhật flash sale thành công!";
            if (previousStatus.equals("ENDED") || previousStatus.equals("CANCELLED")) {
                if (updated.getStatus() == FlashSale.FlashSaleStatus.ACTIVE) {
                    message = "Cập nhật flash sale thành công! Flash sale đã được tự động kích hoạt vì thời gian mới đang trong khoảng active.";
                } else if (updated.getStatus() == FlashSale.FlashSaleStatus.SCHEDULED) {
                    message = "Cập nhật flash sale thành công! Flash sale đã được tự động lên lịch vì thời gian bắt đầu trong tương lai.";
                }
            }
            
            redirectAttributes.addFlashAttribute("success", message);
        } catch (Exception e) {
            System.err.println("Error updating flash sale: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi cập nhật flash sale: " + e.getMessage());
        }
        return "redirect:/admin/flash-sales";
    }
    
    /**
     * Delete flash sale
     */
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            flashSaleService.deleteFlashSale(id);
            redirectAttributes.addFlashAttribute("success", "Xóa flash sale thành công!");
        } catch (Exception e) {
            System.err.println("Error deleting flash sale: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi xóa flash sale: " + e.getMessage());
        }
        return "redirect:/admin/flash-sales";
    }
    
    /**
     * Manage products in flash sale
     */
    @GetMapping("/{id}/products")
    public String manageProducts(@PathVariable Long id, Model model) {
        try {
            Optional<FlashSale> flashSaleOpt = flashSaleService.getFlashSaleById(id);
            if (flashSaleOpt.isEmpty()) {
                return "redirect:/admin/flash-sales";
            }
            
            FlashSale flashSale = flashSaleOpt.get();
            List<FlashSaleProduct> products = flashSaleService.getProductsByFlashSaleId(id);
            
            // Get list of product IDs already in flash sale (to exclude from selection)
            List<Long> existingProductIds = products.stream()
                .map(fsp -> fsp.getProduct().getProductId())
                .collect(java.util.stream.Collectors.toList());
            
            model.addAttribute("flashSale", flashSale);
            model.addAttribute("products", products);
            model.addAttribute("existingProductIds", existingProductIds);
            
            // Get shops and categories for filters
            model.addAttribute("shops", shopService.findAll());
            model.addAttribute("categories", categoryService.getAllCategories());
            
            return "admin/flash-sales/products";
        } catch (Exception e) {
            System.err.println("Error loading products: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/admin/flash-sales";
        }
    }
    
    /**
     * Add product to flash sale (AJAX)
     */
    @PostMapping("/{id}/products/add")
    @ResponseBody
    public java.util.Map<String, Object> addProduct(@PathVariable Long id,
                                                     @RequestParam Long productId,
                                                     @RequestParam BigDecimal flashSalePrice,
                                                     @RequestParam Integer quantityLimit,
                                                     @RequestParam(required = false, defaultValue = "1") Integer maxQuantityPerUser,
                                                     @RequestParam(required = false, defaultValue = "0") Integer displayOrder) {
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        try {
            FlashSaleProduct fsp = flashSaleService.addProductToFlashSale(
                id, productId, flashSalePrice, quantityLimit, maxQuantityPerUser, displayOrder);
            
            response.put("success", true);
            response.put("message", "Thêm sản phẩm thành công!");
            response.put("flashSaleProduct", fsp);
        } catch (Exception e) {
            System.err.println("Error adding product: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra: " + e.getMessage());
        }
        return response;
    }
    
    /**
     * Remove product from flash sale
     */
    @PostMapping("/{id}/products/remove")
    @ResponseBody
    public java.util.Map<String, Object> removeProduct(@PathVariable Long id,
                                                       @RequestParam Long productId) {
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        try {
            flashSaleService.removeProductFromFlashSale(id, productId);
            response.put("success", true);
            response.put("message", "Xóa sản phẩm thành công!");
        } catch (Exception e) {
            System.err.println("Error removing product: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra: " + e.getMessage());
        }
        return response;
    }
    
    /**
     * API: Search products with filters (for AJAX)
     */
    @GetMapping("/{id}/products/search")
    @ResponseBody
    public ResponseEntity<java.util.Map<String, Object>> searchProducts(
            @PathVariable Long id,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long shopId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        
        try {
            // Get existing product IDs in flash sale
            List<FlashSaleProduct> existingProducts = flashSaleService.getProductsByFlashSaleId(id);
            List<Long> existingProductIds = existingProducts.stream()
                .map(fsp -> fsp.getProduct().getProductId())
                .collect(java.util.stream.Collectors.toList());
            
            // Get all products first, then filter
            List<Product> allProducts;
            
            if (shopId != null && search != null && !search.trim().isEmpty()) {
                // Get all matching products (no pagination yet)
                org.springframework.data.domain.Pageable pageableAll = 
                    org.springframework.data.domain.PageRequest.of(0, 10000); // Large page size
                org.springframework.data.domain.Page<Product> productPage = 
                    productService.findByShopAndName(shopId, search.trim(), pageableAll);
                allProducts = productPage.getContent();
            } else if (shopId != null) {
                allProducts = productService.findByShopId(shopId);
            } else if (search != null && !search.trim().isEmpty()) {
                org.springframework.data.domain.Pageable pageableAll = 
                    org.springframework.data.domain.PageRequest.of(0, 10000);
                org.springframework.data.domain.Page<Product> productPage = 
                    productService.findByProductNameContainingIgnoreCase(search.trim(), pageableAll);
                allProducts = productPage.getContent();
            } else {
                allProducts = productService.findAll();
            }
            
            // Apply filters
            List<Product> filteredProducts = allProducts.stream()
                .filter(p -> Boolean.TRUE.equals(p.getStatus())) // Only active products
                .filter(p -> !existingProductIds.contains(p.getProductId())) // Not already in flash sale
                .filter(p -> {
                    // Filter by category if provided
                    if (categoryId != null) {
                        return p.getCategory() != null && p.getCategory().getCategoryId().equals(categoryId);
                    }
                    return true;
                })
                .collect(java.util.stream.Collectors.toList());
            
            // Apply pagination after filtering
            int totalElements = filteredProducts.size();
            int totalPages = (int) Math.ceil((double) totalElements / size);
            int start = page * size;
            int end = Math.min(start + size, totalElements);
            
            List<Product> paginatedProducts = filteredProducts.subList(
                Math.min(start, totalElements), 
                end
            );
            
            // Convert to DTO
            List<java.util.Map<String, Object>> productDTOs = paginatedProducts.stream()
                .map(p -> {
                    java.util.Map<String, Object> dto = new java.util.HashMap<>();
                    dto.put("productId", p.getProductId());
                    dto.put("productName", p.getProductName());
                    dto.put("price", p.getPrice());
                    dto.put("discount", p.getDiscount() != null ? p.getDiscount() : 0);
                    dto.put("quantity", p.getQuantity());
                    dto.put("productImage", p.getProductImage());
                    dto.put("shopName", p.getShop() != null ? p.getShop().getShopName() : "N/A");
                    dto.put("categoryName", p.getCategory() != null ? p.getCategory().getCategoryName() : "N/A");
                    return dto;
                })
                .collect(java.util.stream.Collectors.toList());
            
            result.put("success", true);
            result.put("products", productDTOs);
            result.put("totalElements", totalElements);
            result.put("currentPage", page);
            result.put("totalPages", totalPages);
            
        } catch (Exception e) {
            System.err.println("Error searching products: " + e.getMessage());
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "Có lỗi xảy ra: " + e.getMessage());
        }
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * API: Bulk add products to flash sale
     */
    @PostMapping("/{id}/products/bulk-add")
    @ResponseBody
    public java.util.Map<String, Object> bulkAddProducts(
            @PathVariable Long id,
            @RequestParam String productIds, // Comma-separated product IDs
            @RequestParam BigDecimal defaultFlashSalePrice,
            @RequestParam Integer defaultQuantityLimit,
            @RequestParam(required = false, defaultValue = "1") Integer defaultMaxQuantityPerUser) {
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        
        try {
            String[] ids = productIds.split(",");
            int successCount = 0;
            int failCount = 0;
            List<String> errors = new java.util.ArrayList<>();
            
            for (String idStr : ids) {
                try {
                    Long productId = Long.parseLong(idStr.trim());
                    flashSaleService.addProductToFlashSale(
                        id, productId, defaultFlashSalePrice, 
                        defaultQuantityLimit, defaultMaxQuantityPerUser, 0);
                    successCount++;
                } catch (Exception e) {
                    failCount++;
                    errors.add("Product " + idStr + ": " + e.getMessage());
                }
            }
            
            response.put("success", true);
            response.put("message", "Đã thêm " + successCount + " sản phẩm thành công" + 
                        (failCount > 0 ? ", " + failCount + " sản phẩm thất bại" : ""));
            response.put("successCount", successCount);
            response.put("failCount", failCount);
            if (!errors.isEmpty()) {
                response.put("errors", errors);
            }
            
        } catch (Exception e) {
            System.err.println("Error bulk adding products: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra: " + e.getMessage());
        }
        
        return response;
    }
    
    /**
     * Activate flash sale
     */
    @GetMapping("/{id}/activate")
    public String activate(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            flashSaleService.activateFlashSale(id);
            redirectAttributes.addFlashAttribute("success", "Kích hoạt flash sale thành công!");
        } catch (Exception e) {
            System.err.println("Error activating flash sale: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }
        return "redirect:/admin/flash-sales";
    }
    
    /**
     * Cancel flash sale
     */
    @GetMapping("/{id}/cancel")
    public String cancel(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            flashSaleService.cancelFlashSale(id);
            redirectAttributes.addFlashAttribute("success", "Hủy flash sale thành công!");
        } catch (Exception e) {
            System.err.println("Error cancelling flash sale: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }
        return "redirect:/admin/flash-sales";
    }
    
    /**
     * Schedule flash sale
     */
    @GetMapping("/{id}/schedule")
    public String schedule(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            flashSaleService.scheduleFlashSale(id);
            redirectAttributes.addFlashAttribute("success", "Lên lịch flash sale thành công!");
        } catch (Exception e) {
            System.err.println("Error scheduling flash sale: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }
        return "redirect:/admin/flash-sales";
    }
    
    /**
     * End flash sale manually
     */
    @GetMapping("/{id}/end")
    public String end(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            flashSaleService.endFlashSale(id);
            redirectAttributes.addFlashAttribute("success", "Kết thúc flash sale thành công!");
        } catch (Exception e) {
            System.err.println("Error ending flash sale: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }
        return "redirect:/admin/flash-sales";
    }
}

