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
import jakarta.servlet.http.HttpSession;
import vn.entity.FlashSale;
import vn.entity.FlashSaleProduct;
import vn.entity.Product;
import vn.entity.User;
import vn.repository.ProductRepository;
import vn.service.FlashSaleService;
import vn.service.ProductService;

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
                model.addAttribute("flashSale", flashSaleOpt.get());
                model.addAttribute("statuses", FlashSale.FlashSaleStatus.values());
                return "admin/flash-sales/form";
            } else {
                return "redirect:/admin/flash-sales";
            }
        } catch (Exception e) {
            System.err.println("Error loading flash sale for edit: " + e.getMessage());
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
            flashSaleService.updateFlashSale(id, flashSale);
            redirectAttributes.addFlashAttribute("success", "Cập nhật flash sale thành công!");
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
            
            model.addAttribute("flashSale", flashSale);
            model.addAttribute("products", products);
            
            // Get all products for adding
            List<Product> allProducts = productService.findAll();
            model.addAttribute("allProducts", allProducts);
            
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

